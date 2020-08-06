#  Connecting Open Distro For ElasticSearch to Microsoft Power BI Service

## Setup
* Download and Install [On-premises data gateway](https://docs.microsoft.com/en-us/data-integration/gateway/service-gateway-install)
* Change the path for custom data connector folder in On-premises data gateway so that the gateway can find the custom connector.
> NOTE: Ensure the gateway service account has permissions to access the custom connector folder. 

<img src="img/pbi_gateway_connector_path.png" width=500>

* Verify the status of data gateway is ready to be used.

<img src="img/pbi_gateway_status.png" width=500>

* Login to Power BI Service.
* Click on **Setting** > **Manage Gateway**.

<img src="img/pbi_service_setting.png" width=400>

* Select **Allow user's custom data connectors to refresh through this gateway cluster(preview)**. Click on **Apply**.

<img src="img/pbi_service_cluster_setting.png">

* Add data source to use the gateway.


## Publish Report

* Follow [instructions](./power_bi_support.md) to create graph using Open Disto For Elasticsearch Data connector.
* Click on **Publish** to publish the report on Power BI service.

<img src="img/pbi_publish_report.png">

* Select destination and click on **Select**.

<img src="img/pbi_select_workspace.png" width=400>

* You will get a success message when report is published.

<img src="img/pbi_publish_status.png" width=400>

* Click on **Open '%report name%' in Power BI** to open published report in Power BI service.

## Modify report using Power BI Service

* Click on **Edit report** to modfify report.

<img src="img/pbi_service_edit_reoprt.png">

* Use **Filters**,**Visualizations** and **Fields** to modify report.

<img src="img/pbi_service_modified_report.png">
