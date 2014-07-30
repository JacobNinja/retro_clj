(ns retro.reactor-test
  (:require [clojure.test :refer :all]
            [retro.reactors :refer :all]
            [retro.db :as db]
            [retro.records :refer :all]
            [datomic.api :as d]))

(def test-db-url "datomic:mem://test")

(deftest login-test
  (testing "login"
    (let [conn (db/ensure-db test-db-url)]
      (d/transact conn [{:db/id (d/tempid :db.part/user)
                         :user/username "test"
                         :user/password "123"}])

      (is (= (login "@Dtest@C123" conn)
             (map->User {:username "test" :password "123"})))
      (is (= (login "@Dtest@C321" conn) nil)))))

(deftest navigate-test
  (testing "category with subcategories"
    (let [conn (db/ensure-db test-db-url)
          category-id (d/tempid :db.part/user)
          category @(d/transact conn [{:db/id category-id
                                       :category/id 2
                                       :category/type 0
                                       :category/name "Category name"}])]
      @(d/transact conn [{:db/id (d/tempid :db.part/user)
                          :category/id 5
                          :category/type 0
                          :category/name "Subcategory name"
                          :category/parent (d/resolve-tempid (d/db conn) (:tempids category) category-id)}])
      (is (= (navigate "HJI" conn)
             (map->Category {:name "Category name"
                             :type 0
                             :subcategories [(map->Category {:name "Subcategory name"
                                                             :type 0
                                                             :subcategories []})]}))))))
