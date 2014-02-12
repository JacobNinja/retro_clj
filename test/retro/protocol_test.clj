(ns retro.protocol-test
  (:require [clojure.test :refer :all]
            [retro.protocol :refer :all]))

(deftest protocol-test
  (testing "single parameter"
    (is (= {:header 2 :body ["test"]}
           (packet "@@H@B@Dtest"))))
  (testing "two parameters"
    (is (= ["test" "foo"]
           (:body (packet "@@M@B@Dtest@Cfoo")))))
  (testing "two packets"
    (is (= [{:header 1 :body ["test"]}
            {:header 2 :body ["bar"]}]
           (packets "@@H@A@Dtest@@G@B@Cbar")))))
