(defproject retro "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [com.datomic/datomic-free "0.9.5173" :exclusions [joda-time]]
                 [aleph "0.3.4-SNAPSHOT"]]
  :aot [aleph.tcp lamina.core gloss.core retro.fixtures retro.encoding]
  :main retro.core)
