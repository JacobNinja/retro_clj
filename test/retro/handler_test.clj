(ns retro.handler-test
  (:require [clojure.test :refer :all]
            [retro.handlers :refer :all]
            [retro.records :refer :all]
            [retro.encoding :as encoding]))

(defn make-check [handler & args]
  (fn [expected]
    (is (= expected (apply handler args)))))

(deftest greet-test
  (let [check (make-check greet)]
    (testing "greet"
      (check [{:header 0}]))
    ))

(deftest generate-key-test
  (let [check (make-check generate-key {})]
    (testing "generate-key"
      (let [static-key "[100,105,110,115,120,125,130,135,140,145,150,155,160,165,170,175,176,177,178,180,185,190,195,200,205,206,207,210,215,220,225,230,235,240,245,250,255,260,265,266,267,270,275,280,281,285,290,295,300,305,500,505,510,515,520,525,530,535,540,545,550,555,565,570,575,580,585,590,595,596,600,605,610,615,620,625,626,627,630,635,640,645,650,655,660,665,667,669,670,675,680,685,690,695,696,700,705,710,715,720,725,730,735,740,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822,823,824,825,826,827,828,829,830,831,832,833,834,835,836,837,838,839,840,841,842,843,844,845,846,847,848,849,850,851,852,853,854,855,856,857,858,859,860,861,862,863,864,865,866,867,868,869,870,871,872,873]"]
        (check [{:header 8 :body static-key}
                {:header 257 :body (str "RAHIIIKHJIPAIQAdd-MM-yyyy" (char 2))}]))))
  )

(deftest login-test
  (testing "invalid user"
    (let [check (make-check login {})]
      (check [{:header 35 :body "Incorrect username/password"}])))

  (testing "valid user"
    (let [check (make-check login {:user (atom (map->User {}))})]
      (check [{:header 2 :body "fuse_login"}
              {:header 3}])))
  )

(deftest credits-test
  (let [check (make-check credits {:user (atom (map->User {:credits 120}))})]
    (testing "credits"
      (check [{:header 6 :body "120.0"}]))))

(deftest club-habbo-test
  (testing "club habbo"
    (let [check (make-check club-habbo {})]
      (check [{:header 7 :body "club_habboYNEHHI"}
              {:header 23}]))))

(deftest navigate-test
  (testing "public category with subcategory"
    (is (= (navigate {:category (map->Category {:name "Category Name"
                                                :id 3
                                                :type 1
                                                :capacity 100
                                                :current 1
                                                :subcategories [{:name "Subcategory 1"
                                                                 :id 8
                                                                 :current 20
                                                                 :capacity 200}]})})
           [{:header 220
             :body (str "HKICategory Name" (char 2) "IPYH"
                        "PBHSubcategory 1" (char 2) "PEPrK")}])))

  (testing "public subcategory with rooms"
    (is (= (navigate {:category (map->Category {:name "Subcategory Name"
                                                :id 10
                                                :type 1
                                                :capacity 100
                                                :current 1
                                                :parent-id 2
                                                :rooms [{:name "Public room"
                                                         :id 25
                                                         :current 20
                                                         :capacity 100
                                                         :category-id 10
                                                         :ccts "0" ; thats not right....
                                                         :description "description"}]})})
           [{:header 220
             :body (str "HRBISubcategory Name"
                        (char 2)
                        "IPY"
                        "J" ; parent
                        "QF" ; Room id
                        "I" ; ?
                        "Public room"
                        (char 2)
                        "PE" ; current
                        "PY" ; capacity
                        "RB" ; category id ?
                        "description"
                        (char 2)
                        "QF" ; room id ?
                        "QF" ; room id ?
                        "0" ; ccts
                        (char 2)
                        "HI")}])))


  (testing "private category with rooms"
    (is (= (navigate {:category (map->Category {:name "Private Category"
                                                :id 1
                                                :type 2
                                                :capacity 4
                                                :current 1
                                                :rooms [{:id 1
                                                         :name "Room Name"
                                                         :owner (map->User {:username "Room owner"})
                                                         :status "open"
                                                         :current 1
                                                         :capacity 4
                                                         :description "description"
                                                         }]})})
           [{:header 220
             :body (str "H" ; hide full
                        "I" ; id
                        "J" ; type
                        "Private Category"
                        (char 2)
                        "I" ; current
                        "PY" ; capacity
                        "H" ; parent
                        "I" ; room count
                        ; end category
                        ; start rooms
                        "I" ; room id
                        "Room Name"
                        (char 2)
                        "Room owner"
                        (char 2)
                        "open" ; status
                        (char 2)
                        "I" ; # users
                        "PA" ; max users
                        "description"
                        (char 2))}])))
  )

(deftest messenger-init-test
  (testing "messenger init"
    (is (= (messenger-init {}) []))))

; name=test\rfigure=8000119001280152950125516\rsex=M\rcustomData=mission\rph_tickets=0\rphoto_film=0\rdirectMail=0\r

(deftest user-details-test
  (testing "user details"
    (let [user (atom (map->User {:username "test"
                           :figure "123"
                           :sex "M"
                           :mission "mission"}))]
      (is (= (user-details {:user user})
             [{:header 5
               :body (str "name=test"
                          \return
                          "figure=123"
                          \return
                          "sex=M"
                          \return
                          "customData=mission"
                          \return
                          "rph_tickets=0"
                          \return
                          "photo_film=0"
                          \return
                          "directMail=0"
                          \return)}])))))

(deftest user-flat-cats-test
  (testing "user flat categories"
    (is (= (user-flat-cats {:user-categories [(map->Category {:name "Category name" :id 10})]})
                          [{:header 221
                            :body (str "I" ; # categories
                                       "RB" ; category id
                                       "Category name"
                                       (char 2))}]))))

(deftest room-info-test
  (testing "room info"
    (is (= (room-info {:room (map->Room {:name "Room name"
                                         :description "description"
                                         :id 1
                                         :status "open"
                                         :owner (map->User {:username "owner"})
                                         :current 0
                                         :capacity 25
                                         :model (map->RoomModel {:model "model"})})})
           [{:header 54
             :body (str "H" ; super users?
                        "H" ; state enum ?
                        "I" ; room id
                        "owner" ; room owner
                        (char 2)
                        "model" ; room model
                        (char 2)
                        "Room name"
                        (char 2)
                        "description"
                        (char 2)
                        "H" ; show owner
                        "H" ; can trade
                        "H" ; current users
                        "QF" ; capacity
                        )}]))))

(deftest room-directory-test
  (testing "private room directory"
    (is (= (room-directory {})
           [{:header 19
             :body ""}]))))

(deftest try-flat-test
  (testing "try flat private room"
    (is (= (try-flat {:room (map->Room {})})
           [{:header 41
             :body ""}]))))

(deftest goto-flat-test
  (testing "goto flat private"
    (is (= (goto-flat {:room (map->Room {:floor "xyz"
                                         :wallpaper "zyx"
                                         :model (map->RoomModel {:model "model"})})})
           [{:header 166
             :body "about:blank"}
            {:header 69
             :body "model"}
            {:header 46
             :body "wallpaper/zyx"}
            {:header 46
             :body "floor/xyz"}]))))

(deftest heightmap-test
  (testing "empty heightmap"
    (is (= (heightmap {:room (map->Room {:model (map->RoomModel {:heightmap "xxx"})})})
           [{:header 31
             :body "xxx"}]))))

(deftest users-test
  (testing "empty room"
    (is (= (users {})
           [{:header 28
             :body ""}]))))

(deftest objects-test
  (testing "empty objects"
    (is (= (objects {})
           [{:header 30
             :body ""}
            {:header 32
             :body ""}])))

  (testing "floor items"
    (is (= (objects {:floor-items [(map->FloorItem {:id 1
                                                    :sprite "xxx"
                                                    :x 1
                                                    :y 2
                                                    :z 0
                                                    :rotation 0
                                                    :column "0,0,0"
                                                    :teleport-id 456
                                                    :var "yyy"})]
                     :sprites {"xxx" {:width 1
                                      :length 2}}})
           [{:header 30
             :body ""}
            {:header 32
             :body (str "I" ; # floor items
                        "1" ; id
                        (char 2)
                        "xxx" ; sprite
                        (char 2)
                        "I" ; x
                        "J" ; y
                        "I" ; width
                        "J" ; length
                        "H" ; rotation
                        "0.0" ; z
                        (char 2)
                        "0,0,0" ; column
                        (char 2) (char 2)
                        "XrA" ; teleport id
                        "yyy" ; furni var
                        (char 2))}]))))

(deftest items-test
  (testing "empty items"
    (is (= (items {})
           [{:header 45
             :body ""}]))))

(deftest gstat-test
  (testing "gstat first user no states"
    (is (= (gstat {:user (atom (map->User {:username "test"
                                           :figure "123"
                                           :sex "m"
                                           :mission "mission"
                                           :room 1
                                           :x 0
                                           :y 1
                                           :z 2
                                           :body 5
                                           :head 6}))})
           [{:header 28
             :body (str "i:1" \return ; room user id
                        "n:test" \return ; user name
                        "f:123" \return ; user figure
                        "l:0 1 2" \return ; user location
                        "s:m" \return ; user sex
                        "c:mission" \return ; user mission
                        )}
            {:header 42 :body ""}
            {:header 47 :body ""}
            {:header 34
             :body (str "1 " ; room user id
                        "0,1,2" ; user location
                        ",5,6" ; body/head direction
                        "" ; user states
                        \return
                        )}]))))

(deftest get-interest-test
  (testing "get interest"
    (is (= (get-interest {})
           [{:header 258 :body "0"}]))))

(deftest room-ad-test
  (testing "private room"
    (is (= (room-ad {})
           []))))

(deftest room-movement-test
  (testing "room movement"
    (is (= (room-movement {:user (atom {:x 0 :y 1 :z 2 :body 5 :head 6 :room 1})})
           [{:header 34
             :body (str "1 0,1,2,5,6" \return)}]))))

(deftest move-to-test
  (let [env {:user (atom {:x 0 :y 1 :z 2 :body 1 :head 2 :room 1})
             :path [{:x 1 :y 1 :z 2 :body 5 :head 6}]}]
    (testing "single move"
      (is (= (map #(select-keys % [:header :body :delay])
                  (move-to env))
             [{:header 34
               :body (str "1 0,1,2,5,6/mv 1,1,2" \return)}
              {:header 34
               :body (str "1 1,1,2,5,6" \return)
               :delay 500}])))

    (testing "multiple moves with delay"
      (let [env (assoc env :path [{:x 1 :y 1 :z 2 :body 5 :head 6} {:x 2 :y 1 :z 2 :body 3 :head 4}])]
        (is (= (map #(select-keys % [:header :body :delay])
                    (move-to env))
               [{:header 34
                 :body (str "1 0,1,2,5,6/mv 1,1,2" \return)}
                {:header 34
                 :body (str "1 1,1,2,3,4/mv 2,1,2" \return)
                 :delay 500}
                {:header 34
                 :body (str "1 2,1,2,3,4" \return)
                 :delay 500}]))))))

(deftest move-stuff-test
  (let [env {:move-object (map->FloorItem {:id 123 :x 1 :y 2 :z 0 :sprite "xxx" :rotation 5 :column 6 :var "foo"})
             :room {:model {:heightmap "xxx"}}
             :sprites {"xxx" {:width 3 :length 4}}}]
    (testing "move furni to x y"
      (is (= (move-object env)
             [{:header 95
              :body (str "123"
                         (char 2)
                         "xxx"
                         (char 2)
                         (encoding/encode-vl64 1) ; x
                         (encoding/encode-vl64 2) ; y
                         (encoding/encode-vl64 3) ; width
                         (encoding/encode-vl64 4) ; length
                         (encoding/encode-vl64 5) ; rotation
                         "0.0" ; z
                         (char 2)
                         6 ; column
                         (char 2)
                         (char 2)
                         (encoding/encode-vl64 0) ; teleport id
                         "foo" ; var
                         (char 2)
                         )}
              {:header 31
               :body "xxx"}])))))

(deftest go-away-test
  (testing "user clicks door"
    (is (= (go-away {})
           [{:header 18 :body ""}]))))

(deftest pick-up-test
  (testing "pick up floor item"
    (is (= (pick-up {:pick-up (map->FloorItem {:id 1})})
           [{:header 94 :body "1"}]))))

(deftest hand-test
  (testing "objects not in room belonging to user"
    (is (= (hand {:objects [(map->FloorItem {:id 123
                                             :sprite {:hand-type "S" :sprite "foo" :width 1 :length 2 :col "0,0,0"}})]})
           [{:header 140
             :body (str "SI"
                        (char 30)
                        123
                        (char 30)
                        0
                        (char 30)
                        "S"
                        (char 30)
                        123
                        (char 30)
                        "foo"
                        (char 30)
                        1
                        (char 30)
                        2
                        (char 30)
                        "0,0,0"
                        "/"
                        (char 13)
                        (encoding/encode-vl64 1)
                        )}]))))

(deftest place-stuff-test
  (testing "place object"
    (is (= (place-stuff {:place (map->FloorItem {:id 123
                                                 :sprite {:sprite "foo"
                                                          :width 3
                                                          :length 4
                                                          :col "0,0,0"}
                                                 :x 1
                                                 :y 2
                                                 :z 0
                                                 :rotation 5
                                                 :teleport-id 456
                                                 :var "var"})})
           [{:header 93
             :body (str "123"
                        (char 2)
                        "foo"
                        (char 2)
                        (encoding/encode-vl64 1) ; x
                        (encoding/encode-vl64 2) ; y
                        (encoding/encode-vl64 3) ; width
                        (encoding/encode-vl64 4) ; length
                        (encoding/encode-vl64 5) ; rotation
                        0 ; z
                        (char 2)
                        "0,0,0" ; col
                        (char 2)
                        (char 2)
                        (encoding/encode-vl64 456) ; teleport id
                        "var" ; var
                        (char 2)
                        )}]))))

(deftest catalog-pages-test
  (testing "list pages in order"
    (is (= (catalog-pages {:pages {"Foo" (map->CatalogPage {:name "Foo"
                                                            :order 10
                                                            :visible-name "foo"})
                                   "Bar" (map->CatalogPage {:name "Bar"
                                                            :order 2
                                                            :visible-name "bar"})}})
           [{:header 126
             :body (str "Bar"
                        (char 9)
                        "bar"
                        \return

                        "Foo"
                        (char 9)
                        "foo"
                        \return
                        )}]))))

(deftest catalog-page-info-test
  (testing "floor items"
    (is (= (catalog-page-info {:page (map->CatalogPage {:name "Foo"
                                                        :visible-name "foo"
                                                        :image "image"
                                                        :side-image "side_image"
                                                        :description "description"
                                                        :label "label"
                                                        :layout "layout"
                                                        :additional ["s:2" "f:foo"]
                                                        :items [{:furni {:hand-type "s" :sprite "bar" :width 2 :length 3 :col "0,0,0"},
                                                                 :cost 10,
                                                                 :purchase-code "purchase_bar_3"}]})})
           [{:header 127
             :body (str "i:Foo" \return
                        "n:foo" \return
                        "l:layout" \return
                        "g:image" \return
                        "e:side_image" \return
                        "h:description" \return
                        "h:label" \return
                        "s:2" \return
                        "f:foo" \return
                        ; start items
                        "p:name" ; hrm?
                        (char 9)
                        "description"
                        (char 9)
                        10
                        (char 9) (char 9)
                        "s"
                        (char 9)
                        "bar"
                        (char 9)
                        0 ; ?
                        (char 9)
                        2 ; width
                        ","
                        3 ; length
                        (char 9)
                        "purchase_bar_3"
                        (char 9)
                        "0,0,0" ; col
                        \return
                        )}]))))

(deftest catalog-purchase-test
  (testing "success"
    (is (= (catalog-purchase {})
           [{:header 67
             :body ""}]))))

(deftest wave-test
  (testing "wave state"
    (let [user (atom {:x 0 :y 1 :z 2 :body 5 :head 6 :room 1 :states {:wave (fn [] "/wave")}})
          result (wave {:user user})]
      (is (= (first result)
             {:header 34
              :body (str "1 0,1,2,5,6/wave" \return)}))
      (is (= (select-keys (second result)
                          [:header :body :delay])
             {:header 34
              :body (str "1 0,1,2,5,6" \return)
              :delay 2000}))
      ; Remove wave status
      ((:thunk (second result)))
      (is (nil? (get-in @user [:states :wave]))))))

(deftest dance-test
  (testing "dance state"
    (let [user (atom {:x 0 :y 1 :z 2 :body 5 :head 6 :room 1 :states {:dance (fn [] "/dance")}})]
      (is (= (dance {:user user})
             [{:header 34
               :body (str "1 0,1,2,5,6/dance" \return)}])))))
