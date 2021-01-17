(ns robinhood.clj.test.auth-test
  (:use [robinhood.clj.auth])
  (:use [clojure.test]))

(deftest test-authed-login
  (let [{:keys [robinhood/access-token]} (authed-client)]
    (is (not (nil? access-token)))))

#_(deftest test-unauthed-login
  (let [{:keys [robinhood/access-token]} (unauthed-client)]
    (is (not (nil? access-token)))))
