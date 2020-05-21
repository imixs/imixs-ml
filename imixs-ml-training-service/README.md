# Imixs-ML - Training Service

The Imixs-ML Training Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. The service provides an OpenAPI-UI to interact with the service from your web browser:

	http://localhost:8080/api/openapi-ui/

To train a new model the Imixs-ML Training service provides the Rest Resource */training/*. This resource expects a POST request with the following XML payload:

	<?xml version="1.0" encoding="UTF-8"?>
	<XMLConfig>
		<target>string</target>
		<user>string</user>
		<password>string</password>
		<query>string</query>
		<pagesize>0</pagesize>
		<entities>string</entities>
	</XMLConfig>

This is an example how to use the training service:


	<?xml version="1.0" encoding="UTF-8"?>
	<config>
		<target>http://localhost:8080/api/</target>
		<user>admin</user>
		<password>adminadmin</password>
		<query>($workflowgroup:"Invoice") AND ($taskid:5900)</query>
		<pagesize>100</pagesize>
		<entities>_iban,_bic,_name,_capacity,_invoicenumber</entities>
	</config>




## Build the ML Training Service

To build the training service just build the corresponding Docker containers:

	$ mvn clean install -Pdocker


## Run and Test

To start the dev environment run docker-compose:

	$ docker-compose up
 
	
You can access the adapter service at:

	http://localhost:8080/
	
Run the OpenAPI UI:	

	http://localhost:8080/api/openapi-ui/
	
	

