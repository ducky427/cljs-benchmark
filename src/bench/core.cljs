(ns bench.core
  (:require [goog.object :as gobj]
            [reagent.core :as reagent]
            [cljsjs.benchmark]))

(enable-console-print!)

(def Suite (.-Suite js/Benchmark))

(def f1 (partial + 1))

(def f2 #(+ 1 %))

(defonce app-state (reagent/atom {:fastest nil
                                  :running? false
                                  :results []}))

(defn run-bench
  []
  (let [suite (Suite.)]
    (swap! app-state (fn [xs]
                       (merge xs {:results []
                                  :running? true
                                  :fastest nil})))
    (.. suite
        (add "partial function (partial + 1)"
             (fn []
               (f1 1)))
        (add "anonymous function #(+ 1 %)"
             (fn []
               (f2 1)))
        (on "cycle" (fn [event]
                      (let [b    (-> event .-target)
                            stat (-> b .-stats)]
                        (swap! app-state update :results  conj [(.-name b) stat]))))
        (on "complete" (fn []
                         (this-as this
                           (let [fastest  (-> this
                                              (.filter "fastest")
                                              (.map "name")
                                              (aget 0))]
                             (swap! app-state merge {:fastest fastest
                                                     :running? false})))))
        (run #js {"async" true}))))

(defn create-row
  [header n results fastest]
  [:tr
   [:td header]
   (for [[k v] results]
     [:td {:key k
           :style (when (= k fastest)
                    {:background-color "#d9edf7"})}
      (gobj/get v n)])])

(defn main-page
  []
  (let [data         @app-state
        series-names (map first (:results data))
        running?     (:running? data)
        fastest      (:fastest data) ]
    [:div
     [:h1 "Benchmarking Clojurescript code"]
     [:button.btn.btn-primary {:onClick (fn []
                                          (when-not running?
                                            (run-bench)))
                               :disabled (when running?
                                           "disabled")}
      "Run Benchmark"]
     (when running?
       [:h3 "Benchmarks are running"])
     (when (seq series-names)
       [:div
        [:h3 (str "Fastest function: " (:fastest data))]
        [:table.table
         [:thead
          [:tr
           [:th ""]
           (for [h series-names]
             [:th {:key h
                   :style (when (= h fastest)
                            {:background-color "#d9edf7"})} h])]]
         [:tbody
          [create-row "Mean" "mean" (:results data) fastest]
          [create-row "Deviation" "deviation" (:results data) fastest]
          [create-row "Margin of error" "moe" (:results data) fastest]
          [create-row "Relative moe" "rme" (:results data) fastest]
          [create-row "Standard moe" "sem" (:results data) fastest]]]]

       )]))

(defn mount-root
  []
  (reagent/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (mount-root))

(init!)
