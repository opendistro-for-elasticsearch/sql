#  Connecting Open Distro For ElasticSearch to Microsoft Power BI Service

## Setup
* Download and Install [On-premises data gateway](https://docs.microsoft.com/en-us/data-integration/gateway/service-gateway-install)
* Change the path for custom data connector folder in On-premises data gateway so that the gateway can find the custom connector.
> NOTE: Ensure the gateway service account (**PBIEgwService**) has permissions to access the custom connector folder. Alternatively, you can copy connector file to `C:\Windows\ServiceProfiles\PBIEgwService\Documents\Power BI Desktop\Custom Connectors\`.

<img src="img/pbi_gateway_connector_path.png" width=500>

* Verify the status of data gateway is ready to be used.

<img src="img/pbi_gateway_status.png" width=500>

* Login to Power BI Service.
* Click on **Setting** > **Manage Gateway**.

<img src="img/pbi_service_setting.png" width=400>

* Select **Allow user's custom data connectors to refresh through this gateway cluster(preview)**. Click on **Apply**.

<img src="img/pbi_service_cluster_setting.png">

* Click on **Add data sources to use the gateway**.
* Select Data Source Type as **Open Distro For Elasticsearch**.
* Enter Data Source Name and Server values.
* Select required **Authentication Method**. Select **Anonymous** for auth **NONE**.
For **AWS_SIGV4**, select **Key** and set aws access credentials for user **PBIEgwService** at path `C:\Windows\ServiceProfiles\PBIEgwService\.aws\`

* Select Encryption mode for connection.
* Click on **Add**.

<img src="img/pbi_service_data_source.png" width=650>

* You will get a **Connection Successful** message.

<img src="img/pbi_service_data_source_success.png" width=650>


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

## Dataset Scheduled Refresh

* Click on **Settings** > **Datasets**.
* Select required Gateway and click on **Apply**.

<img src="img/pbi_service_dataset_gateway.png" width=800>

* Turn on Keep your data up to date option.
* Select refresh frequency and timezone.
* Add email for failure notifications if required.
* Click on **Apply**.

<img src="img/pbi_service_dataset_refresh.png" width=550>

* You can also check history by clicking on **Refresh history**.

<img src="img/pbi_service_dataset_refresh_history.png" width=550>