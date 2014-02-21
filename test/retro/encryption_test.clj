(ns retro.encryption-test
  (:require [clojure.test :refer :all]
            [retro.encryption :refer :all]))

(deftest rc4-test
  (testing "public key decode"
    (is (= 535360
           (decode-key "55wfe030o2b17933arq9512j5u111105ckp230c81rp3m61ew9er3y0d523")))))
