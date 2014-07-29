(ns retro.handlers
  (:require [retro.encoding :refer :all]
            [clojure.string :refer [join]]))

(defrecord User [username figure sex mission tickets film mail])

(defn greet [env]
  {:header 0})

(defn generate-key [env]
  (let [static-key "[100,105,110,115,120,125,130,135,140,145,150,155,160,165,170,175,176,177,178,180,185,190,195,200,205,206,207,210,215,220,225,230,235,240,245,250,255,260,265,266,267,270,275,280,281,285,290,295,300,305,500,505,510,515,520,525,530,535,540,545,550,555,565,570,575,580,585,590,595,596,600,605,610,615,620,625,626,627,630,635,640,645,650,655,660,665,667,669,670,675,680,685,690,695,696,700,705,710,715,720,725,730,735,740,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822,823,824,825,826,827,828,829,830,831,832,833,834,835,836,837,838,839,840,841,842,843,844,845,846,847,848,849,850,851,852,853,854,855,856,857,858,859,860,861,862,863,864,865,866,867,868,869,870,871,872,873]"]
    [{:header 8 :body static-key}
     {:header 257 :body (str "RAHIIIKHJIPAIQAdd-MM-yyyy" (char 2))}]))

(defn login [user]
  (if user
    [{:header 7 :body "fuse_login"} {:header 3}]
    [{:header 3 :body "Incorrect username/password"}]))

(defn credits [env]
  [{:header 6 :body (str (get-in env [:user :credits]) ".0")}])

(defn club-habbo [env]
  [{:header 7 :body "club_habboYNEHHI"}
   {:header 23}])

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
       (encode-vl64 (get category :parent 0))
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
       (:owner room)
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

(defn navigate [env category]
  {:header 220
   :body (str (category-response category)
              (apply str (rooms category))
              (subcategories category))})
