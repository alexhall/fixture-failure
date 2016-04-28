(ns fixture-failure.other-test
  (:require [clojure.test :refer :all]))

(deftest test-a
  (testing "I pass"
    (is (= 1 1))))

(deftest test-b
  (testing "I fail"
    (is (= 1 0))))

(deftest test-c
  (testing "I error"
    (throw (Exception. "Gotcha!"))))
