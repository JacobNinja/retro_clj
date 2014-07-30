(ns retro.reactors
  (:require [retro.protocol :as protocol]
            [retro.db :as db]))

(defn login [packet db]
  (let [[username password] (protocol/packet-body packet)]
    (db/fetch-user username password db)))

(defn navigate [packet db]
  (let [[hide-full? category-id _] (protocol/packet-values packet)]
    (db/fetch-category category-id db)))
