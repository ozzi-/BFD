(ns asciinema.gif.main
  (:require [cljs.nodejs :as nodejs]
            [asciinema.gif.helpers :refer [safe-map]]
            [asciinema.gif.log :as log]
            [asciinema.player.asciicast :as asciicast]
            [asciinema.player.frames :as frames]
            [asciinema.player.screen :as screen]
            [cljs.core.async :refer [<! put! chan]]
            [clojure.string :as str])
  (:require-macros [asciinema.gif.macros :refer [<?]]
                   [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(def fs (nodejs/require "fs"))
(def phantomjs (nodejs/require "phantomjs-prebuilt"))
(def path (nodejs/require "path"))
(def child-process (nodejs/require "child_process"))
(def zlib (nodejs/require "zlib"))
(def env (.-env nodejs/process))

(def renderer-html-path (.resolve path (js* "__dirname") "page" "asciicast2gif.html"))
(def renderer-js-path (.resolve path (js* "__dirname") "renderer.js"))

(defn- to-json [obj]
  (-> obj
      clj->js
      JSON.stringify))

(defn- http-get [url ch]
  (let [proto (-> url (str/split #":") first)
        gunzip (.createGunzip zlib)
        client (nodejs/require proto)]
    (.get client url (fn [res]
                       (let [status (.-statusCode res)]
                         (if (contains? #{301 302} status)
                           (let [url (-> res .-headers (aget "location"))]
                             (http-get url ch))
                           (let [data (atom "")
                                 encoding (-> res .-headers (aget "content-encoding"))
                                 stream (if (= encoding "gzip")
                                          (do
                                            (.pipe res gunzip)
                                            gunzip)
                                          res)]
                             (.setEncoding stream "utf8")
                             (.on stream "data" (fn [chunk]
                                                  (swap! data str chunk)))
                             (.on stream "end" (fn []
                                                 (put! ch @data))))))))))

(defn- read-file [path ch]
  (let [data (.readFileSync fs path "utf8")]
    (put! ch data)))

(defn- load-asciicast [url]
  (log/info (str "Loading " url "..."))
  (let [ch (chan 1 (safe-map asciicast/load))]
    (if (str/starts-with? url "http")
      (http-get url ch)
      (read-file url ch))
    ch))

(defn- spawn-phantomjs [script-path & args]
  (log/info "Spawning PhantomJS renderer...")
  (let [program (apply (.-exec phantomjs) script-path args)]
    (.pipe (.-stdout program) (.-stdout nodejs/process))
    (.pipe (.-stderr program) (.-stderr nodejs/process))
    program))

(defn- wait-for-exit [program]
  (let [ch (chan)]
    (.on program "exit" #(put! ch (if (zero? %)
                                    true
                                    (js/Error. (str "program exited with code " %)))))
    ch))

(defn- exit [code]
  (.exit nodejs/process code))

(defn- write-to-stdin [program text]
  (.write (.-stdin program) text))

(defn- close-stdin [program]
  (.end (.-stdin program)))

(defn- shell [cmd]
  (.execSync child-process cmd))

(defn- screen-json [screen]
  (to-json {:lines (screen/lines screen)
            :cursor (screen/cursor screen)}))

(defn- gen-png [renderer pngs-dir idx screen]
  (let [path (str pngs-dir "/" idx ".png")]
    (log/debug "Sending frame" idx "to renderer...")
    (write-to-stdin renderer (str (screen-json screen) "\n"))
    (write-to-stdin renderer (str path "\n"))
    path))

(defn- gen-frame [renderer pngs-dir idx [delay screen]]
  [delay (gen-png renderer pngs-dir idx screen)])

(defn- gen-image-frames [renderer pngs-dir frames]
  (log/info "Generating frame screenshots...")
  (->> frames
       frames/to-relative-time
       (map-indexed (partial gen-frame renderer pngs-dir)) ; generate PNG images
       doall))

(defn- delay-arg [delay-in-secs]
  (str "-delay " (int (* delay-in-secs 100))))

(defn- single-frame-args [[delay path]]
  [(delay-arg delay) path])

(defn- all-frame-args [delays-and-paths]
  (->> delays-and-paths
       (apply concat)
       (#(concat % [1])) ; add 1 second delay at the end
       (drop 1) ; GIF animations don't have delay *before* first image
       (partition 2)
       (map reverse)
       (mapcat single-frame-args)))

(defn- full-cmd [args out-path]
  (str "convert -loop 0 "
       (str/join " " args)
       " -layers Optimize gif:- | gifsicle -k 32 -O2 -Okeep-empty -o "
       out-path
       " -"))

(defn- gen-gif [delays-and-paths out-path]
  (log/info "Combining screenshots into GIF file...")
  (let [cmd (-> delays-and-paths all-frame-args (full-cmd out-path))]
    (log/debug "Executing:" cmd)
    (shell cmd)))

(defn -main [& args]
  (when-not (= (count args) 6)
    (log/error "Error: bad number of args:" (count args))
    (exit 1))
  (go
    (let [[url out-path tmp-dir theme speed scale] args
          speed (js/parseFloat speed)
          forced-width (aget env "WIDTH")
          forced-height (aget env "HEIGHT")
          {:keys [width height frames]} (<? (load-asciicast url))
          width (or forced-width width)
          height (or forced-height height)
          renderer (spawn-phantomjs renderer-js-path renderer-html-path width height theme scale)
          xf (frames/accelerate-xf speed)
          delays-and-paths (gen-image-frames renderer tmp-dir (sequence xf frames))]
      (close-stdin renderer)
      (<? (wait-for-exit renderer))
      (gen-gif delays-and-paths out-path))))

(set! *main-cli-fn* -main)
