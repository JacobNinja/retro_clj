(ns retro.encryption)

(defn- indexed [coll]
  (partition 2
             (interleave (iterate inc 0)
                         coll)))

(defn- offset-builder [table]
  (fn [c]
    (let [index (.indexOf table (str c))
          offset (cond-> index
                         (zero? (mod index 2)) (* 2)
                         (zero? (mod index 3)) (* 3))]
      (if (< offset 0)
        (mod (count table) 2)
        offset))))

(defn decode-key [key]
  (let [[table k] (split-at (dec (/ (count key) 2)) key)]
    (reduce
     (fn [acc [i offset]]
       (bit-xor (+ acc offset)
                (bit-shift-left offset
                                (* 8 (mod i 3)))))
     0
     (indexed (map (offset-builder (apply str table))
                   (butlast k))))))

