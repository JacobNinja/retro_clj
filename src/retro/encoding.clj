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
