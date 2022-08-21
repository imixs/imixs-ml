# Imixs-ML - Training API

The Imixs-ML API Service is a microservice to train a ML Model based on the workflow content of an Imixs-Office-Workflow instance. The service provides an OpenAPI-UI to interact with the service from your web browser:

	http://localhost:8080/api/openapi-ui/

The service proivdes 3 endpoints:

 - training/ - trains a training data and updates the model
 - validate/ - validates a training data set without updating the model
 - analyse/ - analyzes test data with a given model

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
			<value xsi:type="xs:string">en_GB</value>
			<value xsi:type="xs:string">en_US</value>		
			<value xsi:type="xs:string">de_DE</value>
		</item>
		
		<!-- Tika OCR Server PDF_ONLY|OCR_ONLY -->
		<item name="tika.ocrmode"><value xsi:type="xs:string">OCR_ONLY</value></item>
		<item name="tika.options">
			<value xsi:type="xs:string">X-Tika-PDFocrStrategy=OCR_AND_TEXT_EXTRACTION</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrImageType=RGB</value>
			<value xsi:type="xs:string">X-Tika-PDFOcrDPI=400</value>
		</item>
		
		<!-- ML spaCy Server -->
		<item name="ml.training.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/</value></item>
		<item name="ml.training.model"><value xsi:type="xs:string">invoice-de-0.1.0</value></item>
		<item name="ml.training.filepattern"><value xsi:type="xs:string">.pdf|.PDF</value></item>
		<!-- LOW | GOOD -->
		<item name="ml.training.quality"><value xsi:type="xs:string">LOW</value></item>
		<item name="ml.training.iterations"><value xsi:type="xs:string">10</value></item>
		<item name="ml.training.dropoutrate"><value xsi:type="xs:string">0.25</value></item>

		<!-- Query String: min_losses should be between 1 and 0, forgetting_rate 0-100%  -->
		<item name="ml.options">
			<value xsi:type="xs:string">min_losses=0.00001&amp;retrain_rate=25</value>
		</item>
	
	</document>

### ML Settings

**ml.training.endpoint** - defines the service endpoint of the spacy Rest service

**ml.training.model** - the Model to be trained

**ml.training.filepattern** - optional file pattern to filter file types (e.g. pdf documents only)

**ml.training.quality** - training quality - see below

**ml.training.iterations** - number of iterations the training set will be trained (a good value is 10)

**ml.training.dropoutrate** - drop out rate (e.g. 0.25 - means 25% will be filtered out for each iteration)

The Training service runs the trainingSet in iterations. In each iteration the trainingSet is shuffled 	and reduced by a given DropOutRate. This is to avoid the 'memory effect' of an AI model. See details in the spaCy documentation. 


### The Entity List

The property 'entities' contains a list of item names to be taken form the workitems to be analzed.
The itemname is equal to the name identifying the entity within a ml-model (e.g. spaCy). In case the itemname provided in a workitem does not match the entity name used in a model you can adapt the name with a | character.

	 
	<value xsi:type="xs:string">_capacity|invoice.total</value>

This example maps the item '_capacity' to the entity 'invoice.total'.

### The Training Data Quality

The training data quality depends on the entities found in the content of a workitem.  There are the following training data quality levels defined:

 - GOOD - all training items in the workitem have a value and all values are part of the training text. This means a 100% match.
 - LOW - not all training items in the workitem have a value, but all values are part of the training text. This means the workitem data has a partial match.
 - BAD - not all training item values are part of the training text.  This means the training object has a bad quality and can not be used for training
 


The requested quality level for a training data set can be defined with the config item "ml.training.quality"

	....
	<!-- ML Quality Level (GOOD|LOW) -->
	<item name="ml.training.quality"><value xsi:type="xs:string">GOOD</value></item>
	....

**Note:** It is recommended to set the training quality to 'GOOD' which is the default level. The training quality level 'LOW' can lead to a lesser quality of the ML model. 




### OCR Options

During the training mode the TrainingService run a OCR on documents attached to a workitem. The ocr is processed by Apache Tika.
Out of the box, Apache Tika will start with the default configuration. By providing additional config options you can specify a custom tika configuration to be used by the tika server. These options can be set in the config item 'tika.options' and 'tika.ocrmode'.

Find more details [here](https://github.com/imixs/imixs-archive/tree/master/imixs-archive-documents#the-tikadocumentservice).







## The Validation Mode

You can also validate an existing model. The Imixs-ML Training service provides the Rest Resource */validation/*. This resource expects a POST request with the following XML payload:

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
			
		<!-- Tika OCR Server  -->
		<item name="tika.ocrmode"><value xsi:type="xs:string">OCR_ONLY</value></item>
		<item name="tika.options">
			<value xsi:type="xs:string">X-Tika-OCRLanguage=eng+deu</value>
			<value xsi:type="xs:string">X-Tika-PDFocrStrategy=OCR_ONLY</value>		
		</item>
	
		<!-- ML spaCy Server -->
		<item name="ml.validation.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/</value></item>
		<item name="ml.validation.model"><value xsi:type="xs:string">invoice-de-0.1.0</value></item>
		<item name="ml.validation.filepattern"><value xsi:type="xs:string">.pdf|.PDF</value></item>
	</document>


In this mode, the service reads documents stored in a workflow instance and performs a OCR Scan. The extracted text is than send for analyzing to the Imixs-ML Service. The results are printed into the log file. 
You can use this mode to verify your current model.


# Training Strategies

If you train a complete new empty model you should have enough training data. A minimum of 1000 Training data records is recommended. 

If you retrain the same data set take care about the options `min_losses` and `retrain_rate`. For the beginning, a good setting is 

		<!-- Query String: min_losses should be between 1 and 0, forgetting_rate 0-100%  -->
		<item name="ml.options">
			<value xsi:type="xs:string">min_losses=0.00001&amp;retrain_rate=25</value>
		</item>
	
This means training result above 0.00001 losses will be retrained in any case. Results below will be retrains with chance of 25% 	

If the model is already in production a min_losses of 0.1 can perform better. But do not set the retrain_rate to 0 because this can lead to a 'forgetting' situation where already good training data will be lost over the time. 


## Example

Let's assume you have 1000 Invoice documents already tagged and you want to train a new model. 

For the first Training Phase use 10 iterations with min_losses 0.0 and a retrain rate of 100%

	<!-- Define the training set and taining mode -->
	<item name="workflow.query"><value xsi:type="xs:string">($workflowgroup:"Rechnungseingang" OR $workflowgroup:"Sachrechnung") AND ($taskid:5900)</value></item>	  
	<item name="workflow.pagesize"><value xsi:type="xs:int">1000</value></item>
	<item name="workflow.pageindex"><value xsi:type="xs:int">0</value></item>
	<item name="ml.training.iterations"><value xsi:type="xs:string">10</value></item>
	<item name="ml.training.dropoutrate"><value xsi:type="xs:string">0.0</value></item>
	<item name="ml.options">
		<value xsi:type="xs:string">min_losses=0.0&amp;retrain_rate=100</value>
	</item>


Result Example:


	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 13.778002031959247
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 6.893718251502024
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 5.772998749105051
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 5.382218959718584
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 5.252528165562391
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 4.781865464834507
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 3.5301892510610036
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 3.052076283581273
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 2.6510388435630166
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 2.4029949594783506
	imixs-ml-training_1  | |#]



In the second phase we refine the model by changed the min_losses to 0.1 and retrain rate to 25% which means we retrain the data which is not yet good enough but we also retain good data with a rate of 25% 	
		
	<!-- Define the training set and taining mode -->
	<item name="workflow.query"><value xsi:type="xs:string">($workflowgroup:"Rechnungseingang" OR $workflowgroup:"Sachrechnung") AND ($taskid:5900)</value></item>	  
	<item name="workflow.pagesize"><value xsi:type="xs:int">1000</value></item>
	<item name="workflow.pageindex"><value xsi:type="xs:int">0</value></item>
	<item name="ml.training.iterations"><value xsi:type="xs:string">10</value></item>
	<item name="ml.training.dropoutrate"><value xsi:type="xs:string">0.0</value></item>
	<item name="ml.options">
		<value xsi:type="xs:string">min_losses=0.1&amp;retrain_rate=25</value>
	</item>
	
	
Result Example:
	
	
	imixs-ml-training_1  |   
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 2.0860731714231404
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 2.083436307751995
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.9063005937115136
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.7812667292139812
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.6297307309480873
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.7164011037635025
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 2.0494950737422126
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.6481886264606276
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.6205282350385186
	imixs-ml-training_1  | 
	imixs-ml-training_1  | ......documents trained in total = 1000
	imixs-ml-training_1  |   ......     quality level GOOD = 45.1%  (451)
	imixs-ml-training_1  |   ......      quality level LOW = 54.9%  (549)
	imixs-ml-training_1  |   ......      quality level BAD = 0%  (0)
	imixs-ml-training_1  |   ......            average NER = 1.5447060877300045
	imixs-ml-training_1  | |#]
	
	
## Validation

You can also validate a control dataset which was not yet part of the traiing to verify how good the model is. For this purpose you can call the /validate/ resource with a xml 

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:xs="http://www.w3.org/2001/XMLSchema">
		
		<!-- Workflow Instance -->
		<item name="workflow.endpoint"><value xsi:type="xs:string">https://localhost:8080/api/</value></item>
		<item name="workflow.userid"><value xsi:type="xs:string">admin</value></item>
		<item name="workflow.password"><value xsi:type="xs:string">...</value></item>
	
		<item name="workflow.entities">
			<value xsi:type="xs:string">cdtr.name</value>
			<value xsi:type="xs:string">cdtr.iban</value>
			<value xsi:type="xs:string">cdtr.bic</value>
			<value xsi:type="xs:string">invoice.total</value>
			<value xsi:type="xs:string">invoice.date</value>
			<value xsi:type="xs:string">invoice.number</value>
		</item>
		<item name="workflow.locale">
			<value xsi:type="xs:string">en_GB</value>
			<value xsi:type="xs:string">en_US</value>		
			<value xsi:type="xs:string">de_DE</value>
		</item>
		
		<!-- Tika OCR Server  -->
		<item name="tika.ocrmode"><value xsi:type="xs:string">OCR_ONLY</value></item>
		<item name="tika.options">
			<value xsi:type="xs:string">X-Tika-OCRLanguage=eng+deu</value>
			<value xsi:type="xs:string">X-Tika-PDFocrStrategy=OCR_ONLY</value>		
		</item>
	
		<!-- ML spaCy Server -->
		<item name="ml.training.endpoint"><value xsi:type="xs:string">http://imixs-ml-spacy:8000/</value></item>
		<item name="ml.training.model"><value xsi:type="xs:string">invoice-de-0.2.0</value></item>
		<item name="ml.training.filepattern"><value xsi:type="xs:string">.pdf|.PDF</value></item>
	        <!-- LOW | GOOD -->
		<item name="ml.training.quality"><value xsi:type="xs:string">LOW</value></item>
	
		
		<!-- Define the training set and taining mode -->
		<item name="workflow.query"><value xsi:type="xs:string">($workflowgroup:"Rechnungseingang" OR $workflowgroup:"Sachrechnung") AND ($taskid:5900)</value></item>	  
		<item name="workflow.pagesize"><value xsi:type="xs:int">10</value></item>
		<item name="workflow.pageindex"><value xsi:type="xs:int">303</value></item>
	
	
	</document>	


# Development 

## Docker Build

To build the latest image from sources run:

	$ mvn clean install -Pdocker