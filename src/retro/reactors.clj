(ns retro.reactors
  (:require [retro.protocol :as protocol]
            [retro.db :as db]
            [retro.path-finding :as p]
            [clojure.string :refer [split]]))

(defn default [p env])

(defn login [packet {:keys [db]}]
  (let [[username password] (protocol/packet-body packet)]
    {:user (db/fetch-user username password db)}))

(defn navigate [packet {:keys [db]}]
  (let [[hide-full? category-id _] (protocol/packet-values packet)]
    {:category (db/fetch-category category-id db)}))

(defn user-flat-cats [packet {:keys [db]}]
  {:user-categories (db/fetch-user-categories db)})

(defn- with-model [room models]
  (assoc room :model (get models (:model room))))

(defn- room-with-model [room-id db models]
  (let [room (db/fetch-room (Integer/parseInt room-id) db)]
    (with-model room models)))

(defn room-info [room-id {:keys [db room-models]}]
  {:room (room-with-model room-id db room-models)})

(defn goto-flat [room-id {:keys [db room-states room-models user]}]
  (let [room (room-with-model room-id db room-models)
        model (:model room)
        user-loc (merge (select-keys model [:x :y :z]) {:head 2 :body 2 :room-id 0})]
    (swap! room-states update-in [(:id room) :users] assoc (:username user) user-loc)
    {:room room}))

(defn look-to [packet {:keys [room-states user room]}]
  (let [[body head] (protocol/point packet)]
    (swap! room-states update-in [(:id room) :users (:username user)] merge {:body body :head head})))

(defn move-to [packet {:keys [user room room-states]}]
  (let [[x y] (protocol/packet-values-b64 packet)
        user-state (get-in @room-states [(:id room) :users (:username user)])
        current (map user-state [:x :y])]
    (swap! room-states update-in [(:id room) :users (:username user)] merge {:x x :y y})
    {:path (map (fn [[x y]]
                  {:x x :y y})
                (p/find-path current [x y]))}))

(defn search-flats [search-term {:keys [db room-models]}]
  {:rooms (map #(with-model % room-models)
               (db/search-rooms search-term db))})
