# Imixs-ML - Training Service

The Imixs-ML Training Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. The service provides an OpenAPI-UI to interact with the service from your web browser:

	http://localhost:8080/api/openapi-ui/


## The TrainingData Object

To train a ml-model an array of TraingData objects can be sent to the API resource /trainingdata/

A training data object array is defined by the following JSON structure:

	[
	  {
	    "text": "String",
	    "entities": [
	      {
	        "label": "string",
	        "start": 0,
	        "stop": 0
	      }
	    ]
	  }
	]
	
Each Training Data object contains a *text* and a list of *entity* objects. A single entity object is defined by its label and the start/stop position within the training text. So it is possible to define multiple entity objects for one training text. See the following example:


	[
	  {
	    "text": "Imixs Workflow in an open source project organized from Munich.",
	    "entities": [
	      {
	        "label": "project",
	        "start": 0,
	        "stop": 14
	      },
	      {
	        "label": "city",
	        "start": 56,
	        "stop": 62
	      }
	    ]
	  }
	]
  


## The AnalyseData Object

To analyse a text based on a ml-model a AnalyseData object can be send to the API resource /analyse/

An AnaylseData object has the following JSON structure:

	{
	  "text": "string"
	}


## The Training Mode

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


### The Entity List

The property 'entities' contains a list of item names to be taken form the workitems to be analzed.
The itemname is equal to the name identifying the entity within a spaCy model. In case the itemname provided in a workitem does not match the entity name used in a model you can adapt the name with a | character.

	 
	<entities>_capacity|_invoicetotal,_invoicenumber</entities>

This example maps the item '_capacity' to the entity '_invoicetotal'.



## The Testing Mode

You can also test an existing model. The Imixs-ML Training service provides the Rest Resource */testing/*. This resource expects a POST request with the following XML payload:

	<?xml version="1.0" encoding="UTF-8"?>
	<XMLConfig>
		<target>string</target>
		<user>string</user>
		<password>string</password>
		<query>string</query>
		<pagesize>0</pagesize>
		<entities>string</entities>
	</XMLConfig>


In this mode, the service reads documents stored in a workflow instance and performs a OCR Scan. The extracted text is than send for analyzing to the Imixs-ML Service. The results are printed into the log file. 
You can use this mode to verify your current model.


# The EntityAdapter

The EntityAdapter can be used to  adapt an entity with alternative fromating values.  This adapter is useful if a string constant can ocure in different formats like a currency. For example the float value 1500.00 can have different presentations in a textfragment:


	1500.00
	1500,00
	1.500,00
	1,500.00
	
The TrainingService implements an Observer Pattern based on CDI Events to call registered EntityAdapters
The Event is defined by the class:

	org.imixs.ml.service.AnalyzeEntityEvent




# Build and Run


To build the Imixs-ML Training service just build the corresponding Docker containers:

	$ mvn clean install -Pdocker


## Run and Test

To start the dev environment run docker-compose:

	$ docker-compose up
 
	
You can access the adapter service at:

	http://localhost:8080/
	
Run the OpenAPI UI:	

	http://localhost:8080/api/openapi-ui/
	


