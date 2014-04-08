(ns retro.encoding)

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
