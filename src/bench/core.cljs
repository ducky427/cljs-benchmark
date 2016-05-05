(ns bench.core
  (:require [goog.object :as gobj]
            [reagent.core :as reagent]
            [cljsjs.benchmark]))

(enable-console-print!)

(def Suite (.-Suite js/Benchmark))

(def f1 (partial + 1))

(def f2 #(+ 1 %))

(defonce app-state (reagent/atom {:fastest nil
                                  :results {}}))

(defn run-bench
  []
  (let [suite (Suite.)]
    (swap! app-state (fn [xs]
                       (merge xs {:results {}
                                  :fastest nil})))
    (.. suite
        (add "partial"
             (fn []
               (f1 1)))
        (add "anonymous function"
             (fn []
               (f2 1)))
        (on "cycle" (fn [event]
                      (let [b    (-> event .-target)
                            stat (-> b .-stats)]
                        (swap! app-state assoc-in [:results (.-name b)] stat))))
        (on "complete" (fn []
                         (this-as this
                           (let [fastest  (-> this
                                              (.filter "fastest")
                                              (.map "name")
                                              (aget 0))]
                             (swap! app-state assoc :fastest fastest)))))
        (run #js {"async" true}))))

(defn main-page
  []
  (let [data         @app-state
        series-names (sort (keys (:results data)))]
    [:div
     [:button {:onClick #(run-bench)} "Run Benchmark"]
     [:h2 (str "Fastest function: " (:fastest data))]
     (when (seq series-names)
       [:table
        [:thead
         [:tr
          [:th ""]
          (for [h series-names]
            [:th {:key h} h])]]
        [:tbody
         [:tr
          [:td "Mean"]
          (for [h series-names]
            [:td {:key h} (gobj/get (get-in data [:results h]) "mean")])]
         [:tr
          [:td "Deviation"]
          (for [h series-names]
            [:td {:key h} (gobj/get (get-in data [:results h]) "deviation")])]
         [:tr
          [:td "Margin of error"]
          (for [h series-names]
            [:td {:key h} (gobj/get (get-in data [:results h]) "moe")])]
         [:tr
          [:td "Relative moe"]
          (for [h series-names]
            [:td {:key h} (gobj/get (get-in data [:results h]) "rme")])]]])]))

(defn mount-root
  []
  (reagent/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (mount-root))

(init!)
