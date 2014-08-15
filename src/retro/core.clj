(ns retro.core
  (:require [retro.server-headers :as headers]
            [retro.reactors :as reactors]
            [retro.handlers :as handlers]
            [retro.protocol :as protocol]
            [retro.encoding :as encoding]
            [datomic.api :as d]
            [aleph.tcp :as tcp]
            [lamina.core :as l]
            [gloss.core :as g]))

;(def db-url "datomic:free://localhost:4334/retro")
(def db-url "datomic:mem://retro")

(def mapping
  {headers/login [reactors/login handlers/login]
   headers/user-details [reactors/default handlers/user-details]
   headers/club [reactors/default handlers/club-habbo]
   headers/badge [reactors/default handlers/badge]
   headers/generate-key [reactors/default handlers/generate-key]
   headers/user-flat-cats [reactors/user-flat-cats handlers/user-flat-cats]
   headers/room-info [reactors/room-info handlers/room-info]
   headers/navigate [reactors/navigate handlers/navigate]})

(defn send-packet [ch packet]
  (println (str "SEND: " packet))
  (l/enqueue ch (protocol/encode-packet packet)))

(defn- response-handler [env ch packets]
  (doseq [packet packets]
    (println (str "INCOMING: " packet))
    (when-let [[reactor handler] (mapping (encoding/decode-b64 (subs packet 0 2)))]
      (try
        (swap! env merge (reactor (subs packet 2) @env))
        (doseq [r (handler @env)]
          (send-packet ch r))
        (catch Exception e
          (.printStackTrace e)
          (println e))))))

(defn- client-handler [env ch info]
  (println "CONNECTED")
  (send-packet ch (first (handlers/greet)))
  (l/receive-all ch (partial response-handler (atom env) ch)))

(def frame
  (g/repeated (g/string :ascii
                        :prefix (g/prefix (g/string :ascii :length 3)
                                          #(encoding/decode-b64 (subs % 1))
                                          identity))
              :prefix :none))

(defn seed []
  (let [public-category (d/tempid :db.part/user)
        private-category (d/tempid :db.part/user)
        chill-category (d/tempid :db.part/user)
        user (d/tempid :db.part/user)]
    [{:db/id public-category
      :category/id 3 :category/type 0 :category/name "Public Category"}
     {:db/id private-category
      :category/id 4 :category/type 2 :category/name "Private Category"}
     ;     {:id 5 :type 2 :name "No Category" :parent 4}
     ;     {:id 6 :type 2 :name "Staff Recommended Rooms" :parent 4}
     ;     {:id 7 :type 2 :name "Trade Rooms" :parent 4}
     {:db/id (d/tempid :db.part/user)
      :category/id 8 :category/type 0 :category/name "Outside Spaces" :category/parent public-category}
     ;     {:id 9 :type 0 :name "System Rooms (Invisible)" :parent 3}
     ;     {:id 10 :type 0 :name "Unfinished" :parent 9}
     ;     {:id 11 :type 0 :name "Room Parts" :parent 9}
     ;     {:id 12 :type 0 :name "Events" :parent 9}
     {:db/id chill-category
      :category/id 13 :category/type 2 :category/name "Chat Chill & Discussion Rooms" :category/parent private-category}
     ;     {:id 14 :type 2 :name "Casinos" :parent 4}
     ;     {:id 16 :type 2 :name "Help Rooms" :parent 4}
     ;     {:id 17 :type 2 :name "Game Rooms" :parent 4}
     {:db/id user
      :user/username "test"
      :user/password "123"
      :user/mission "something"
      :user/figure "8000119001280152950125516"
      :user/sex "M"}
     {:db/id (d/tempid :db.part/user)
      :room/id 1
      :room/name "Test room"
      :room/description "description"
      :room/category chill-category
      :room/owner user
      :room/model "model_a"}]))

(defn seed-db [db]
  (:db-after (d/with db (seed))))

(defn -main [& args]
  (let [conn (retro.db/ensure-db db-url)
        command (first args)]
    (if command
      (when (= command "seed")
        (do (println "here")
          @(d/transact conn (seed))))
      (do
        (println "Starting TCP server...")
        (tcp/start-tcp-server (partial client-handler {:db (seed-db (d/db conn))})
                              {:port 1234 :decoder frame})))))
