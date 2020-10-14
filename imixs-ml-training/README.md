# Imixs-ML - API

The Imixs-ML API Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. The service provides an OpenAPI-UI to interact with the service from your web browser:

	http://localhost:8080/api/openapi-ui/


# Build and Run

The Imixs-ML API Service is build with Docker and you can deploy it into any container environment like Kubernetes or Docker Swarm. 

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
		   <!-- use a mapping for optional items names -->
			<value xsi:type="xs:string">_iban|cdtr.iban</value>
			<value xsi:type="xs:string">_bic|cdtr.bic</value>
			<value xsi:type="xs:string">_invoicetotal|invoice.total</value>
			<value xsi:type="xs:string">_invoicenumber|invoice.number</value>
		</item>
		<item name="workflow.locale">
			<value xsi:type="xs:string">UK</value>
			<value xsi:type="xs:string">DE</value>
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
The itemname is equal to the name identifying the entity within a ml-model (e.g. spaCy). In case the itemname provided in a workitem does not match the entity name used in a model you can adapt the name with a | character.

	 
	<value xsi:type="xs:string">_capacity|invoice.total</value>

This example maps the item '_capacity' to the entity 'invoice.total'.

### The Training Data Quality

The training data quality depends on the entities found in the content of a workitem.  There are the following training data quality levels defined:

 - FULL - all training items in the workitem have a value and all values are part of the traingData. This means a 100% match.
 - PARTIAL - not all training items in the workitem have a value, but all values are part of the  traingData. This means the workitem data has a partial  match.
 - BAD - not all training item values of the workitem are part of the traingData.  This means the training object has a bad quality and can not be used for training

The requested quality level for a training data set can be defined with the config item "ml.training.quality"

	....
	<!-- ML Quality Level (FULL|PARTIAL) -->
	<item name="ml.training.quality"><value xsi:type="xs:string">FULL</value></item>
	....

**Note:** It is recommended to set the training quality to 'FULL' which is the default level. The training quality level 'PARTIAL' can lead to a lesser quality of the ML model. 


## The Testing Mode

You can also test an existing model. The Imixs-ML Training service provides the Rest Resource */testing/*. This resource expects a POST request with the following XML payload:

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
		
		<!-- Tika OCR Server PDF_ONLY|OCR_ONLY|MIXED -->
		<item name="tika.ocrmode"><value xsi:type="xs:string">MIXED</value></item>
		<item name="tika.options">
			<value xsi:type="xs:string">X-Tika-PDFocrStrategy=OCR_AND_TEXT_EXTRACTION</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrImageType=RGB</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrDPI=400</value>
		</item>
		
		<!-- ML spaCy Server -->
		<item name="ml.analyse.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/analyse</value></item>
	</document>


In this mode, the service reads documents stored in a workflow instance and performs a OCR Scan. The extracted text is than send for analyzing to the Imixs-ML Service. The results are printed into the log file. 
You can use this mode to verify your current model.
