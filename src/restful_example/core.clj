(ns restful-example.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]))

(defn respond-hello [request]
  {:status 200 :body "Hello, Pedestal"})

(def users (atom [{:name "Romullo" :age 20}
                  {:name "Astolfo" :age 22}
                  {:name "Jeremias" :age 26}
                  {:name "Geraldo" :age 27}
                  {:name "Silvio Luiz" :age 28}]))

(defn filter-users [params users]
  (filter (fn [user] (= params
                        (select-keys user (keys params))))
          users))

(def teste (atom nil))

(defn get-users-handler [request]
  (-> (reset! teste request)
      (:params {})
      (filter-users @users)
      (http/json-response)))

(defn post-users-handler [request]
  (let [new-user (:json-params request)]
    (if (int? (:age new-user))
      (do
        (swap! users conj new-user)
        (-> new-user
            http/json-response
            (assoc :status 201)))
      (http/json-response []))))

(defn vec-remove [pos coll]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))


(defn delete-user-handler [request]
  (let [pos (:json-params request)]
    (if (>= (dec (count @users)) (:pos pos))
      (do (->> @users
               (vec-remove (:pos pos))
               (reset! users))
          (http/json-response @users))
      (http/json-response []))))


(defn put-user-handler [request]
  (let [{:keys [pos name age]} (:json-params request)]
    (if  (and (int? age) (>= (dec (count @users)) pos))
      (do
        (swap! users assoc pos {:name name :age age})
        (http/json-response (get @users pos)))
      (http/json-response []))))



(def routes
  (route/expand-routes
   #{["/greet" :get respond-hello :route-name :greet]
     ["/users" :get get-users-handler :route-name :users ]
     ["/users" :post post-users-handler :route-name :create-user]
     ["/users" :delete delete-user-handler :route-name :delete-user]
     ["/users" :put put-user-handler :route-name :put-user]}))

(def pedestal-config
  (-> {::http/routes routes
   ::http/type  :jetty
   ::http/join? false
   ::http/port  3000}
    http/default-interceptors
    (update ::http/interceptors conj (body-params/body-params))))

(defn create-server []
  (http/create-server pedestal-config))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


