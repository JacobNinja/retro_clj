(ns retro.reactors
  (:require [retro.protocol :as protocol]
            [retro.db :as db]))

(defn login [packet conn]
  (let [[username password] (protocol/packet-body packet)]
    (db/fetch-user username password conn)))

(defn navigate [packet conn]
  (let [[hide-full? category-id _] (protocol/packet-values packet)]
    (db/fetch-category category-id conn)))
