(ns retro.path-finding-test
  (:require [clojure.test :refer :all]
            [retro.path-finding :refer :all]))

(deftest path-finding-test
  (testing "straight path"
    (is (= (find-path [1 1] [1 5])
           [[1 2] [1 3] [1 4] [1 5]]))
    (is (= (find-path [1 1] [5 1])
           [[2 1] [3 1] [4 1] [5 1]])))
  (testing "diagonal path"
    (is (= (find-path [1 1] [5 5])
           [[2 2] [3 3] [4 4] [5 5]]))
    (is (= (find-path [6 6] [2 2])))))
