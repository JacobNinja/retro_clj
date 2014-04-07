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
  (let [i (Math/abs i)
        initial-byte (+ 64 (bit-and i 3))
        negative-mask (if (>= i 0)
                        0
                        4)]
    (loop [bytes '()
           i (bit-shift-right i 2)]
      (if (= i 0)
        (apply str (map char
                        (cons (bit-or initial-byte
                                      (bit-shift-left (inc (count bytes)) 3)
                                      negative-mask)
                              bytes)))
        (recur (cons (+ 64 (bit-and i 0x3f))
                     bytes)
               (bit-shift-right i 6))))))
