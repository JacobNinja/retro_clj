(ns retro.reactors
  (:require [retro.protocol :as protocol]
            [retro.db :as db]))

(defn default [p env])

(defn login [packet {:keys [db]}]
  (let [[username password] (protocol/packet-body packet)]
    {:user (db/fetch-user username password db)}))

(defn navigate [packet {:keys [db]}]
  (let [[hide-full? category-id _] (protocol/packet-values packet)]
    {:category (db/fetch-category category-id db)}))

(defn user-flat-cats [packet {:keys [db]}]
  {:user-categories (db/fetch-user-categories db)})
