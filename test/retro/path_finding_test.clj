(ns retro.path-finding-test
  (:require [clojure.test :refer :all]
            [retro.path-finding :refer :all]))

(deftest path-finding-test
  (testing "straight path"
    (is (= (find-path [1 1] [1 5])
           [{:x 1 :y 2 :body 4 :head 4} {:x 1 :y 3 :body 4 :head 4} {:x 1 :y 4 :body 4 :head 4} {:x 1 :y 5 :body 4 :head 4}]))
    (is (= (find-path [1 5] [1 4])
           [{:x 1 :y 4 :body 0 :head 0}]))
    (is (= (find-path [1 1] [5 1])
           [{:x 2 :y 1 :body 2 :head 2} {:x 3 :y 1 :body 2 :head 2} {:x 4 :y 1 :body 2 :head 2} {:x 5 :y 1 :body 2 :head 2}]))
    (is (= (find-path [5 1] [4 1])
           [{:x 4 :y 1 :body 6 :head 6}])))
  (testing "diagonal path"
    (is (= (find-path [1 1] [5 5])
           [{:x 2 :y 2 :body 3 :head 3} {:x 3 :y 3 :body 3 :head 3} {:x 4 :y 4 :body 3 :head 3} {:x 5 :y 5 :body 3 :head 3}]))
    (is (= (find-path [6 6] [2 2])
           [{:x 5 :y 5 :body 7 :head 7} {:x 4 :y 4 :body 7 :head 7} {:x 3 :y 3 :body 7 :head 7} {:x 2 :y 2 :body 7 :head 7}]))
    (is (= (find-path [3 3] [2 4])
           [{:x 2 :y 4 :head 5 :body 5}]))
    (is (= (find-path [3 3] [4 2])
           [{:x 4 :y 2 :body 1 :head 1}]))))
