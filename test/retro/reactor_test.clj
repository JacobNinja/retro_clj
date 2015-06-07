(ns retro.reactor-test
  (:require [clojure.test :refer :all]
            [retro.reactors :refer :all]
            [retro.db :as db]
            [retro.records :refer :all]
            [datomic.api :as d]))

(def test-db-url "datomic:mem://test")
(def conn (atom (db/ensure-db test-db-url)))
(def base-db (d/db @conn))

(defn database-fixture [f]
  (reset! conn (db/ensure-db test-db-url))
  (f)
  (d/delete-database test-db-url))

(use-fixtures :each database-fixture)

(def test-user (map->User {:username "test"
                           :credits 1
                           :film 0
                           :mail 0
                           :tickets 0}))

(def test-room-model (map->RoomModel {:x 0 :y 1 :z 2}))
(def test-room-models {"model" test-room-model})

(def test-room (map->Room {:id 1
                           :name "Roomie"
                           :description "desc"
                           :owner test-user
                           :current 0
                           :capacity 25
                           :model test-room-model
                           :wallpaper "xxx"
                           :floor "yyy"
                           :status "open"}))

(def test-sprite {:width 5 :length 6 :col "0,0,0" :sprite "foo"})

(defn- test-user-tx [tempid]
  {:db/id tempid
   :user/username (:username test-user)
   :user/credits (:credits test-user)
   :user/password "123"})

(defn test-room-tx
  ([] (test-room-tx (d/tempid :db.part/user)))
  ([room-id]
   (let [owner-id (d/tempid :db.part/user)]
     [(test-user-tx owner-id)
      {:db/id room-id
       :room/id (:id test-room)
       :room/name (:name test-room)
       :room/description (:description test-room)
       :room/owner owner-id
       :room/model "model"
       :room/floor (:floor test-room)
       :room/wallpaper (:wallpaper test-room)}])))

(deftest login-test
  (testing "login"
    (let [db (:db-after (d/with base-db [(test-user-tx (d/tempid :db.part/user))]))]
      (let [result (login "@Dtest@C123" {:db db})]
        (is (= @(:user result)
               test-user)))
      (is (nil? (login "@Dtest@C321" {:db db}))))))

(deftest navigate-test
  (testing "category with subcategories"
    (let [category-id (d/tempid :db.part/user)
          subcategory-id (d/tempid :db.part/user)
          db (d/with base-db [{:db/id category-id
                               :category/id 2
                               :category/type 0
                               :category/name "Category name"}
                              {:db/id subcategory-id
                               :category/id 5
                               :category/type 0
                               :category/name "Subcategory name"
                               :category/parent category-id}])]
      (is (= (navigate "HJI" {:db (:db-after db)})
             {:category (map->Category {:name "Category name"
                                        :type 0
                                        :id 2
                                        :capacity 100
                                        :current 0
                                        :parent-id 0
                                        :rooms []
                                        :subcategories [(map->Category {:name "Subcategory name"
                                                                        :type 0
                                                                        :id 5
                                                                        :capacity 100
                                                                        :current 0
                                                                        :parent-id 2
                                                                        :rooms []
                                                                        :subcategories []})]})}))))

  (testing "subcategory with rooms"
    (let [subcategory-id (d/tempid :db.part/user)
          owner-id (d/tempid :db.part/user)
          db (d/with base-db [{:db/id subcategory-id
                               :category/id 2
                               :category/type 0
                               :category/name "Subcategory name"}
                              (test-user-tx owner-id)
                              {:db/id (d/tempid :db.part/user)
                               :room/name "Room name"
                               :room/description "description"
                               :room/id 1
                               :room/owner owner-id
                               :room/category subcategory-id
                               :room/wallpaper "xxx"
                               :room/floor "yyy"}])]
      (is (= (navigate "HJI" {:db (:db-after db)})
             {:category (map->Category {:name "Subcategory name"
                                        :type 0
                                        :current 0
                                        :capacity 100
                                        :parent-id 0
                                        :id 2
                                        :subcategories []
                                        :rooms [(map->Room {:name "Room name"
                                                            :description "description"
                                                            :owner test-user
                                                            :wallpaper "xxx"
                                                            :floor "yyy"
                                                            :status "open"
                                                            :model nil
                                                            :current 0
                                                            :capacity 25
                                                            :id 1})]})})))))

(deftest user-flat-cats-test
  (testing "categories"
    (let [category-id (d/tempid :db.part/user)
          db (d/with base-db [{:db/id category-id
                               :category/name "Parent category"
                               :category/type 2}
                              {:db/id (d/tempid :db.part/user)
                               :category/type 2
                               :category/name "Category name"
                               :category/id 10
                               :category/parent category-id}])]
      (is (= (user-flat-cats "" {:db (:db-after db)})
             {:user-categories [(map->Category {:name "Category name"
                                                :id 10})]})))))

(deftest room-info-test
  (testing "room info"
    (let [room-model (map->RoomModel {})
          owner-id (d/tempid :db.part/user)
          db (d/with base-db (test-room-tx))]
      (is (= (room-info "1" {:db (:db-after db)
                             :room-models {"model" room-model}})
             {:room (map->Room {:id 1
                                :name "Roomie"
                                :description "desc"
                                :owner test-user
                                :current 0
                                :capacity 25
                                :model room-model
                                :wallpaper "xxx"
                                :floor "yyy"
                                :status "open"})})))))

(deftest goto-flat-test
  (testing "empty flat new state"
    (let [db (d/with base-db (test-room-tx))
          room-states (atom {})
          results (goto-flat "1" {:user (atom test-user)
                                  :db (:db-after db)
                                  :room-states room-states
                                  :room-models test-room-models})]
      (is (= (select-keys @(get-in @room-states [1 :users "test"])
                          [:x :y :z :body :head :room])
             {:x 0 :y 1 :z 2 :body 2 :head 2 :room 1}))
      (is (= (get-in results [:room :id]) 1)))))

(deftest look-to-test
  (testing "look to"
    (let [db (d/with base-db (test-room-tx))
          user-state (atom {:x 0 :y 0 :z 0 :body 1 :head 1})]
      (is (nil? (look-to "5 6" {:user user-state})))
      (is (= @user-state
             {:x 0 :y 0 :z 0 :body 5 :head 6})))))


(deftest move-to-test
  (testing "move to"
    (let [user-state (atom (map->User {:x 1 :y 1 :room 1}))]
      (is (= (move-to "@A@B" {:user user-state
                              :db base-db})
             {:path [{:x 1 :y 2 :body 4 :head 4}]})))))

(deftest search-flats-test
  (testing "search by username"
    (let [db (d/with base-db (test-room-tx))]
      (is (= (search-flats "test" {:db (:db-after db)
                                   :room-models test-room-models})
             {:rooms [test-room]})))))

(deftest objects-test
  (testing "objects in flat"
    (let [room-id (d/tempid :db.part/user)
          db (d/with base-db (concat (test-room-tx room-id)
                                     [{:db/id (d/tempid :db.part/user)
                                       :floor-item/id 1
                                       :floor-item/x 1
                                       :floor-item/y 2
                                       :floor-item/z 3
                                       :floor-item/rotation 4
                                       :floor-item/var "sign"
                                       :floor-item/sprite "foo"
                                       :floor-item/room room-id}
                                      {:db/id (d/tempid :db.part/user)
                                       :floor-item/id 2
                                       :floor-item/x 5
                                       :floor-item/y 6
                                       :floor-item/z 1
                                       :floor-item/rotation 2
                                       :floor-item/var "sign"
                                       :floor-item/sprite "bar"
                                       :floor-item/room room-id}
                                      {:db/id (d/tempid :db.part/user)
                                       :floor-item/sprite "bar"}]))]
      (is (= (objects "" {:db (:db-after db)
                          :user (atom {:room 1})
                          :sprites {"foo" {:width 1 :length 1}}})
             {:floor-items [(map->FloorItem {:id 1
                                             :x 1
                                             :y 2
                                             :z 3
                                             :rotation 4
                                             :column "0,0,0"
                                             :var "-"
                                             :teleport-id 456
                                             :sprite "foo"})
                            (map->FloorItem {:id 2
                                             :x 5
                                             :y 6
                                             :z 1
                                             :rotation 2
                                             :column "0,0,0"
                                             :var "-"
                                             :teleport-id 456
                                             :sprite "bar"})]})))))

(deftest move-object-test
  (testing "move object to x y"
    (let [validate-fn (fn [& args] true)
          room-id (d/tempid :db.part/user)
          db (:db-after @(datomic.api/transact @conn (concat (test-room-tx room-id)
                                                            [{:db/id (d/tempid :db.part/user)
                                                              :floor-item/id 123
                                                              :floor-item/x 1
                                                              :floor-item/y 2
                                                              :floor-item/z 3
                                                              :floor-item/rotation 0
                                                              :floor-item/sprite "foo"
                                                              :floor-item/room room-id}])))]
      (is (= (select-keys (:move-object (move-object validate-fn
                                                     "123 2 3 4"
                                                     {:db db
                                                      :conn @conn
                                                      :room {:id 1}}))
                          [:id :x :y :z :rotation])
             {:id 123 :x 2 :y 3 :z 3 :rotation 4})))))

(deftest go-away-test
  (testing "user is removed from room"
    (let [user (atom (map->User {:username "foo" :room 1}))
          room-states (atom {1 {:users {"foo" user}}})]
      (is (nil? (go-away "" {:room-states room-states
                             :user user})))
      (is (nil? (get-in @room-states [1 :users "foo"]))))))

(deftest pick-up-test
  (testing "pick up floor item"
    (let [room-id (d/tempid :db.part/user)
          floor-item-temp-id (d/tempid :db.part/user)
          tempids (:tempids @(datomic.api/transact @conn (concat (test-room-tx room-id)
                                                                 [{:db/id floor-item-temp-id
                                                                   :floor-item/id 123
                                                                   :floor-item/room room-id}])))
          floor-item-id (d/resolve-tempid (d/db @conn) tempids floor-item-temp-id)]
      (is (= (select-keys (:pick-up (pick-up "new stuff 123" {:conn @conn}))
                          [:id])
             {:id 123}))
      (is (nil? (:floor-item/room (d/entity (d/db @conn) floor-item-id)))))))

(deftest hand-test
  (testing "floor items in hand"
    (let [room (d/tempid :db.part/user)
          owner (d/tempid :db.part/user)
          floor-item (d/tempid :db.part/user)
          floor-item-in-room (d/tempid :db.part/user)
          floor-item-no-owner (d/tempid :db.part/user)
          db (:db-after (d/with base-db [{:db/id owner
                                          :user/username "foo"
                                          :user/items [floor-item floor-item-in-room]}
                                         {:db/id room
                                          :room/id 1}
                                         {:db/id floor-item
                                          :floor-item/id 1}
                                         {:db/id floor-item-no-owner
                                          :floor-item/id 3}
                                         {:db/id floor-item-in-room
                                          :floor-item/id 2
                                          :floor-item/room room}
                                         ]))
          result (hand "" {:user (atom (map->User {:username "foo"}))
                           :db db
                           :sprites {}})]
      (is (= 1
             (count (:objects result))))
      (is (= (:id (first (:objects result)))
             1)))))

(deftest place-stuff-test
  (testing "place floor item"
    (let [user-id (d/tempid :db.part/user)
          floor-item-id (d/tempid :db.part/user)
          room-id (d/tempid :db.part/user)
          tx @(d/transact @conn [{:db/id user-id
                                 :user/username (:username test-user)
                                 :user/items floor-item-id}
                                {:db/id room-id
                                 :room/id 1}
                                {:db/id floor-item-id
                                 :floor-item/sprite (:sprite test-sprite)
                                 :floor-item/id 123}])
          floor-item-id (d/resolve-tempid (d/db @conn) (:tempids tx) floor-item-id)
          room-id (d/resolve-tempid (d/db @conn) (:tempids tx) room-id)
          result (place-stuff "123 1 2 3 4 5" {:conn @conn
                                               :db (:db-after tx)
                                               :sprites {"foo" test-sprite}
                                               :user (atom (assoc test-user :room 1))})]
      (is (= (select-keys (:place result)
                          [:id :x :y :sprite :rotation])
             {:id 123 :x 1 :y 2 :sprite test-sprite :rotation 5}))
      (is (= (:db/id (:floor-item/room (d/entity (d/db @conn) floor-item-id)))
             room-id)))))

(deftest catalog-page-info-test
  (testing "page info"
    (let [page (map->CatalogPage {:name "cat_mode"})]
      (is (= (catalog-page-info "production/foo/en" {:pages {"foo" page}})
             {:page page})))))

(deftest catalog-purchase-test
  (testing "purchase item"
    (let [user-id (d/tempid :db.part/user)
          tx @(d/transact @conn [(test-user-tx user-id)
                                 {:db/id (d/tempid :db.part/user)
                                  :floor-item/id 1}])
          result (catalog-purchase (str "production" \return
                                        "category" \return
                                        "en" \return
                                        "purchase_foo" \return
                                        "-" \return
                                        "0" \return
                                        \return \return)
                                   {:conn @conn
                                    :user (atom test-user)
                                    :pages {"category" (map->CatalogPage {:items [{:purchase-code "purchase_foo"
                                                                                   :furni {:sprite "foo"}}]})}})]
      (is (nil? result))
      (let [items (:user/items (d/entity (d/db @conn)
                                         (d/resolve-tempid (:db-after tx)
                                                           (:tempids tx)
                                                           user-id)))
            entity (d/entity (d/db @conn)
                             (:db/id (first items)))]
        (is (= (:floor-item/sprite entity)
               "foo"))
        (is (= (:floor-item/id entity)
               2))))))
