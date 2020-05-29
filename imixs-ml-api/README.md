# Imixs-ML - API

The Imixs-ML API Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. The service provides an OpenAPI-UI to interact with the service from your web browser:

	http://localhost:8080/api/openapi-ui/




# Build and Run

The Imixs-ML API Service is build with Docker and you can deploy it inot any contaienr environment like Kubernetes or Docker Swarm. 

To build the Imixs-ML API service just build the corresponding Docker containers:

	$ mvn clean install -Pdocker

## Run and Test

To start the dev environment run docker-compose:

	$ docker-compose up
 
You can access the adapter service at:

	http://localhost:8080/
	
Run the OpenAPI UI:	

	http://localhost:8080/api/openapi-ui/
	





## The Training Mode

To train a new model the Imixs-ML Training service provides the Rest Resource */training/*. This resource expects a POST request with an Imixs XMLDocument providing the following XML payload:

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:xs="http://www.w3.org/2001/XMLSchema">
		
		<!-- Workflow Instance -->
		<item name="workflow.endpoint"><value xsi:type="xs:string">http://localhost:8080/api/</value></item>
		<item name="workflow.userid"><value xsi:type="xs:string">admin</value></item>
		<item name="workflow.password"><value xsi:type="xs:string">...</value></item>
		<item name="workflow.query"><value xsi:type="xs:string">($workflowgroup:"Invoice") AND ($taskid:5900)</value></item>
		<item name="workflow.pagesize"><value xsi:type="xs:int">100</value></item>
		<item name="workflow.pageindex"><value xsi:type="xs:int">0</value></item>
		
		<item name="workflow.entities">
			<value xsi:type="xs:string">_iban</value>
			<value xsi:type="xs:string">_bic</value>
			<value xsi:type="xs:string">_invoicetotal</value>
			<value xsi:type="xs:string">_invoicenumber</value>
		</item>
		
		<!-- Tika OCR Server PDF_ONLY|OCR_ONLY|MIXED -->
		<item name="tika.ocrmode"><value xsi:type="xs:string">MIXED</value></item>
		<item name="tika.options">
			<value xsi:type="xs:string">X-Tika-PDFocrStrategy=OCR_AND_TEXT_EXTRACTION</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrImageType=RGB</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrDPI=400</value>
		</item>
		
		<!-- ML spaCy Server -->
		<item name="ml.training.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/training</value></item>
		<item name="ml.analyse.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/analyse</value></item>
	</document>


### OCR Options

During the training mode the TrainingService run a OCR on documents attached to a workitem. The ocr is processed by Apache Tika.
Out of the box, Apache Tika will start with the default configuration. By providing additional config options you can specify a custom tika configuration to be used by the tika server. These options can be set in the config item 'tika.options' and 'tika.ocrmode'.

Find more details [here](https://github.com/imixs/imixs-archive/tree/master/imixs-archive-documents#the-tikadocumentservice).


### The Entity List

The property 'entities' contains a list of item names to be taken form the workitems to be analzed.
The itemname is equal to the name identifying the entity within a spaCy model. In case the itemname provided in a workitem does not match the entity name used in a model you can adapt the name with a | character.

	 
	<value xsi:type="xs:string">_capacity|_invoicetotal</value>

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


