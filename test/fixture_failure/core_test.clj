(ns fixture-failure.core-test
  (:require [clojure.test :refer :all]
            [fixture-failure.core :refer :all]))

(defn future-f
  "This makes lein test hang for 60 seconds after the fixture exception."
  [f]
  (println
   (deref (future
            (println "This is a future!")
            "Have a nice day.")))
  (f))

(defn agent-f
  "This makes lein test hang forever after the fixture exception."
  [f]
  (let [a (agent nil)
        p (promise)]
    (send a #(do
               (println "This is an agent!")
               (deliver p "Here you go.")
               %))
    (println (deref p 10000 ::timeout)))
  (f))

(defn error-f
  "Flip tables, peace out."
  [& _]
  (println "(╯°□°）╯︵ ┻━┻")
  (throw (Exception. "Nope!")))

(use-fixtures :once
  agent-f
  future-f
  error-f)

(deftest a-test
  (testing "I pass."
    (is (= 1 1))))
