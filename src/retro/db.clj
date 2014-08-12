(ns retro.db
  (:require [datomic.api :as d]
            [retro.records :refer :all]))

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

   ; Category
   {:db/id #db/id[:db.part/db]
    :db/ident :tx/category
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Tx associated with category"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :category/id
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Category id"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :category/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Category name"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :category/type
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Category type"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :category/parent
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Category parent"
    :db.install/_attribute :db.part/db}

   ; Room
   {:db/id #db/id[:db.part/db]
    :db/ident :tx/room
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Tx associated with room"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/id
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Room id"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Room name"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/description
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Room description"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/category
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Room category"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/owner
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Room owner"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/wallpaper
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Room wallpaper"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/floor
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Room floor"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :room/model
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Room model"
    :db.install/_attribute :db.part/db}
   ])

(defn ensure-schema [conn]
  (or (-> conn d/db (d/entid :tx/room))
      @(d/transact conn schema)))

(defn ensure-db [db-uri]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)]
    (ensure-schema conn)
    conn))

(defn- db->User [user-attrs]
  (map->User {:username (:user/username user-attrs)
              :figure (:user/figure user-attrs)
              :sex (:user/sex user-attrs)
              :mission (:user/mission user-attrs)
              :tickets 0
              :film 0
              :mail 0}))

(defn- db->Category
  ([parent entity]
   (db->Category parent entity [] []))
  ([parent entity subcategories rooms]
   (map->Category {:name (:category/name entity)
                   :type (:category/type entity)
                   :id (:category/id entity)
                   :capacity 100
                   :current 0
                   :parent-id (if parent
                                (:category/id parent)
                                0)
                   :subcategories (map (partial db->Category entity)
                                       subcategories)
                   :rooms rooms})))

(defn- db->UserCategory [entity]
  (map->Category {:name (:category/name entity)
                  :id (:category/id entity)}))

(defn db->Room [db entity]
  (map->Room {:name (:room/name entity)
              :description (:room/description entity)
              :id (:room/id entity)
              :owner (db->User (d/entity db (:db/id (:room/owner entity))))
              :status :open
              :model (:room/model entity)
              :current 0
              :capacity 25
              :wallpaper (:room/wallpaper entity)
              :floor (:room/floor entity)}))

(defn fetch-user [username password db]
  (when-let [user (ffirst (datomic.api/q '[:find ?user
                                           :in $ ?username ?password
                                           :where [?user :user/username ?username]
                                                  [?user :user/password ?password]]
                                         db
                                         username
                                         password))]
    (db->User (d/entity db user))))

(defn- fetch-subcategories [category db]
  (first (d/q '[:find ?subcategory
                :in $ ?category
                :where [?subcategory :category/parent ?category]]
              db
              category)))

(defn- fetch-rooms [category-id db]
  (map first (d/q '[:find ?rooms
                :in $ ?category-id
                :where [?rooms :room/category ?category-id]]
              db
              category-id)))

(defn fetch-room [room-id db]
  (when-let [room (ffirst (datomic.api/q '[:find ?room
                                           :in $ ?room-id
                                           :where [?room :room/id ?room-id]]
                                         db
                                         room-id))]
    (db->Room db (d/entity db room))))

(defn fetch-parent [db category]
  (when-let [parent (:category/parent category)]
    (d/entity db parent)))

(defn fetch-category [category-id db]
  (when-let [category-id (ffirst (datomic.api/q '[:find ?category
                                                  :in $ ?category-id
                                                  :where [?category :category/id ?category-id]]
                                                db
                                                category-id))]
    (let [category (d/entity db category-id)]
      (db->Category (fetch-parent db category)
                    category
                    (map (partial d/entity db)
                         (fetch-subcategories category-id db))
                    (map #(db->Room db (d/entity db %))
                         (fetch-rooms category-id db))))))

(defn fetch-user-categories [db]
  (let [categories (map first (datomic.api/q '[:find ?category
                                               :in $ ?type
                                               :where [?category :category/type ?type]
                                                      [?category :category/parent _]]
                                             db
                                             2))]
    (map (comp db->UserCategory (partial d/entity db))
         categories)))
