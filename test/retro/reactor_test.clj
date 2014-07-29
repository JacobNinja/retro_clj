(ns retro.reactor-test
  (:require [clojure.test :refer :all]
            [retro.reactors :refer :all]
            [retro.handlers :refer [map->User]]
            [retro.db :as db]
            [datomic.api :as d]))

(deftest reactors-test
  (testing "login"
    (let [conn (db/ensure-db "datomic:mem://retro")]
      (d/transact conn [{:db/id (d/tempid :db.part/user)
                         :user/username "test"
                         :user/password "123"}])

      (is (= (login "@Dtest@C123" conn)
             (map->User {:username "test" :password "123"})))
      (is (= (login "@Dtest@C321" conn) nil)))))
