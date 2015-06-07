(ns retro.path-finding)

(defn take-through
  [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if (pred (first s))
       (list (first s))
       (cons (first s) (take-through pred (rest s)))))))

(defn- g [{:keys [x y]} next-point]
  (if (or (= x (:x next-point))
          (= y (:y next-point)))
    10
    14))

(defn- heuristic [{:keys [x y]} end-point]
  (let [end-x (:x end-point)
        end-y (:y end-point)
        x-diff (Math/abs (- x end-x))
        y-diff (Math/abs (- y end-y))]
    (if (> x-diff y-diff)
      (+ (* 14 y-diff) (* 10 (- x-diff y-diff)))
      (+ (* 14 x-diff) (* 10 (- y-diff x-diff))))))

(defn- blocked? [obstacles {:keys [x y]}]
  (some #(and (= x (:x %)) (= y (:y %)))
        obstacles))

(defn- find-segment [end-point obstacles start-point]
  (let [{:keys [x y]} start-point
        diagonal [{:x (inc x) :y (inc y) :body 3 :head 3}
                  {:x (inc x) :y (dec y) :body 1 :head 1}
                  {:x (dec x) :y (inc y) :body 5 :head 5}
                  {:x (dec x) :y (dec y) :body 7 :head 7}]
        cardinal [{:x (inc x) :y y :body 2 :head 2}
                  {:x (dec x) :y y :body 6 :head 6}
                  {:x x :y (inc y) :body 4 :head 4}
                  {:x x :y (dec y) :body 0 :head 0}]
        unblocked (filter (partial (complement blocked?) obstacles)
                          (concat diagonal cardinal))]
    (apply (partial min-key (fn [point]
                              (+ (g start-point point)
                                 (heuristic point end-point))))
           unblocked)))

(defn find-path
  ([start end] (find-path start end []))
  ([[x y] [end-x end-y] obstacles]
   (when-not (some (fn [obstacle] (= (select-keys obstacle [:x :y]) {:x end-x :y end-y})) obstacles)
     (let [end-point {:x end-x :y end-y}]
       (rest (take-through #(or (= end-point (select-keys % [:x :y])) (empty? %))
                           (iterate (partial find-segment end-point obstacles)
                                    {:x x :y y})))))))
