(ns retro.encoding)

(defn- zip [a b]
  (partition 2 (interleave a b)))

(defn- negative? [s]
  (= 4 (bit-and (int (first s)) 4)))

(defn- total-bytes [b]
  (bit-and (bit-shift-right b 3) 7))

(defn- shift-amount [i]
  (+ 2 (* 6 i)))

(defn- base-64 [idx chr]
  (let [n (- (int chr) 64)
        base (Math/pow 64 idx)]
    (* n (if (zero? base) 1 base))))

(defn decode-b64 [bits]
  (int (reduce + (map-indexed base-64 (reverse bits)))))

(defn encode-b64 [num]
  (apply str
         (map #(char (+ 64 %))
              [(bit-and (int (/ num 64)) 63) (mod num 64)])))

(defn encode-vl64 [i]
  (loop [bytes '()
         next-i (bit-shift-right (Math/abs i) 2)]
    (if (= next-i 0)
      (apply str (map char
                      (cons (bit-or (+ 64 (bit-and (Math/abs i) 3))
                                    (bit-shift-left (inc (count bytes)) 3)
                                    (if (>= i 0) 0 4))
                            bytes)))
      (recur (cons (+ 64 (bit-and next-i 0x3f))
                   bytes)
             (bit-shift-right next-i 6)))))

(defn- decode-vl64* [bytes]
  (reduce (fn [_ [b shift]]
            (bit-shift-left (bit-and b 0x3f) shift))
          (bit-and (first bytes) 3)
          (zip (rest bytes)
               (map shift-amount
                    (range (total-bytes (first bytes)))))))

(defn decode-vl64 [s]
  (let [result (decode-vl64* (map int s))]
    (if (negative? s)
      (* result -1)
      result)))
