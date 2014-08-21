(ns retro.path-finding)

(defn take-through
  [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if (pred (first s))
       (list (first s))
       (cons (first s) (take-through pred (rest s)))))))

(defn- g [[x y] [next-x next-y]]
  (if (or (= x next-x)
          (= y next-y))
    10
    14))

(defn- heuristic [[x y] [end-x end-y]]
  (let [x-diff (Math/abs (- x end-x))
        y-diff (Math/abs (- y end-y))]
    (if (> x-diff y-diff)
      (+ (* 14 y-diff) (* 10 (- x-diff y-diff)))
      (+ (* 14 x-diff) (* 10 (- y-diff x-diff))))))

(defn- find-segment [end-point start-point]
  (let [[x y] start-point
        diagonal [[(inc x) (inc y)]
                  [(inc x) (dec y)]
                  [(dec x) (inc y)]
                  [(dec x) (dec y)]]
        cardinal [[(inc x) y]
                  [(dec x) y]
                  [x (inc y)]
                  [x (dec y)]]]
    (apply (partial min-key (fn [point]
                              (+ (g start-point point)
                                 (heuristic point end-point))))
           (concat diagonal cardinal))))

(defn find-path [start-point end-point]
  (rest (take-through #(= end-point %)
                      (iterate (partial find-segment end-point)
                               start-point))))
