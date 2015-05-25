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
   headers/heightmap [reactors/default handlers/heightmap]
   headers/items [reactors/default handlers/items]
   headers/objects [reactors/objects handlers/objects]
   headers/try-flat [reactors/room-info handlers/try-flat]
   headers/goto-flat [reactors/goto-flat handlers/goto-flat]
   headers/get-interest [reactors/default handlers/get-interest]
   headers/room-ad [reactors/default handlers/room-ad]
   headers/users [reactors/default handlers/users]
   headers/gstat [reactors/default handlers/gstat]
   headers/move-to [reactors/move-to handlers/move-to]
   headers/room-directory [reactors/default handlers/room-directory]
   headers/look-to [reactors/look-to handlers/room-movement]
   headers/navigate [reactors/navigate handlers/navigate]
   headers/move-object [(partial reactors/move-object (fn [& args] true)) handlers/move-object]})

(defn send-packet [ch packet]
  (when (:delay packet)
    (Thread/sleep (:delay packet)))
  (when-let [thunk (:thunk packet)]
    (thunk))
  (println (str "SEND: " packet))
  (l/enqueue ch (protocol/encode-packet packet)))

(defn- with-state [{:keys [conn] :as env}]
  (assoc env :db (d/db conn)))

(defn- handle-packet [packet env]
  (when-let [[reactor handler] (mapping (encoding/decode-b64 (subs packet 0 2)))]
    (let [state (with-state @env)]
      (swap! env merge (reactor (subs packet 2) state))
      (handler (merge state @env)))))

(defn- response-handler [env ch replay packets]
  (doseq [packet packets]
    (swap! replay conj packet)
    (println (str "INCOMING: " packet))
    (try
      (doseq [r (handle-packet packet env)]
        (send-packet ch r))
      (catch Exception e
        (spit "replay.out" @replay)
        (.printStackTrace e)
        (println e)))))

(defn- client-handler [env ch info]
  (println "CONNECTED")
  (send-packet ch (first (handlers/greet)))
  (l/receive-all ch (partial response-handler (atom env) ch (atom []))))

(def frame
  (g/repeated (g/string :ascii
                        :prefix (g/prefix (g/string :ascii :length 3)
                                          #(encoding/decode-b64 (subs % 1))
                                          identity))
              :prefix :none))

(def room-models
  {"model_a" {:id 1
              :name "Guest Model A"
              :max_guests nil
              :heightmap (apply str (replace {\| \return} "xxxxxxxxxxxx|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxx00000000|xxxxxxxxxxxx|xxxxxxxxxxxx"))
              :ccts "0",
              :x 3
              :y 5
              :z 0
              :model "model_a"
              :max_ascend 1.5
              :max_descend 4.0}})

(def test-sprites
  {"md_limukaappi" {:sprite "md_limukaappi", :flags "M", :width 1, :length 1, :height 0.0, :col "0,0,0", :var_type 4, :action_height 0.0, :can_trade 1, :public 0, :hand_type "S"}})

(defn seed [conn]
  (let [public-category (d/tempid :db.part/user)
        private-category (d/tempid :db.part/user)
        chill-category (d/tempid :db.part/user)
        user (d/tempid :db.part/user)
        user-room-id (d/tempid :db.part/user)]
    @(d/transact conn
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
                  {:db/id user-room-id
                   :room/id 1
                   :room/name "Test room"
                   :room/description "description"
                   :room/category chill-category
                   :room/owner user
                   :room/model "model_a"}
                  {:db/id (d/tempid :db.part/user)
                   :floor-item/id 123
                   :floor-item/x 8
                   :floor-item/y 8
                   :floor-item/z 0
                   :floor-item/room user-room-id
                   :floor-item/sprite "md_limukaappi"}])))

(defn replay [conn]
  (seed conn)
  (let [packets (read-string (slurp "replay.out"))
        env (atom {:room-models room-models
                   :room-states (atom {})
                   :sprites test-sprites
                   :conn conn})]
    (try
      (doseq [packet packets]
        (when (string? packet)
          (println packet)
          (handle-packet packet env)))
      (catch Exception e
        (.printStackTrace e)
        (println e)))))

(defn -main [& args]
  (let [conn (retro.db/ensure-db db-url)
        command (first args)]
    (condp = command
      "seed" (seed conn)
      "replay" (replay conn)
      (do

        (println "Starting TCP server...")
        (tcp/start-tcp-server (partial client-handler {:room-models room-models
                                                       :room-states (atom {})
                                                       :sprites test-sprites
                                                       :conn conn})
                              {:port 1234 :decoder frame})))))
