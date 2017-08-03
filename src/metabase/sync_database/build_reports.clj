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
             [collection :as collection :refer [Collection]]
             [dashboard :as dashboard :refer [Dashboard]]
             [dashboard-card :as dashboard-card :refer [DashboardCard]]
             [field :as field :refer [Field]]
             [table :as table :refer [Table]]]
            [toucan
             [db :as db]
             [models :as models]]))

;TODO remove/hide this

(def ^:private ^:const events-wem [{:name "ContentTypeCreate" :event "ContentTypeCreate"} {:name "ContentTypeModify" :event "ContentTypeModify"} {:name "ContentTypePublish" :event "ContentTypePublish"} {:name "ContentTypeDelete" :event "ContentTypeDelete"} {:name "ContentInstanceModify" :event "ContentInstanceModify"} {:name "ContentInstanceCreate" :event "ContentInstanceCreate"} {:name "ContentInstanceDelete" :event "ContentInstanceDelete"} {:name "ContentInstancePublish" :event "ContentInstancePublish"} {:name "ContentInstanceUnpublish" :event "ContentInstanceUnpublish"} {:name "ContentInstanceRead" :event "ContentInstanceRead"} {:name "ContentStaticFileCreate" :event "ContentStaticFileCreate"} {:name "ContentStaticFileModify" :event "ContentStaticFileModify"} {:name "ContentStaticFileDelete" :event "ContentStaticFileDelete"} {:name "ContentStaticFilePublish" :event "ContentStaticFilePublish"} {:name "ContentStaticFileUnpublish" :event "ContentStaticFileUnpublish"} {:name "ContentStaticFileRead" :event "ContentStaticFileRead"} {:name "ContentChannelCreate" :event "ContentChannelCreate"} {:name "ContentChannelModify" :event "ContentChannelModify"} {:name "ContentChannelDelete" :event "ContentChannelDelete"} {:name "ContentChannelPublish" :event "ContentChannelPublish"} {:name "ContentChannelUnpublish" :event "ContentChannelUnpublish"} {:name "ContentSiteCreate" :event "ContentSiteCreate"} {:name "ContentSiteModify" :event "ContentSiteModify"} {:name "ContentSiteDelete" :event "ContentSiteDelete"} {:name "ContentSitePublish" :event "ContentSitePublish"} {:name "ContentSiteUnpublish" :event "ContentSiteUnpublish"} {:name "OperationRoleModifyAddCapability" :event "RoleModifyAddCapability"} {:name "OperationRoleModifyDelCapability" :event "RoleModifyDelCapability"} {:name "OperationRoleModifyDelUser" :event "OperationRoleModifyDelUser"} {:name "OperationRoleModifyAddUser" :event "OperationRoleModifyAddUser"} {:name "OperationRoleModifyAddGroup" :event "OperationRoleModifyAddGroup"} {:name "OperationRoleModifyDelGroup" :event "OperationRoleModifyDelGroup"} {:name "OperationRoleCreate" :event "OperationRoleCreate"} {:name "OperationLoginSuccess" :event "OperationLoginSuccess"} {:name "OperationRoleDelete" :event "OperationRoleDelete"} {:name "OperationLoginFailure" :event "OperationLoginFailure"} {:name "OperationConfigVarModify" :event "OperationConfigVarModify"} {:name "OperationConfigVarCreate" :event "OperationConfigVarCreate"} {:name "OperationConfigVarDelete" :event "OperationConfigVarDelete"} {:name "OperationSearchSimple" :event "OperationSearchSimple"} {:name "OperationConfigCommit" :event "OperationConfigCommit"} {:name "OperationSearchAdvanced" :event "OperationSearchAdvanced"} {:name "WorkFlowActivityStart" :event "WorkFlowActivityStart"} {:name "WorkFlowActivityDecline" :event "WorkFlowActivityDecline"} {:name "WorkFlowActivityAccept" :event "WorkFlowActivityAccept"} {:name "WorkFlowActivityTerminate" :event "WorkFlowActivityTerminate"} {:name "WorkFlowActivityComplete" :event "WorkFlowActivityComplete"} {:name "WorkFlowActivityAbort" :event "WorkFlowActivityAbort"} {:name "WorkFlowJobPublish" :event "WorkFlowJobPublish"} {:name "WorkFlowJobUnpublish" :event "WorkFlowJobUnpublish"} {:name "WorkFlowStart" :event "WorkFlowStart"} {:name "WorkFlowComplete" :event "WorkFlowComplete"} {:name "WorkFlowTerminate" :event "WorkFlowTerminate"} {:name "WorkFlowAbort" :event "WorkFlowAbort"} {:name "WorkFlowJob" :event "Job"} {:name "WorkFlowActivity" :event "Activity"} {:name "WorkFlow" :event "WorkFlow"} {:name "OperationSearch" :event "Search"} {:name "OperationConfigVar" :event "Var"} {:name "OperationConfig" :event "Config"} {:name "OperationLogin" :event "Login"} {:name "OperationRoleModify" :event "Modify"} {:name "OperationRole" :event "Role"} {:name "Operation" :event "Operation"} {:name "ContentSite" :event "Site"} {:name "ContentChannel" :event "Channel"} {:name "ContentStaticFile" :event "StaticFile"} {:name "ContentInstance" :event "Instance"} {:name "ContentType" :event "Type"} {:name "Content" :event "Content"} ])
(def ^:private ^:const events-otmm [{:name "OperationLoginSuccess" :event "OperationLoginSuccess"} {:name "AdminRecyclebinAssetPurge" :event "AdminRecyclebinAssetPurge"} {:name "ContentAssetModifySecurityPolicies" :event "AssetModifySecurityPolicies"} {:name "AdminFolderTypeDelete" :event "AdminFolderTypeDelete"} {:name "AdminSecurityPolicyAdministratorsRemove" :event "PolicyAdministratorsRemove"} {:name "OperationLoginFailure" :event "OperationLoginFailure"} {:name "ContentAssetModifyCategories" :event "ContentAssetModifyCategories"} {:name "ContentFolderCreate" :event "ContentFolderCreate"} {:name "AdminUserCreate" :event "AdminUserCreate"} {:name "AdminCategoryRootCreate" :event "AdminCategoryRootCreate"} {:name "OperationSearchSimple" :event "OperationSearchSimple"} {:name "ContentAssetDelete" :event "ContentAssetDelete"} {:name "ContentFolderRename" :event "ContentFolderRename"} {:name "AdminUserModify" :event "AdminUserModify"} {:name "AdminCategoryCreate" :event "AdminCategoryCreate"} {:name "OperationSearchAdvanced" :event "OperationSearchAdvanced"} {:name "ContentAssetUndelete" :event "ContentAssetUndelete"} {:name "ContentFolderModifyMetadata" :event "ContentFolderModifyMetadata"} {:name "AdminUserDelete" :event "AdminUserDelete"} {:name "CreativeReviewReviewCreate" :event "CreativeReviewReviewCreate"} {:name "AdminCategoryModify" :event "AdminCategoryModify"} {:name "CreativeReviewReviewerCompleteTask" :event "ReviewReviewerCompleteTask"} {:name "OperationSearchExpert" :event "OperationSearchExpert"} {:name "CreativeReviewReviewerReviewAssets" :event "ReviewReviewerReviewAssets"} {:name "CreativeReviewReviewClose" :event "CreativeReviewReviewClose"} {:name "CreativeReviewAssetsRemove" :event "CreativeReviewAssetsRemove"} {:name "ContentAssetCheckout" :event "ContentAssetCheckout"} {:name "ContentFolderUnsubscribe" :event "ContentFolderUnsubscribe"} {:name "AdminGroupCreate" :event "AdminGroupCreate"} {:name "ContentFolderModifySecurityPolicies" :event "FolderModifySecurityPolicies"} {:name "CreativeReviewAssetsAdd" :event "CreativeReviewAssetsAdd"} {:name "CreativeReviewReviewerUpdate" :event "CreativeReviewReviewerUpdate"} {:name "AdminCategoryDelete" :event "AdminCategoryDelete"} {:name "ContentAssetDownload" :event "ContentAssetDownload"} {:name "OperationSearchSave" :event "OperationSearchSave"} {:name "ContentAssetPreview" :event "ContentAssetPreview"} {:name "CreativeReviewReviewerApprovalUpdate" :event "ReviewReviewerApprovalUpdate"} {:name "CreativeReviewReviewerApprovalSet" :event "ReviewReviewerApprovalSet"} {:name "ContentAssetCancelCheckout" :event "ContentAssetCancelCheckout"} {:name "AdminGroupModify" :event "AdminGroupModify"} {:name "CreativeReviewReviewerAdd" :event "CreativeReviewReviewerAdd"} {:name "CreativeReviewReviewDelete" :event "CreativeReviewReviewDelete"} {:name "ContentFolderDelete" :event "ContentFolderDelete"} {:name "ContentFolderSubscribe" :event "ContentFolderSubscribe"} {:name "AdminCategoryThesaurusLoad" :event "AdminCategoryThesaurusLoad"} {:name "CreativeReviewReviewerRemove" :event "CreativeReviewReviewerRemove"} {:name "CreativeReviewReviewStart" :event "CreativeReviewReviewStart"} {:name "CreativeReviewReviewerDeclineTask" :event "ReviewReviewerDeclineTask"} {:name "OperationSearchRunSaved" :event "OperationSearchRunSaved"} {:name "CreativeReviewReviewUpdate" :event "CreativeReviewReviewUpdate"} {:name "CreativeReviewReviewerSendReminder" :event "ReviewReviewerSendReminder"} {:name "ContentAssetBulkModify" :event "ContentAssetBulkModify"} {:name "AdminGroupDelete" :event "AdminGroupDelete"} {:name "ContentAssetCheckin" :event "ContentAssetCheckin"} {:name "ContentFolderUnDelete" :event "ContentFolderUnDelete"} {:name "AdminCategoryThesaurusExport" :event "AdminCategoryThesaurusExport"} {:name "AdminSecurityPolicyCreate" :event "AdminSecurityPolicyCreate"} {:name "AdminRoleCreate" :event "AdminRoleCreate"} {:name "ContentAssetSubscribe" :event "ContentAssetSubscribe"} {:name "ContentFolderRemove" :event "ContentFolderRemove"} {:name "AdminTemplateCreate" :event "AdminTemplateCreate"} {:name "AdminSecurityPolicyModify" :event "AdminSecurityPolicyModify"} {:name "AdminRoleModify" :event "AdminRoleModify"} {:name "ContentAssetUnsubscribe" :event "ContentAssetUnsubscribe"} {:name "ContentAssetImport" :event "ContentAssetImport"} {:name "AdminMetadataConfigurationSave" :event "AdminMetadataConfigurationSave"} {:name "AdminSecurityPolicyDelete" :event "AdminSecurityPolicyDelete"} {:name "AdminRoleDelete" :event "AdminRoleDelete"} {:name "ContentAssetLink" :event "ContentAssetLink"} {:name "AdminMetadataConfigurationApply" :event "MetadataConfigurationApply"} {:name "ContentAssetExport" :event "ContentAssetExport"} {:name "AdminSecurityPolicyAdministratorsAdd" :event "PolicyAdministratorsAdd"} {:name "AdminFolderTypeCreate" :event "AdminFolderTypeCreate"} {:name "ContentAssetRemove" :event "ContentAssetRemove"} {:name "AdminRecyclebinAssetUndelete" :event "AdminRecyclebinAssetUndelete"} {:name "ContentAssetModifyMetadata" :event "ContentAssetModifyMetadata"} {:name "AdminSecurityPolicyPermissionsChange" :event "PolicyPermissionsChange"} {:name "AdminFolderTypeModify" :event "AdminFolderTypeModify"} {:name "ApprovalCreativeReviewReviewerApprova" :event "Approval"} {:name "CreativeReviewReviewer" :event "Reviewer"} {:name "CreativeReviewAssets" :event "Assets"} {:name "CreativeReview" :event "Review"} {:name "CreativeReviewAssets" :event "CreativeReview"} {:name "AdminRecyclebin" :event "Recyclebin"} {:name "AdminMetadataConfiguration" :event "MetadataConfiguration"} {:name "AdminTemplate" :event "Template"} {:name "AdminCategory" :event "Category"} {:name "AdminFolderType" :event "FolderType"} {:name "AdminRole" :event "Role"} {:name "AdminGroup" :event "Group"} {:name "AdminUser" :event "User"} {:name "AdminSecurityPolicy" :event "SecurityPolicy"} {:name "Admin" :event "Admin"} {:name "OperationSearch" :event "Search"} {:name "OperationLogin" :event "Login"} {:name "Operation" :event "Operation"} {:name "ContentAsset" :event "Asset"} {:name "ContentFolder" :event "Folder"} {:name "Content" :event "Content"}])
(def ^:private ^:const database-id 1)

(defn ^:private get-dataset-query
  "Default query string according to table/user/date id fields"
  [table-id username-id date-id]
  {:pre [(integer? table-id)
         (integer? username-id)
         (integer? date-id)]}
    {:table (json/generate-string {:database database-id :type "query" :query {:source_table table-id}})
     :pie   (json/generate-string {:database database-id :type "query"
                                   :query {:source_table table-id
                                           :aggregation [["count"]]
                                           :breakout [["field-id" username-id]]}})
     :line  (json/generate-string {:database database-id :type "query"
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
                   :creator_id user-id  :database_id database-id
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

(defn ^:private insert-collection
   "Add a Table to db" 
   [name]
   {:pre [(string? name)]}
   (->> (db/insert! Collection {
                    :name name          :slug name   :description "Default questions collection" 
                    :color "#509EE3"    :archived false  })))


(defn build
  [database-id user-id]
  (when-not (or (db/exists? Dashboard :id 1) (= database-id 2))
    (insert-collection "default")
    (doseq [event (seq events-wem)]
      (let [dash-id     (u/get-id (insert-dash (event :name) user-id))
            table-id    (u/get-id (db/select-one Table :name (event :event)))
            date-id     (get-field-id "event_date" table-id)
            username-id (get-field-id "username" table-id)
            data        (get-dataset-query table-id username-id date-id)]
            (let [card-table-id     (u/get-id (insert-card user-id (str (event :name)) "table" (data :table) table-id (results-metadata :table)))
                  card-pie-id       (u/get-id (insert-card user-id (str (event :name) ", Grouped by Username") "pie"   (data :pie)   table-id (results-metadata :pie)))
                  card-line-id      (u/get-id (insert-card user-id (str (event :name) ", Grouped by Username and Event Date (day)") "line"  (data :line)  table-id (results-metadata :line)))]
              (let [param-usr (Long/toString (math/floor (* (rand) (math/expt 2 32))) 16)
                    param-date (Long/toString (math/floor (* (rand) (math/expt 2 32))) 16)]
                  (insert-dash-card 12 7 0 0 card-table-id dash-id username-id date-id param-usr param-date)
                  (insert-dash-card 6 7 0 12 card-pie-id dash-id username-id date-id param-usr param-date)
                  (insert-dash-card 18 5 7 0 card-line-id dash-id username-id date-id param-usr param-date)
                  (->> (let [current-param (db/select-one-field :parameters Dashboard :id dash-id)
                             new-param (concat current-param (get-parameters-dash param-date param-usr))]
                         (db/update! Dashboard dash-id :parameters new-param))))))
    (log/info (u/format-color 'magenta "Default Dashboards imported")))))


