(ns tryclojure.server
  (:use compojure.core)
  (:require [compojure.route :as route]
            [noir.util.middleware :as nm]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [secure-site-defaults site-defaults]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [tryclojure.views.home :as home]
            [tryclojure.views.tutorial :as tutorial]
            [tryclojure.views.eval :as eval]))

(def app-routes
  [(GET "/" [] (home/root-html))
   (GET "/about" [] (home/about-html))
   (GET "/links" [] (home/links-html))
   (POST "/tutorial" [:as {args :params}] (tutorial/tutorial-html (args :page)))
   (POST "/eval.json" [:as {args :params}] (eval/eval-json (args :expr) (args :jsonp)))
   (GET "/eval.json" [:as {args :params}] (eval/eval-json (args :expr) (args :jsonp)))
   (route/resources "/")
   (route/not-found "Not Found")])

(def app
  (nm/app-handler app-routes :ring-defaults (-> site-defaults
                                                (assoc-in [:security :anti-forgery] false)
                                                (assoc-in [:session :store] (ttl-memory-store 900)))))

(def secure-app
  (nm/app-handler app-routes :ring-defaults (-> secure-site-defaults
                                                (assoc-in [:security :anti-forgery] false)
                                                (assoc-in [:session :store] (ttl-memory-store 900))
                                                (assoc :proxy true))))

(defn -main [port]
  (jetty/run-jetty #'secure-app {:port (Long. port) :join? false}))
