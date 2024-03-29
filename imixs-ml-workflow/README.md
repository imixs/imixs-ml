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
	ML_OPTIONS=...

All these parameters can be overwritten by the model. The parameter '*ML_OPTIONS*' is optional and can contain additional key/value pairs used to configure the ml framework. See the ML framework implementation for details. 

### Configuration by the Model

The MLAdapter can also be configured through the model by defining a workflow result item named '*ml-config*'.

<img src="ml-model-configuration.png" />

See the following example:
    

	<ml-config name="model">invoice-de-0.0.3</ml-config>
	<ml-config name="endpoint">
	    https://localhost:8111/api/resource/
	</ml-config>
	<ml-config name="locales">de_DE,en_GB</ml-config>
	<ml-config name="options">value1=x;value2=</ml-config>

**Note:** The ml-config '*model*' is mandatory. In case no default model is defined by the environment variable 'ML_MODEL' and no model is specified by the BPMN model, the Adapter throws an ProcessingErrorException. 

The ml-configuration will be stored after a successful text analyses  by the MLAdapter into the item 'ml.model'. This information is used by the ML Training Service for later processing. 


### Natural Language Processing (NLP) 

The MLAdapter sends text from documents to the ML Service endpoint for Named Entity Recognition (NER) and text classification. The results are automatically stored into the current process instance. 

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

Per default each  entity is marked as 'required=false' for later training. 
This means, that in case a workitem does not provide a value for a ml-config entity or the value is not found in the training text, this workitem will be classified with TRAININGDATA_QUALITY_LOW and will be included for later training.

If you want to force a MLEntity to be part of a Training Data set you can set the option "required=true"

	<ml-config name="entity">
	    <name>invoice.total</name>
	    <type>currency</type>
	    <required>true</required>
	</ml-config>
	<ml-config name="entity">
	    <name>invoice.date</name>
	    <type>date</type>
	    <required>true</required>
	</ml-config>

In this case a missing item value or a not matching text representation does lead to the TRAININGDATA_QUALITY_BAD and the workitem will not be used for training.

Specially for Date and Currency values this requirement can often not be fulfilled. For example the Date Text representation

	APR. 14, 2021

can hardly be accociated with the date ISO Date 2021-04-14. To exclude those workitems from training the optional attribute 'required' can be set to true. 

A good practice is to start with the flag 'required=true' for all ML Entities. If the training results are to low, than the switch to 'required=false' for some entities. 



#### Text Classification

For a text classification the MLAdapter expects a configuration specifying the target item:

	<ml-config name="category">
	    <target>ml.category</target>
	    <source>$workflowgroup</source>
	</ml-config>
	
In this example all categories found by the ML Framework will be listed in the item 'ml.category' (target item).

The tag <source> specifies the text category to be used for a incremental training. The source tag can be empty if a incremental training should not be performed. 



### Regex Pattern Recognition

For complex tasks, it’s sometimes hard to train a statistical entity recognition model.
Since statistical models require very accurate training data, rule-based approaches are more practical in many situations. Especially when sufficiently large test data are not yet available. This is especially true at the start of a project. In this situation, you can use *Imixs-ML Regex Pattern Recognition* as part of the data collection process to "bootstrap" a statistical model.

With the *Regex Pattern Recognition* the Imixs ML Adapter will try to find matches in the text, in case the statistical model did not provide any results.  

To activate the *Regex Pattern Recognition* you can add the configuration via the workflow model:

	<ml-config name="regex">
	    <name>cdtr.iban</name>
	    <type>text</type>
	    <pattern>[A-Z]{2}\d{2} ?\d{4} ?\d{4} ?\d{4} ?\d{4} ?[\d]{0,2}</pattern>
	</ml-config>
	<ml-config name="regex">
	    <name>cdtr.bic</name>
	    <type>text</type>
	    <pattern>[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}(?:[A-Z0-9]{3})?</pattern>
	</ml-config>

This is an example to find matches for IBAN/BIC

**Note:** The *Regex Pattern Recognition* will only be applied in case the statistical model did not provide any results.  





### Filter by File Pattern 

Per default the MLAdapter aggregates the text content from all attached files. If only a subset of files should be applied to the ML Service than an optional
Filename Pattern can be provided. 

	<ml-config name="filename.pattern">([^\s]+(\.(?i)(pdf|docx))$)</ml-config>

This example will only analyse content from PDF and MS Word files. 


## The ML Definition

During the processing of a workitem the MLService creates a MLDefinition holding the details of a ml service endpoint. These data is stored in a Map object with the following data

 - ml.endpoint - the service endpoint of a ML Rest Service
 - ml.model - the model name
 - ml.locales - a list of locale definitions used for text analysis 
 - ml.items - list of entities recognized by ml service during text analysis 
 - ml.status - the current status of a ml.definition (suggest|confirmed|trained)


A client implementation can store additional data into ml.definition

## The MLService

The Machine Learning Status of a workitem is controlled by the `MLService` which is a stateless EJB reacting on Processing workflow events. The service updates the  `ml.status` for each `ml.definition` stored in a workitem. 
 
The `ml.status` of a single mlDefinition can have one of the following values:

 - `suggest` - not yet confirmed by the user
 - `confirmed` - confirmed by the user
 - `training` - workitem is archived and ready for a training

** The Suggest Status **
If no ml.status item  exists, and ml.items is not empty, than the status of the mlDefintion is set to `suggest`. 

** The Confimred Status **
If the status is `suggest` and the current event is `public` than the status is set to `confirmed`

** The Training Status **
If the status is `confirmed` and the workitem type is `workitemarchive` than the status is set to `training`. This status indicates that all known entities of a mlDefinition are filled with data found in the document content. This means that this workitem can be used for later training. The MLService automatically creates the eventlog entry `ml.training` for this workitem. See the 'ML TrainingScheduler'.



### Overwriting the ML Status flag

You can overwrite the ML-Status flag in a BPMN event by providing a ml-config tag with the name 'status':

	<ml-config name="status">suggest</ml-config>

This example reset the ML-Status to 'suggest'. Overwriting the ML Status flag may only be needed in case a workflow is canceled and you want to avoid that the workitem is used for later training. In such a case you can set the status to 'cancel'

	<ml-config name="status">cancel</ml-config>
		
## ML Training Scheduler

The service MLTrainingScheduler is an EJB Timer Service sending the collected training data of a workitem to the ML training service.
The service reacts on the  eventlog entry 'ml.training'. If this event log topic was fired, the service verifies the status of all mlDefinitions stored in the corresponding workitem.

If the ml status of a mlDefinition is 'training' then the workitem data is used for training againsed the corresponding ml service endpoint.

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
  
The training scheduler will only train data with a training quality level="GOOD" or "LOW".

 - TRAININGDATA_QUALITY_GOOD=10  - all ML Items of a workitem provide matching values. 
 - TRAININGDATA_QUALITY_LOW=4 -  some entities have no matching value and the required flag is set to false (default).
 - TRAININGDATA_QUALITY_BAD=0 - some entities have no matching value and the required flag is set to true. Or no matching training data if found at all.

It is possible to force the training quality level "GOOD" if all MLEntities are marked with the flag 'required=true'

	<ml-config name="entity">
	    <name>invoice.total</name>
	    <type>currency</type>
	    <required>true</required>
	</ml-config>
	<ml-config name="entity">
	    <name>invoice.date</name>
	    <type>date</type>
	    <required>true</required>
	</ml-config>

In this example only workitems with matching values for 'invoice.total' and 'invoice.date' will be used for training. If the items have no matching values or are empty the workitem is qualified with TRAININGDATA_QUALITY_BAD and will not be trained.

**Note:** If a workitem provides no value for a MLEntity, but the corresponding training text contains a logical text value, than this may lead to a decrease of the overall ml model quality. Also, if a workitem provides a value for a MLEntiry, which is not part of the corresponding training text, can lead to a decrease of the overall ml model quality. You can avoid this effect by setting the ML Entity flag 'required=true'.



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

