(ns retro.records)

(defrecord User [username figure sex mission tickets film mail])
(defrecord Category [name type rooms subcategories])
(defrecord Room [name description owner wallpaper floor model id])
(defrecord RoomModel [name heightmap x y z])
