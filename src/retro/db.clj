(ns retro.db
  (:require [datomic.api :as d]
            [retro.handlers :refer [map->User]]))

(def schema
  [
   ; User
   {:db/id #db/id[:db.part/db]
    :db/ident :tx/user
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Tx associated with user"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "User name"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "User password"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :user/figure
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "User figure"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :user/mission
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "User mission"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :user/sex
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "User sex"
    :db.install/_attribute :db.part/db}
   ])

(defn ensure-schema [conn]
  (or (-> conn d/db (d/entid :tx/user))
      @(d/transact conn schema)))

(defn ensure-db [db-uri]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)]
    (ensure-schema conn)
    conn))

(defn- db->User [user-attrs]
  (map->User {:username (:user/username user-attrs)
              :password (:user/password user-attrs)}))

(defn fetch-user [username password conn]
  (when-let [user (ffirst (datomic.api/q '[:find ?user
                                           :in $ ?username ?password
                                           :where [?user :user/username ?username]
                                                  [?user :user/password ?password]]
                                         (d/db conn)
                                         username
                                         password))]
    (db->User (d/entity (d/db conn) user))))
