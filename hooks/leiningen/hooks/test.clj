(ns leiningen.hooks.test
  (:require [clojure.test :refer [test-ns do-report *report-counters*
                                  *initial-report-counters* *testing-vars*]]
            [leiningen.test :refer [form-for-testing-namespaces]]
            [robert.hooke :refer [add-hook]]))

(defn catch-fixture-errors [f & args]
  `(do
     (alter-var-root
      #'test-ns
      (fn [old#]
        (fn [ns#]
          (binding [*report-counters* (ref *initial-report-counters*)]
            (try
              (old# ns#)
              (catch Throwable t#
                (binding [*testing-vars*
                          (list (with-meta
                                  []
                                  {:name ns#
                                   :ns ns#}))]
                  (do-report {:type :error
                              :message "Uncaught exception in test fixture"
                              :expected nil
                              :actual t#}))
                (do-report {:type :end-test-ns, :ns (the-ns ns#)})
                ;; This won't include the in-progress summary from the
                ;; failed ns, but it's better than just going away forever.
                @*report-counters*))))))
     ~(apply f args)))

(defn activate []
  (when (System/getenv "CATCH_FIXTURE_ERRS")
    (add-hook #'form-for-testing-namespaces catch-fixture-errors)))
