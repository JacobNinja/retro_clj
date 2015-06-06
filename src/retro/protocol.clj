(ns retro.protocol
  (:require [retro.encoding :as encoding])
  (:refer-clojure :exclude [partition-by split]))

(def packet-header-size 2)

(defn- partition-by [f step coll]
  "Takes a function f that returns an integer for partition size"
  (loop [c coll r []]
    (if-not (empty? c)
      (let [length (f (take step c))
            adjusted-c (drop step c)]
        (recur
           (drop length adjusted-c)
           (conj r (take length adjusted-c))))
      r)))

(defn- packet-header [raw-packet]
  (encoding/decode-b64 (take packet-header-size raw-packet)))

(defn split
  ([body] (split body #" "))
  ([body delimiter]
   (clojure.string/split body delimiter)))

(defn packet-body [body]
  (map clojure.string/join
       (partition-by packet-header packet-header-size body)))

(defn packet-length [p]
  (+ (+ packet-header-size 3)
     (reduce + (map #(+ packet-header-size (count %)) (:body p)))))

(defn packet [raw-packet]
  (let [length (packet-header (drop 1 raw-packet))
        adjusted-packet (take length (drop (inc packet-header-size) raw-packet))]
    {:header (packet-header adjusted-packet)
     :body (packet-body (drop packet-header-size adjusted-packet))}))

(defn packets [raw-packet]
  (loop [p raw-packet r []]
    (if-not (empty? p)
      (let [packet-seg (packet p)]
        (recur (drop (packet-length packet-seg) p)
               (conj r packet-seg)))
      r)))

(defn packet-values [packet]
  (loop [p packet
         v []]
    (if (empty? p)
      v
      (let [length (encoding/vl64-length p)]
        (recur (drop length p)
               (conj v (encoding/decode-vl64 (clojure.string/join (take length p)))))))))

(defn packet-values-b64 [packet]
  (map encoding/decode-b64 (partition 2 packet)))

(defn point [packet]
  (map #(Integer/parseInt %) (clojure.string/split packet #"\s")))

(defn encode-packet [p]
  (str (encoding/encode-b64 (:header p))
       (:body p)
       (char 1)))
