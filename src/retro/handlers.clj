(ns retro.handlers
  (:require [retro.encoding :refer :all]
            [retro.client-headers :as headers]
            [clojure.string :refer [join]]))

(defn- zip-fill [coll1 coll2 fill]
  (partition 2 (interleave coll1
                           (concat coll2 (repeat fill)))))

(defn greet []
  [{:header headers/greet}])

(defn generate-key [env]
  (let [static-key "[100,105,110,115,120,125,130,135,140,145,150,155,160,165,170,175,176,177,178,180,185,190,195,200,205,206,207,210,215,220,225,230,235,240,245,250,255,260,265,266,267,270,275,280,281,285,290,295,300,305,500,505,510,515,520,525,530,535,540,545,550,555,565,570,575,580,585,590,595,596,600,605,610,615,620,625,626,627,630,635,640,645,650,655,660,665,667,669,670,675,680,685,690,695,696,700,705,710,715,720,725,730,735,740,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822,823,824,825,826,827,828,829,830,831,832,833,834,835,836,837,838,839,840,841,842,843,844,845,846,847,848,849,850,851,852,853,854,855,856,857,858,859,860,861,862,863,864,865,866,867,868,869,870,871,872,873]"]
    [{:header headers/pub-key :body static-key}
     {:header 257 :body (str "RAHIIIKHJIPAIQAdd-MM-yyyy" (char 2))}]))

(defn login [{:keys [user]}]
  (if user
    [{:header headers/fuse-rights :body "fuse_login"} {:header 3}]
    [{:header headers/ban :body "Incorrect username/password"}]))

(defn credits [{:keys [user]}]
  [{:header headers/credits :body (str (:credits user) ".0")}])

(defn club-habbo [env]
  [{:header headers/club :body "club_habboYNEHHI"}
   {:header 23}])

(defn badge [env]
  [])

(defn messenger-init [env]
  [])

(defn user-details [{:keys [user]}]
  [{:header headers/user-details
    :body (str (join \return
                     (map (partial join "=")
                          (zip-fill '("name" "figure" "sex" "customData" "rph_tickets" "photo_film" "directMail")
                                    (map #(% user) '(:username :figure :sex :mission))
                                    0)))
               \return)}])

(defn user-flat-cats [{:keys [user-categories]}]
  [{:header headers/user-flat-cats
    :body (str (encode-vl64 (count user-categories))
               (join (map (fn [category]
                            (str (encode-vl64 (:id category))
                                 (:name category)
                                 (char 2)))
                          user-categories)))}])

;; Begin NAVIGATE

(defn- subcategories [category]
  "[0] = id
   [1] = 0 ?
   [2] = name
   [3] = chr 2
   [4] = Current # users
   [5] = Capacity
   [6] = parent id"
  (join (map #(str (encode-vl64 (:id %))
                   (encode-vl64 0)
                   (:name %)
                   (char 2)
                   (encode-vl64 (:current %))
                   (encode-vl64 (:capacity %))
                   (encode-vl64 (:id category)))
             (:subcategories category))))

(defn- guest-category? [category]
  (= (:type category) 2))

(defn- category-rooms [category]
  (get category :rooms []))

(defn- category-response [category]
  "[0] = Hide full rooms, default: false
   [1] = Category id
   [2] = Category type
   [3] = Category name
   [4] = chr 2
   [5] = 1 ?
   [6] = 100 ?
   [7] = Parent id"
  (str (encode-vl64 0)
       (encode-vl64 (:id category))
       (encode-vl64 (:type category))
       (:name category)
       (char 2)
       (encode-vl64 1)
       (encode-vl64 100)
       (encode-vl64 (get category :parent-id 0))
       (when (guest-category? category)
         (encode-vl64 (count (category-rooms category))))))

(defn- private-room [room]
  "[0] = Id
   [1] = Name
   [2] = chr 2
   [3] = Owner
   [4] = chr 2
   [5] = Status, default: open
   [6] = chr 2
   [7] = # Users
   [8] = Capacity
   [9] = Description
   [10] = chr 2"
  (str (encode-vl64 (:id room))
       (:name room)
       (char 2)
       (get-in room [:owner :username])
       (char 2)
       (:status room)
       (char 2)
       (encode-vl64 (:current room))
       (encode-vl64 (:capacity room))
       (:description room)
       (char 2)))

(defn- public-room [room]
  "[0] = Id
   [1] = 1 ?
   [2] = Name
   [3] = chr 2
   [4] = # Users
   [5] = Capacity
   [6] = Category Id ?
   [7] = Description
   [8] = chr 2
   [9] = Id?
   [10] = Id?
   [11] = CCTs
   [12] = chr 2
   [13] = 0 ?
   [14] = 1 ?"
  (str (encode-vl64 (:id room))
       (encode-vl64 1)
       (:name room)
       (char 2)
       (encode-vl64 (:current room))
       (encode-vl64 (:capacity room))
       (encode-vl64 (:category-id room))
       (:description room)
       (char 2)
       (encode-vl64 (:id room))
       (encode-vl64 (:id room))
       (:ccts room)
       (char 2)
       (encode-vl64 0)
       (encode-vl64 1)))

(defn- rooms [category]
  (map (if (guest-category? category)
         private-room
         public-room)
       (category-rooms category)))

(defn navigate [{:keys [category]}]
  [{:header headers/navigate
    :body (str (category-response category)
               (apply str (rooms category))
               (subcategories category))}])

;; end NAVIGATE

(defn room-info [{:keys [room]}]
  "[0] = Super users ?
   [1] = Status (Enum)
   [2] = Id
   [3] = Owner
   [4] = Chr 2
   [5] = Model
   [6] = Chr 2
   [7] = Name
   [8] = Chr 2
   [9] = Description
   [10] = Chr 2
   [11] = Show owner [Bool]
   [12] = Enable trade [Bool]
   [13] = Current users
   [14] = Capacity"
  [{:header headers/room-info
    :body (str (encode-vl64 0)
               (encode-vl64 0)
               (encode-vl64 (:id room))
               (get-in room [:owner :username])
               (char 2)
               (get-in room [:model :model])
               (char 2)
               (:name room)
               (char 2)
               (:description room)
               (char 2)
               (encode-vl64 0)
               (encode-vl64 0)
               (encode-vl64 (:current room))
               (encode-vl64 (:capacity room))
               )}])

(defn room-directory [{:keys [room]}]
  [{:header headers/room-directory
    :body ""}])

(defn try-flat [{:keys [room]}]
  [{:header headers/try-flat
    :body ""}])

(defn goto-flat [{:keys [room]}]
  [{:header headers/ad
    :body "about:blank"}
   {:header headers/room-model
    :body (get-in room [:model :model])}
   {:header headers/room-decoration
    :body (str "wallpaper/" (:wallpaper room))}
   {:header headers/room-decoration
    :body (str "floor/" (:floor room))}])

(defn heightmap [{:keys [room]}]
  [{:header headers/heightmap
    :body (get-in room [:model :heightmap])}])

(defn users [env]
  [{:header headers/users
    :body ""}])

(defn objects [{:keys [floor-items]}]
  [{:header headers/wall-items
    :body ""}
   {:header headers/floor-items
    :body (join (map (fn [{:keys [id sprite x y z width length column rotation furni-var]}]
                       (str id
                            (char 2)
                            sprite
                            (char 2)
                            (encode-vl64 x)
                            (encode-vl64 y)
                            (encode-vl64 width)
                            (encode-vl64 length)
                            (encode-vl64 rotation)
                            (encode-vl64 z)
                            (char 2)
                            (encode-vl64 column)
                            (char 2)
                            (char 2)
                            (encode-vl64 0)
                            furni-var
                            (char 2)))
                     floor-items))}])

(defn items [env]
  [{:header headers/items
    :body ""}])

(defn- user-state-move [{:keys [x y z]}]
  (fn []
    (str "/mv " (join "," (list x y z)))))

(defn- user-movement [{:keys [x y z body head room-id states]}]
  {:header headers/movement
   :body (str room-id ; room user id
              \space
              (join "," (list x y z body head)) ; user location
              (join (map (fn [f] (f)) (or states [])))
              \return)})

(defn gstat [{:keys [user user-state]}]
  (let [{:keys [x y z body head room-id] :as user-loc} @user-state]
    [{:header headers/users
      :body (str "i:" room-id \return
                 "n:" (:username user) \return
                 "f:" (:figure user) \return
                 "l:" (join " " (list x y z)) \return
                 "s:" (:sex user) \return
                 "c:" (:mission user) \return)}
     {:header 42 :body ""} ; rights?
     {:header 47 :body ""} ; admin rights?
     (user-movement user-loc)]))

(defn get-interest [env]
  [{:header headers/get-interest :body "0"}])

(defn room-ad [env]
  [])

(defn room-movement [{:keys [user-state]}]
  [(user-movement @user-state)])

(defn move-to [{:keys [user-state path]}]
  (let [path (map #(assoc % :z (:z @user-state)) path)
        movement (map (fn [[a b]]
                        (user-movement (merge @user-state
                                              a
                                              {:body (:body b)
                                               :head (:head b)
                                               :states (list (user-state-move b))})))
                      (partition 2 1 (cons @user-state path)))
        last-movement (merge @user-state (last path))]
    (cons (first movement)
          (map #(assoc % :delay 500)
               (concat (rest movement)
                       (list (assoc (user-movement last-movement) :thunk (fn [] (reset! user-state last-movement)))))))))
