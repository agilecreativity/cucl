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

(deftest keywordize-arguments-test
  (testing "simple parse-args example"
    (is (= (keywordize-arguments "aa" 1 "bb" 2) {:aa 1 :bb 2}))))

(deftest quote-fn-test
  (testing "quote-fn"
    (is (= true (function? (quote-fn "1234"))))
    (is (= true (function? (quote-fn " 1234 "))))
    (is (= false (function? (quote-fn "not-a-number"))))))

(deftest remove-ansi-test
  (are [result arg] (= result (remove-ansi arg))
    nil nil
    "" ""
    "simple text" "simple text"
    " this is red" "\033[31m this is red"
    " magenta background" "\033[45m magenta background"
    " bold" "\033[1m bold"
    " reset foreground" "\033[39m reset foreground"
    " reset background" "\033[49m reset background"
    " green foreground" "\033[32m green foreground"
    " reset + rgb color" "\033[0;38;2;99;88;77m reset + rgb color"))
