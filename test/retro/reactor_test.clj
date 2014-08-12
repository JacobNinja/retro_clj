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
                               :room/model "room-model"
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
                                                            :status :open
                                                            :model "room-model"
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
    (let [owner-id (d/tempid :db.part/user)
          db (d/with base-db [{:db/id owner-id
                               :user/username "owner"}
                              {:db/id (d/tempid :db.part/user)
                               :room/id 1
                               :room/name "Roomie"
                               :room/description "desc"
                               :room/owner owner-id
                               :room/model "model"}])]
      (is (= (room-info "1" {:db (:db-after db)})
             {:room (map->Room {:id 1
                                :name "Roomie"
                                :description "desc"
                                :owner (map->User {:username "owner" :film 0 :mail 0 :tickets 0})
                                :current 0
                                :capacity 25
                                :model "model"
                                :status :open})})))))
