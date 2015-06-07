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
   {:db/id #db/id[:db.part/db]
    :db/ident :user/credits
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "User credits"
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

   ; Floor item
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/id
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "id"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/x
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "x"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/y
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "y"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/z
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "z"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/rotation
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "rotation"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/room
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "room"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/var
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "signed var"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/sprite
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "sprite"
    :db.install/_attribute :db.part/db}
   {:db/id #db/id[:db.part/db]
    :db/ident :floor-item/owner
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "owner"
    :db.install/_attribute :db.part/db}
   ])

(defn ensure-schema [conn]
  (or (-> conn d/db (d/entid :tx/floor-item))
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
              :credits (:user/credits user-attrs)
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

(defn db->Room [user entity]
  (map->Room {:name (:room/name entity)
              :description (:room/description entity)
              :id (:room/id entity)
              :owner user
              :status "open"
              :model (:room/model entity)
              :current 0
              :capacity 25
              :wallpaper (get entity :room/wallpaper 0)
              :floor (get entity :room/floor 0)}))

(defn- db->FloorItem [entity]
  (map->FloorItem {:id (:floor-item/id entity)
                   :x (:floor-item/x entity)
                   :y (:floor-item/y entity)
                   :z (:floor-item/z entity)
                   :column "0,0,0"
                   :rotation (:floor-item/rotation entity)
                   :sprite (:floor-item/sprite entity)
                   :teleport-id 456 ; TODO static value
                   :var "-"}))

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
  (when-let [[room user] (first (datomic.api/q '[:find ?room ?user
                                                 :in $ ?room-id
                                                 :where [?room :room/id ?room-id]
                                                        [?room :room/owner ?user]]
                                               db
                                               room-id))]
    (db->Room (db->User (d/entity db user))
              (d/entity db room))))

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
                    (map #(db->Room (db->User (:room/owner %)) %)
                         (map (partial d/entity db)
                              (fetch-rooms category-id db)))))))

(defn fetch-user-categories [db]
  (let [categories (map first (datomic.api/q '[:find ?category
                                               :in $ ?type
                                               :where [?category :category/type ?type]
                                                      [?category :category/parent _]]
                                             db
                                             2))]
    (map (comp db->UserCategory (partial d/entity db))
         categories)))

(defn search-rooms [search-term db]
  (let [[user & rooms] (first (datomic.api/q '[:find ?user ?rooms
                                               :in $ ?term
                                               :where [?user :user/username ?term]
                                                      [?rooms :room/owner ?user]]
                                             db
                                             search-term))]
    (when-let [user (db->User (d/entity db user))]
      (map (comp (partial db->Room user)
                 (partial d/entity db))
           rooms))))

(defn fetch-floor-items [db room-id]
  (let [floor-items (first (datomic.api/q '[:find ?item
                                            :in $ ?room-id
                                            :where [?room :room/id ?room-id]
                                                   [?item :floor-item/room ?room]]
                                          db
                                          room-id))]
    (map #(db->FloorItem (d/entity db %)) floor-items)))

(defn- transact-move-object [floor-item-id x y rotation conn]
  (datomic.api/transact conn
                        [{:db/id floor-item-id
                          :floor-item/x x
                          :floor-item/y y
                          :floor-item/rotation rotation}]))

(defn move-floor-item [floor-item-id x y rotation conn]
  (when-let [floor-item (ffirst (datomic.api/q '[:find ?item
                                                 :in $ ?item-id
                                                 :where [?item :floor-item/id ?item-id]]
                                               (d/db conn)
                                               floor-item-id))]

    (let [db (:db-after @(transact-move-object floor-item x y rotation conn))]
      (db->FloorItem (d/entity db floor-item)))))

(defn pick-up-floor-item [floor-item-id conn]
  (when-let [[floor-item room] (first (datomic.api/q '[:find ?item ?room
                                                       :in $ ?item-id
                                                       :where [_ :floor-item/room ?room]
                                                              [?item :floor-item/id ?item-id]]
                                                     (d/db conn)
                                                     floor-item-id))]
    @(datomic.api/transact conn
                            [[:db/retract floor-item
                              :floor-item/room room]])
    (map->FloorItem {:id floor-item-id})))

(defn fetch-hand-objects [username db]
  (let [hand-objects (map first (d/q '[:find ?objects
                                       :in $ ?username
                                       :where [?user :user/username ?username]
                                              [?objects :floor-item/owner ?user]
                                              (not [?objects :floor-item/room])]
                                     db
                                     username))]
    (map db->FloorItem (map (partial d/entity db) hand-objects))))

(defn place-floor-item [conn id x y room-id]
  (let [[floor-item room] (first (d/q '[:find ?floor-item ?room
                                        :in $ ?floor-item-id ?room-id
                                        :where [?room :room/id ?room-id]
                                               [?floor-item :floor-item/id ?floor-item-id]]
                                      (d/db conn)
                                      id
                                      room-id))
        tx @(d/transact conn
                        [{:db/id floor-item
                          :floor-item/x x
                          :floor-item/y y
                          :floor-item/room room}])]
    (db->FloorItem (d/entity (:db-after tx) floor-item))))
