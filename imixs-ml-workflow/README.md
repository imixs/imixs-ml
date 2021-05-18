# Imixs-ML Workflow

The Imixs-ML Workflow module provides Adapter classes, CDI Beans and Service EJBs to integrate the Imixs machine learning framework into the workflow processing life cycle.

 - **ML-Adapter**<br/>The Workflow Adapter class 'MLAdapter' is used for *Natural language processing (NLP)* within the processing life cycle on a workflow instance. The adapter can analyse the text of documents and extract relevant business data for a running business process. This includes entity recognition and text classification.  <br/>

 - **ML-Definition** <br/>A data structure holding the information of a single ml service endpoint  <br/>

 - **ML-Service** <br/>A service EJB reacting on Processing events storing the ml definition object  <br/>

 - **ML-TrainingScheduler** <br/>The MLTrainingScheduler is a timer service sending training data to a ML Service endpoint.   <br/>

 - **ML-Controller** <br/> The CDI bean 'MLController' is used for user interaction like data input, data verification and data confirmation.   <br/>

## The MLAdapter

The adapter *'org.imixs.ml.workflow.MLAdapter'* is used for ml analysis against a ML Service endpoint. The MLAdaper automatically analyses the text content of all attached documents, and stores entities and categories found in the text into corresponding items.  

### Configuration by Properties

The MLAdapter can be configured by the following imixs.properties 

 - *ml.service.endpoint* - defines the serivce endpoint of tha machine learning framework based on the Imixs-ML-Core API
 - *ml.model* - the default model name to be used (optional)
 - *ml.locales* list of supported language locales

The parameters can be set in the imixs.properties or as environment variables:

	ML_SERVICE_ENDPOINT=http://imixs-ml-spacy:8000/
	ML_MODEL=invoice-model-1.0.0
	ML_LOCALES=de_DE,en_GB

All these parameters can be overwritten by the model.

### Configuration by the Model

The MLAdapter can also be configured through the model by defining a workflow result item named '*ml-config*'.

<img src="ml-model-configuration.png" />

See the following example:
    

	<ml-config name="model">invoice-de-0.0.3</ml-config>
	<ml-config name="endpoint">
	    https://localhost:8111/api/resource/
	</ml-config>
	<ml-config name="quality">REDUCED</ml-config>
	<ml-config name="locales">de_DE,en_GB</ml-config>

**Note:** The mode name is mandatory. In case not default model is defined by the environment variable 'ML_MODEL' and not model is specified by the BPMN model, the Adapter throws an ProcessingErrorException. 

The model name of a successful text analyses will be stored by the MLAdapter into the item 'ml.model'. This information is used by the ML Training Service later. 



### Natural Language Processing (NLP) 

The MLAdapter sends text from documents to the ML Service endpoint for Named Entity Recognition (NER) and text classification. The results will automatically stored into the current process instance. 

The ML Adapter creates the following items

 - *ml.items* - all item names which are part of the NER.
 - *ml.categories* - all categories from the text classification
 

**Note:** Even if an entity was not found in the document content, but was configured by the bpmn model, the entity name will
be part of 'ml.items'. With this mechanism, as new entity can later be trained even if the entity is yet not part of the model.

*ml.categories* are optional and will only be provided if text classification was trained before.

#### Named Entity Recognition (NER)

Per default the MLAdapter takes all entities into the current workitem if an item with the name did not yet exist. 
To configure the behavior of the entity adaption in a more fine grained way, optional configuration via the workflow
 model is possible with the item 'ml-entity':

	<ml-config name="entity">
	    <name>invoice.total</name>
	    <type>currency</type>
	</ml-config>
	<ml-config name="entity">
	    <name>cdtr.bic</name>
	    <type>text</type>
	    <mapping>bic</mapping>
	</ml-config>
	

In this example the entity 'invoice.total' will be adapted by the Currency Adapter. 
The entity 'cdtr.bic' will be mapped into the item 'bic'.

##### Data Types

The following ml types are supported for the Named Entity Recognition:

**text**

A plain text string

**currency**

A text representing a currency in one of the defined locales. See the following examples:

	1.700.411,00
	100,-
	663.52

**date**

A text representing a date formated in one of the defined locales. See the following examples:

	3 May 2020
	3. May 2020
	03.05.2020
	
**iban**

A text representing a IBAN. See the following examples:

	DE69 5095 1469 0005 0111 11
	DE69509514690005011111	

##### Text length

A text entity has a predefined maximum length of 128 characters. The length can be overwritten with the tag 'length'

	<ml-config name="entity">
	    <name>cdtr.bic</name>
	    <type>text</type>
	    <length>8</length>
	</ml-config>

In this example the returned text entity for the item 'cdtr.bic' will have a maximum length of 8 characters. 

##### Required

Per default each  entity is marked as 'required=true' for later training. 
This means if a workitem does not provide a value for a ml-config entity, than the workitem is classified with TRAININGDATA_QUALITY_LEVEL_BAD and will not be included for later training.

Specially for Date and Currency values this requirement can often not be fulfilled. For example the Date Text representation

	APR. 14, 2021

can hardly be accociated with the date ISO Date 2021-04-14.

To avoid that a workitem is excluded from training because of a not matching text representation in the training text the optional attribute 'required' can be set to false. 


	<ml-config name="entity">
	    <name>invoice.total</name>
	    <type>currency</type>
	    <required>false</required>
	</ml-config>
	<ml-config name="entity">
	    <name>invoice.date</name>
	    <type>date</type>
	    <required>false</required>
	</ml-config>

In this case a not matching text representation does not lead to the TRAININGDATA_QUALITY_LEVEL_BAD.


#### Text Classification

For a text classification the MLAdapter expects a configuration specifying the target item:

	<ml-config name="category">
	    <target>ml.category</target>
	    <source>$workflowgroup</source>
	</ml-config>
	
In this example all categories found by the ML Framework will be listed in the item 'ml.category' (target item).

The tag <source> specifies the text category to be used for a incremental training. The source tag can be empty if a incremental training should not be performed. 



### Filter by File Pattern 

Per default the MLAdapter aggregates the text content from all attached files. If only a subset of files should be applied to the ML Service than an optional
Filename Pattern can be provided. 

	<ml-config name="filename.pattern">([^\s]+(\.(?i)(pdf|docx))$)</ml-config>

This example will only analyse content from PDF and MS Word files. 


### Training Quality

Per default the required training data quality is set to 'PARTIAL' which means that not all Items must contain a value to be acceptable for the training service. 
You can overwrite the Training quality level to 'FULL' or 'REDUCED' to accept a different quality level.

	<ml-config name="ml.training.quality">REDUCED</ml-config>



## The ML Definition

During the processing of a workitem the MLService creates a MLDefinition holding the details of a ml service endpoint. These data is stored in a Map object with the following data

 - ml.endpoint - the service endpoint of a ML Rest Service
 - ml.model - the model name
 - ml.locales - a list of locale definitions used for text analysis 
 - ml.items - list of entities recognized by ml service during text analysis 
 - ml.status - the current status of a ml.definition (suggest|confirmed|trained)


A client implementation can store additional data into ml.definition

## The MLService

The MLService is a stateless EJB reacting on Processing events. The service updates the  ml.status for each ml.definition of a workitem. 
 
The '*ml.status*' of a single mlDefinition can have one of the following values:

 - suggest - not yet confirmed by the user
 - confirmed - confirmed by the user
 - training - workitem is ready for a training

If no ml.status item  exists, and ml.items is not empty, than the status of the mlDefintion is set to 'suggest'. 

If the status is 'suggest' and the current event is 'public' than the status is set to 'confirmed'

If the status is 'confirmed' and the workitem type is 'workitemarchive' than the status is set to 'training'. This status indicates that all known entities of a mlDefinition are filled with data found in the document content. This means that this workitem can be used for later training. The MLService creats the eventlog entry 'ml.training' for this workitem. See the 'ML TrainingScheduler'.

### Overwriting the ML Status flag

You can overwrite the ML-Status flag in a BPMN event by providing a ml-config tag with the name 'status':

	<ml-config name="status">suggest</ml-config>

This example reset the ML-Status to 'suggest'.

## ML Training Scheduler

The service MLTrainingScheduler is an EJB Timer Service sending the collected training data of a workitem to the ML training service.
The service reacts on the  eventlog entry 'ml.training'. If this event log topic was fired, the service verifies the status of all mlDefinitions stored in the corresponding workitem.

If the ml status of a mlDefinition is 'training' then the workitem data is used for training againsed the corresponding ml service endpoint.

**Note:** the training service can reject the workitem for training if the data is of an insufficient quality.

The training service can be configured by the following configuration parameters:

 - ML_TRAINING_SCHEDULER_ENABLED - true|false
 - ML_TRAINING_SCHEDULER_INTERVAL - scheduler interval in milliseconds
 - ML_TRAINING_SCHEDULER_INITIALDELAY - initial delay during first startup (deployment) 
 
To activate the Training Scheduler the service need to be enabled (it is disabled per default). See the following example configuration:

	ML_TRAINING_SCHEDULER_ENABLED=true
	ML_TRAINING_SCHEDULER_INTERVAL=30000
	ML_TRAINING_SCHEDULER_INITIALDELAY=60000

This setting will enable the training scheduler with an interval of 30 seconds and an initial delay of 60 seconds. 
  
### The Training Quality Level
  
The training scheduler will only train data with a training quality level="PARTIAL" or "FULL". The  training quality level="PARTIAL" is the default setting. 

 - ML_TRAINING_QUALITYLEVEL=FULL  - all ML Items of a workitem must provide matching values. 
 - ML_TRAINING_QUALITYLEVEL=PARTIAL -  empty values are allowed (default).
 - ML_TRAINING_QUALITYLEVEL=REDUCED

It is possible to force the training quality level "FULL" with the environment 'ML_TRAINING_QUALITYLEVEL': 

	ML_TRAINING_QUALITYLEVEL=FULL


**Note:** If a workitem provide no value for a item, but the corresponding text is part of the text, this may lead to a decrease of the overall ml model quality. 



## The MLController

The MLController is a request scoped CDI bean used to verify and confirm ML data.
The method *getStatus()* returns  a JSON object containing the current ml result status and the item names collected by the MLAdatper.

	{
	  "status":"suggest",
      "items": [
        "invoice.number",
        "invoice.total"
      ]
    }


This method can be called in JSF page to handle the status in JavaScript:

	<ui:fragment rendered="#{mlController!=null}">
		<script type="text/javascript">
			/*<![CDATA[*/
				// get ml status
				var mlResult = #{mlController.getMLResult()};
			/*]]>*/
		</script>
	</ui:fragment>


   
### UI Integration

The project [Imixs-Office-Workflow](https://github.com/imixs/imixs-office-workflow/) provides an example for a UI integration based on the MLCOntroler status and a autocompletion feature.
The subform *workitem-ml.xhtml* demonstrates a full integration with a highlight and autocompletion feature:	

<img src="ml-input-suggest.png" />

	
### ML Autocompletion

To support autocompletion teh MLController provides a method to search for a text phrase within the document content. This helps to generate valid training data, as the text values of input items are part of the document content which is important for later training.

