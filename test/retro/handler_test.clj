(ns retro.handler-test
  (:require [clojure.test :refer :all]
            [retro.handlers :refer :all]))

(defn make-check
  ([handler]
   (fn [expected]
     (make-check expected {})))
  ([handler env & args]
   (fn [expected]
     (is (= expected (apply handler (cons env args)))))))

(deftest greet-test
  (let [check (make-check greet)]
    (testing "greet"
      (check [{:header 0}]))
  ))

(deftest generate-key-test
  (let [check (make-check generate-key)]
    (testing "generate-key"
      (let [static-key "[100,105,110,115,120,125,130,135,140,145,150,155,160,165,170,175,176,177,178,180,185,190,195,200,205,206,207,210,215,220,225,230,235,240,245,250,255,260,265,266,267,270,275,280,281,285,290,295,300,305,500,505,510,515,520,525,530,535,540,545,550,555,565,570,575,580,585,590,595,596,600,605,610,615,620,625,626,627,630,635,640,645,650,655,660,665,667,669,670,675,680,685,690,695,696,700,705,710,715,720,725,730,735,740,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822,823,824,825,826,827,828,829,830,831,832,833,834,835,836,837,838,839,840,841,842,843,844,845,846,847,848,849,850,851,852,853,854,855,856,857,858,859,860,861,862,863,864,865,866,867,868,869,870,871,872,873]"]
        (check [{:header 8 :body static-key}
                {:header 257 :body (str "RAHIIIKHJIPAIQAdd-MM-yyyy" (char 2))}]))))
  )

(deftest login-test
  (testing "invalid user"
    (let [check (make-check login {:body ["foo" "bar"]})]
      (check [{:header 3 :body "Incorrect username/password"}])))

  (testing "valid user"
    (let [check (make-check login {:body ["test" "123"]})]
      (check [{:header 7 :body "fuse_login"}
              {:header 3}])))
  )

(deftest credits-test
  (let [check (make-check credits {:user (map->User {:credits 120})})]
    (testing "credits"
      (check [{:header 6 :body "120.0"}]))))

(deftest club-habbo-test
  (testing "club habbo"
    (let [check (make-check club-habbo)]
      (check [{:header 7 :body "club_habboYNEHHI"}
              {:header 23}]))))

(deftest navigate-test
  (testing "public category with subcategory"
    (is (= (navigate {:body "HKI"} {:name "Category Name"
                                    :id 3
                                    :type 1
                                    :capacity 100
                                    :current 1
                                    :subcategories [{:name "Subcategory 1"
                                                     :id 8
                                                     :current 20
                                                     :capacity 200}]})
           {:header 220
            :body (str "HKICategory Name" (char 2) "IPYH"
                       "PBHSubcategory 1" (char 2) "PEPrK")})))

  (testing "public subcategory with rooms"
    (is (= (navigate {:body "HKI"} {:name "Subcategory Name"
                                    :id 10
                                    :type 1
                                    :capacity 100
                                    :current 1
                                    :parent 2
                                    :rooms [{:name "Public room"
                                             :id 25
                                             :current 20
                                             :capacity 100
                                             :category-id 10
                                             :ccts "0" ; thats not right....
                                             :description "description"}]})
           {:header 220
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
                       "HI")})))


  (testing "private category with rooms"
    (is (= (navigate {:body "HPAI"} {:name "Private Category"
                                     :id 1
                                     :type 2
                                     :capacity 4
                                     :current 1
                                     :rooms [{:id 1
                                              :name "Room Name"
                                              :owner "Room owner"
                                              :status "open"
                                              :current 1
                                              :capacity 4
                                              :description "description"
                                              }]})
           {:header 220
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
                       (char 2))})))
  )
