(ns metabase.sync-database.build-reports
  "Functions which handle the import of default reports of OTMM or Wem Audit."
  (:require [cheshire.core :as json]
            [clojure.math.numeric-tower :as math]
            [clojure.string :as s]
            [clojure.tools.logging :as log]
            [metabase
             [events :as events]
             [util :as u]]
            [metabase.db.metadata-queries :as queries]
            [metabase.models
             [card :as card :refer [Card]]
             [dashboard :as dashboard :refer [Dashboard]]
             [dashboard-card :as dashboard-card :refer [DashboardCard]]
             [field :as field :refer [Field]]
             [table :as table :refer [Table]]]
            [toucan
             [db :as db]
             [models :as models]]))

;TODO remove/hide this
(def ^:private ^:const events-wem ["contenttypecreate" "contenttypemodify" "contenttypepublish" "contenttypedelete" "contentinstancemodify" "contentinstancecreate"
  "contentinstancedelete" "contentinstancepublish" "contentinstanceunpublish" "contentinstanceread" "contentstaticfilecreate" "contentstaticfilemodify""contentstaticfiledelete" "contentstaticfilepublish" "contentstaticfileunpublish" "contentstaticfileread" "contentchannelcreate" "contentchannelmodify"
  "contentchanneldelete" "contentchannelpublish" "contentchannelunpublish" "contentsitecreate" "contentsitemodify" "contentsitedelete"
  "contentsitepublish" "contentsiteunpublish" "operationrolemodifyaddcapability" "operationrolemodifydelcapability" "operationrolemodifydeluser"
  "operationrolemodifyadduser" "operationrolemodifyaddgroup" "operationrolemodifydelgroup" "operationrolecreate" "operationloginsuccess"
  "operationroledelete" "operationloginfailure" "operationconfigvarmodify" "operationconfigvarcreate" "operationconfigvardelete" "operationsearchsimple"
  "operationconfigcommit" "operationsearchadvanced" "workflowactivitystart" "workflowactivitydecline" "workflowactivityaccept" "workflowactivityterminate"
  "workflowactivitycomplete" "workflowactivityabort" "workflowjobpublish" "workflowjobunpublish" "workflowstart" "workflowcomplete" "workflowterminate"
  "workflowabort" "job" "activity" "workflow" "search" "var" "config" "login" "modify" "role" "operation" "site" "channel" "staticfile" "instance"
  "type" "content"])

(def ^:private ^:const events-otmm [])

(defn ^:private get-dataset-query
  "Default query string according to table/user/date id fields"
  [table-id username-id date-id]
  {:pre [(integer? table-id)
         (integer? username-id)
         (integer? date-id)]}
    {:table (json/generate-string {:database 1 :type "query" :query {:source_table table-id}})
     :pie   (json/generate-string {:database 1 :type "query"
                                   :query {:source_table table-id
                                           :aggregation [["count"]]
                                           :breakout [["field-id" username-id]]}})
     :line  (json/generate-string {:database 1 :type "query"
                                   :query {:source_table table-id
                                           :breakout [["field-id" username-id]
                                                      ["datetime-field" ["field-id", date-id] "day"]]
                                           :aggregation [["count"]]}})})

(def ^:private ^:const results-metadata {
  :table ""
  :pie (json/generate-string [{:base_type "type/Text" :display_name "Username" :name "username"}
                              {:base_type "type/Integer" :display_name "count" :name "count" :special_type "type/Number"}])
  :line (json/generate-string [{:base_type "type/Text" :display_name "Username" :name "username"}
                               {:base_type "type/Integer" :display_name "count" :name "count" :special_type "type/Number"}
                               {:base_type "type/DateTime" :display_name "Event Date" :name "event_date" :unit "day"}])})

(defn ^:private ^:const get-parameters-dash
  [param-date param-usr]
  [{:name "Date Filter" :slug "date_filter" :id param-date :type "date/all-options"}
   {:name "Username" :slug "username" :id param-usr :type "category"}])


(defn ^:private get-parameters-dashcard
  [param-usr param-date card-id username-id date-id]
  [{:parameter_id param-date :card_id card-id :target [:dimension [:field-id date-id]]}
   {:parameter_id param-usr :card_id card-id :target [:dimension [:field-id username-id]]}])

(defn ^:private insert-dash-card
  "Add a DashBoardCard to db"
  [size-x size-y row col card-id dash-id username-id date-id param-usr param-date]
  (->> (db/insert! DashboardCard {
                   :sizeX size-x            :sizeY size-y
                   :row row                 :col col
                   :card_id card-id         :dashboard_id dash-id
                   :parameter_mappings (get-parameters-dashcard param-usr param-date card-id username-id date-id)
                   :visualization_settings {}})))

(defn ^:private insert-card
  "Add a Card to db"
  [user-id event display query table-id result]
  {:pre [(integer? user-id) (string? event)
         (string? display)  (string? query)
         (integer? table-id) (string? result)]}
  (->> (db/insert! Card {
                   :name event          :display display
                   :dataset_query query :visualization_settings "{}"
                   :creator_id user-id  :database_id 1
                   :table_id table-id   :query_type "query"
                   :archived false      :enable_embedding false
                   :collection_id 1     :result_metadata result})
       (events/publish-event! :card-create)))

(defn ^:private insert-dash
  "Add a DashBoard to db"
  [event user-id]
  {:pre [(integer? user-id)
         (string? event)]}
  (->> (db/insert! Dashboard {
                   :name event              :creator_id user-id
                   :parameters []           :show_in_getting_started false
                   :enable_embedding false  :archived false})
       (events/publish-event! :dashboard-create)))

(defn ^:private get-field-id
  [name table-id]
  (db/select-one-field :id Field :name name :table_id table-id))

(defn build
  [user-id]
  (when-not (db/exists? Dashboard :id 1)
    (doseq [event (seq events-wem)]
      (let [dash-id     (u/get-id (insert-dash event user-id))
            table-id    (u/get-id (db/select-one Table :name event))
            date-id     (get-field-id "event_date" table-id)
            username-id (get-field-id "username" table-id)
            data        (get-dataset-query table-id username-id date-id)]
            (let [card-table-id     (u/get-id (insert-card user-id event "table" (data :table) table-id (results-metadata :table)))
                  card-pie-id       (u/get-id (insert-card user-id event "pie"   (data :pie)   table-id (results-metadata :pie)))
                  card-line-id      (u/get-id (insert-card user-id event "line"  (data :line)  table-id (results-metadata :line)))]
              (let [param-usr (Long/toString (math/floor (* (rand) (math/expt 2 32))) 16)
                    param-date (Long/toString (math/floor (* (rand) (math/expt 2 32))) 16)]
                  (insert-dash-card 12 7 0 0 card-table-id dash-id username-id date-id param-usr param-date)
                  (insert-dash-card 6 7 0 12 card-pie-id dash-id username-id date-id param-usr param-date)
                  (insert-dash-card 18 5 7 0 card-line-id dash-id username-id date-id param-usr param-date)
                  (->> (let [current-param (db/select-one-field :parameters Dashboard :id dash-id)
                             new-param (concat current-param (get-parameters-dash param-date param-usr))]
                         (db/update! Dashboard dash-id :parameters new-param)))))))
    (log/info (u/format-color 'magenta "Default Dashboards imported"))))


