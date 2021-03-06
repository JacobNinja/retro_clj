(ns retro.reactors
  (:require [retro.protocol :as protocol]
            [retro.db :as db]
            [retro.path-finding :as p]
            [clojure.string :refer [split]]))

(defn- with-model [room models]
  (assoc room :model (get models (:model room))))

(defn- room-with-model [room-id db models]
  (let [room (db/fetch-room (Integer/parseInt room-id) db)]
    (with-model room models)))

(defn- with-sprite [sprites object]
  (assoc object :sprite (sprites (:sprite object))))

;; Handlers

(defn default [p env])

(defn login [packet {:keys [db]}]
  (let [[username password] (protocol/packet-body packet)]
    (when-let [user (db/fetch-user username password db)]
      {:user (atom user)})))

(defn navigate [packet {:keys [db]}]
  (let [[hide-full? category-id _] (protocol/packet-values packet)]
    {:category (db/fetch-category category-id db)}))

(defn user-flat-cats [packet {:keys [db]}]
  {:user-categories (db/fetch-user-categories db)})

(defn room-info [room-id {:keys [db room-models]}]
  {:room (room-with-model room-id db room-models)})

(defn goto-flat [room-id {:keys [db room-states room-models user] :as env}]
  (let [room (room-with-model room-id db room-models)
        model (:model room)
        user-loc (update-in (merge (select-keys model [:x :y :z])
                                   {:head 2 :body 2 :room (:id room)})
                            [:states] assoc :rights (fn [] "/flatctrl"))]
    (swap! user merge user-loc)
    (swap! room-states update-in [(:id room) :users] assoc (:username @user) user)
    {:room room}))

(defn look-to [packet {:keys [user]}]
  (let [[body head] (protocol/point packet)]
    (swap! user merge {:body body :head head})
    nil))

(defn move-to [packet {:keys [user room db]}]
  (let [[next-x next-y] (protocol/packet-values-b64 packet)
        {:keys [x y]} @user
        floor-items (db/fetch-floor-items db (:room @user))]
    {:path (p/find-path [x y] [next-x next-y] floor-items)}))

(defn search-flats [search-term {:keys [db room-models]}]
  {:rooms (map #(with-model % room-models)
               (db/search-rooms search-term db))})

(defn objects [_ {:keys [db user]}]
  {:floor-items (db/fetch-floor-items db (:room @user))})

(defn move-object [validate-fn packet {:keys [conn] :as env}]
  (let [[object-id x y rotation] (map #(Integer/parseInt %)
                             (protocol/split packet))
        object (db/move-floor-item object-id x y rotation conn)]
    {:move-object object}))

(defn go-away [packet {:keys [room-states user]}]
  (let [current-room (:room @user)]
    (swap! room-states update-in [current-room :users] #(dissoc % (:username @user)))
    nil))

(defn pick-up [packet {:keys [conn]}]
  (let [[_ _ floor-item-id] (protocol/split packet)]
    {:pick-up (db/pick-up-floor-item (Integer/parseInt floor-item-id)
                                     conn)}))

(defn hand [_ {:keys [db user sprites]}]
  {:objects (map (partial with-sprite sprites)
                 (db/fetch-hand-objects (:username @user) db))})

(defn place-stuff [packet {:keys [conn user sprites]}]
  (let [[id x y _ _ rotation] (map #(Integer/parseInt %) (protocol/split packet))]
    {:place (with-sprite sprites
              (db/place-floor-item conn id x y rotation (:room @user)))}))

(defn catalog-page-info [packet {:keys [pages]}]
  (let [[_ page _] (protocol/split packet #"\/")]
    {:page (pages page)}))

(defn catalog-purchase [packet {:keys [conn user pages]}]
  (let [[_ category _ purchase-code _] (protocol/split packet #"\r")
        sprite (get-in (first (filter #(= (:purchase-code %) purchase-code)
                                      (:items (pages category))))
                       [:furni :sprite])]
    (db/purchase conn sprite @user)
    nil))

(defn wave [_ {:keys [user]}]
  (swap! user update-in [:states] assoc :wave (fn [] "/wave")))

(defn dance [_ {:keys [user]}]
  (swap! user update-in [:states] assoc :dance (fn [] "/dance")))

(defn stop [packet {:keys [user]}]
  (let [command (condp = packet
                  "Dance" :dance
                  nil)]
    (swap! user update-in [:states] dissoc command)
    nil))
