(ns cucl.core-utils-test
  (:require [clojure.test :refer :all]
            [cucl.core-utils :refer :all]))

(deftest map-keys-test
  (testing "apply function to each keys"
    (is (= (map-keys name {:a 1, :b 2}) {"a" 1 "b" 2}))))

(deftest map-vales-test
  (testing "apply function to each values"
    (is (= (map-vals inc {:a 1, :b 2}) {:a 2 :b 3}))))

(deftest expand-and-normalized-path-test
  (testing "expand the ~ to home directory."
    (is (= (System/getenv "HOME") (expand-and-normalized-path "~")))))
