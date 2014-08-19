(ns retro.reactor-test
  (:require [clojure.test :refer :all]
            [retro.reactors :refer :all]
            [retro.db :as db]
            [retro.records :refer :all]
            [datomic.api :as d]))

(def test-db-url "datomic:mem://test")
(def conn (db/ensure-db test-db-url))
(def base-db (d/db conn))

(def test-user (map->User {:username "test"
                           :film 0
                           :mail 0
                           :tickets 0}))

(def test-room-model (map->RoomModel {:x 0 :y 1 :z 2}))

(def test-room (map->Room {:id 1
                           :name "Roomie"
                           :description "desc"
                           :owner (map->User {:username "owner" :film 0 :mail 0 :tickets 0})
                           :current 0
                           :capacity 25
                           :model test-room-model
                           :wallpaper 0
                           :floor 0
                           :status "open"}))
(defn test-room-tx []
  (let [owner-id (d/tempid :db.part/user)]
    [{:db/id owner-id
      :user/username "owner"}
     {:db/id (d/tempid :db.part/user)
      :room/id (:id test-room)
      :room/name (:name test-room)
      :room/description (:description test-room)
      :room/owner owner-id
      :room/model "model"}]))

(deftest login-test
  (testing "login"
    (let [db (d/with base-db [{:db/id (d/tempid :db.part/user)
                               :user/username (:username test-user)
                               :user/password "123"}])]
      (is (= (login "@Dtest@C123" {:db (:db-after db)}) {:user test-user}))
      (is (= (login "@Dtest@C321" {:db (:db-after db)}) {:user nil})))))

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
                              {:db/id owner-id
                               :user/username (:username test-user)}
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
          db (d/with base-db [{:db/id owner-id
                               :user/username "owner"}
                              {:db/id (d/tempid :db.part/user)
                               :room/id 1
                               :room/name "Roomie"
                               :room/description "desc"
                               :room/owner owner-id
                               :room/model "model"}])]
      (is (= (room-info "1" {:db (:db-after db)
                             :room-models {"model" room-model}})
             {:room (map->Room {:id 1
                                :name "Roomie"
                                :description "desc"
                                :owner (map->User {:username "owner" :film 0 :mail 0 :tickets 0})
                                :current 0
                                :capacity 25
                                :model room-model
                                :wallpaper 0
                                :floor 0
                                :status "open"})})))))

(deftest goto-flat-test
  (testing "empty flat new state"
    (let [db (d/with base-db (test-room-tx))
          room-states (atom {})
          results (goto-flat "1" {:user test-user
                                  :db (:db-after db)
                                  :room-states room-states
                                  :room-models {"model" test-room-model}})]
      (is (= @room-states
             {1 {:users {"test" {:x 0 :y 1 :z 2 :body 2 :head 2 :room-id 0}}}}))
      (is (= (get-in results [:room :id]) 1)))))

(deftest look-to-test
  (testing "look to"
    (let [db (d/with base-db (test-room-tx))
          room-states (atom {1 {:users {"test" {:x 0 :y 0 :z 0 :body 1 :head 1}}}})]
      (look-to "5 6" {:user test-user
                      :room test-room
                      :room-states room-states})
      (is (= @room-states
             {1 {:users {"test" {:x 0 :y 0 :z 0 :body 5 :head 6}}}})))))


(deftest move-to-test
  (testing "move to"
    (let [room-states (atom {})]
      (move-to "@E@F" {:user test-user
                      :room test-room
                      :room-states room-states})
      (is (= (select-keys (get-in @room-states [1 :users "test"]) [:x :y])
             {:x 5 :y 6})))))

