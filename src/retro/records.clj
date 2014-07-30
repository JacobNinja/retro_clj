(ns retro.records)

(defrecord User [username figure sex mission tickets film mail])
(defrecord Category [name type rooms subcategories])
(defrecord Room [name description type owner wallpaper floor])
