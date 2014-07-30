(ns retro.reactor-test
  (:require [clojure.test :refer :all]
            [retro.reactors :refer :all]
            [retro.db :as db]
            [retro.records :refer :all]
            [datomic.api :as d]))

(def test-db-url "datomic:mem://test")
(def conn (db/ensure-db test-db-url))
(def base-db (d/db conn))

(def user (map->User {:username "test"
                      :password "123"}))

(deftest login-test
  (testing "login"
    (let [db (d/with base-db [{:db/id (d/tempid :db.part/user)
                               :user/username (:username user)
                               :user/password (:password user)}])]
      (is (= (login "@Dtest@C123" (:db-after db)) user))
      (is (= (login "@Dtest@C321" (:db-after db)) nil)))))

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
      (is (= (navigate "HJI" (:db-after db))
             (map->Category {:name "Category name"
                             :type 0
                             :rooms []
                             :subcategories [(map->Category {:name "Subcategory name"
                                                             :type 0
                                                             :rooms []
                                                             :subcategories []})]})))))

  (testing "subcategory with rooms"
    (let [subcategory-id (d/tempid :db.part/user)
          owner-id (d/tempid :db.part/user)
          db (d/with base-db [{:db/id subcategory-id
                               :category/id 2
                               :category/type 0
                               :category/name "Subcategory name"}
                              {:db/id owner-id
                               :user/username (:username user)
                               :user/password (:password user)}
                              {:db/id (d/tempid :db.part/user)
                               :room/name "Room name"
                               :room/description "description"
                               :room/owner owner-id
                               :room/category subcategory-id
                               :room/type :room-type
                               :room/wallpaper "xxx"
                               :room/floor "yyy"}])]
      (is (= (navigate "HJI" (:db-after db))
             (map->Category {:name "Subcategory name"
                             :type 0
                             :subcategories []
                             :rooms [(map->Room {:name "Room name"
                                                 :description "description"
                                                 :owner user
                                                 :type :room-type
                                                 :wallpaper "xxx"
                                                 :floor "yyy"})]}))))))
