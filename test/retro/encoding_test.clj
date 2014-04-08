(ns retro.encoding-test
  (:require [clojure.test :refer :all]
            [retro.encoding :refer :all]))

(deftest b64-encoding-test
  (testing "decode"
    (is (= 0 (decode-b64 "@@")))
    (is (= 1 (decode-b64 "@A")))
    (is (= 2 (decode-b64 "@B")))
    (is (= 390 (decode-b64 "FF")))
    (is (= 491 (decode-b64 "Gk")))
    (is (= 1313 (decode-b64 "Ta"))))

  (testing "encode"
    (is (= "@@" (encode-b64 0)))
    (is (= "@A" (encode-b64 1)))
    (is (= "Dl" (encode-b64 300)))
    (is (= "IX" (encode-b64 600)))
    (is (= "NH" (encode-b64 5000)))))

(deftest vl64-encoding
  (testing "encode"
    (is (= "H" (encode-vl64 0)))
    (is (= "I" (encode-vl64 1)))
    (is (= "J" (encode-vl64 2)))
    (is (= "PY" (encode-vl64 100)))
    (is (= "TY" (encode-vl64 -100)))
    ))
