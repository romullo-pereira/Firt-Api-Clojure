(ns restful-example.core-test
  (:require [clojure.test :refer [testing deftest is]]
            [matcher-combinators.test :refer [match?]]
            [restful-example.core :as core]
            [io.pedestal.http :as http]
            [io.pedestal.test :as http-test]
            [cheshire.core :as json]))

(defn make-request! [verb path & args]
  (let [service-fn (::http/service-fn (core/create-server))
        response (apply http-test/response-for service-fn verb path args)]
     (update response :body json/decode true)))
    

(deftest crud-users
  (testing "listening users"
    (reset! core/users [])
    (is (match? {:body [] :status 200} (make-request! :get "/users"))))
  
  (testing "creating a user with wrong format for age"
    (is (match? {:body [] :status 200}
                (make-request! :post "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:name "New user" :age "20"})))))
  
  (testing "creating a user"
    (is (match? {:body {:name "New user" :age 20} :status 201}
                (make-request! :post "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:name "New user" :age 20})))))
  
  (testing "listing users after adding one"
    (is (match? {:body [{:name "New user" :age 20}] :status 200}
                (make-request! :get "/users"))))
  
  (testing "updating a user that doesn't exist"
    (is (match? {:body [] :status 200}
                (make-request! :put "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:pos 7 :name "User" :age 18})))))
  
  (testing "updating a user with wrong format for age"
    (is (match? {:body [] :status 200}
                (make-request! :put "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:pos 0 :name "User" :age "18"})))))
  
  (testing "updating a user"
    (is (match? {:body {:name "User" :age 18} :status 200}
                (make-request! :put "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:pos 0 :name "User" :age 18})))))
  
  (testing "deleting a user that doesn't exist"
    (is (match? {:body [] :status 200}
                (make-request! :delete "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:pos 5})))))
  
  (testing "deleting a user"
    (is (match? {:body [] :status 200}
                (make-request! :delete "/users"
                               :headers {"Content-Type" "application/json"}
                               :body (json/encode {:pos 0}))))))


