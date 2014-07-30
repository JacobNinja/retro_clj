(ns retro.protocol
  (:require [retro.encoding :as encoding])
  (:refer-clojure :exclude [partition-by]))

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

(defn packet-body [body]
  (map #(apply str %)
       (partition-by packet-header packet-header-size body)))

(defn- packet-length [p]
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
  (map (comp encoding/decode-vl64 str)
       packet))
