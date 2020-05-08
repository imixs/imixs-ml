# Imixs-ML - Training Service


The Imixs-ML Trainging Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. 

To train a new model you can use the ModelGenarator. This class expects a properties file named 'training.properties' placed in the context root directory (src/test/resources/training.properties).

The file provides the following data:



	target.url=http://localhost:8080/
	target.user=admin
	target.password=adminadmin
	
	target.query=($workflowgroup:"Rechnungseingang") AND ($taskid=5900)
	target.query.maxcount=100



## Build the ML Training Service

To build the training service just build the corresponding Docker containers:

	$ mvn clean install -Pdocker


## Run and Test

To start the dev environment run docker-compose:

	$ docker-compose up
 
	
You can access the adapter service at:

	http://localhost:9080/
	
Run the OpenAPI UI:	

	http://localhost:9080/openapi/ui/
	
	

