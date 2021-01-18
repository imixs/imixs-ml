# Imixs-ML - Core

The Imixs-ML Core module defines the core interface to access a machine learning framework. It provides Data Object classes and a RestFull Client to exchange data between the *Imixs-Workflow Engine* and a ML Engine. 

In this way the Imixs-ML Core API acts as an *Anti Corruption Layer* between the Imixs-ML API Service and ML Engine like [spaCy](https://spacy.io/) or [Apache mxnet](https://mxnet.apache.org/).

<p align="center"><img src="../images/imixs-ml-architecture-002.png" /></p>



## Adapter Services

For each ml framework a separate Adapter Service implementing the Imixs-ML Core api can be provided. The Adapter service wraps the concrete ml framework API into the Imixs-ML API. 

For example the [Imixs-ML-spaCy](../imixs-ml-spacy/README.md) module provides a wrapper service for the [spaCy](https://spacy.io/) ml framework written in Python. 

A adapter service must implement the following endpoints:

 - **POST: /analyse/{model}** - send a text to be analysed by a specific model
 - **POST: /training/{model}** - send a training dataset to train a specific model


## POST a TrainingData Object

To train a ml-model an array of TraingData objects can be POST to the API resource */training/*

	POST
	/training/{model}

The model name is mandatory and specifies the name of the model to be trained. 

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
  


## POST a AnalyseData Object

To analyse a text based on a ml-model a AnalyseData object can be POST to the API resource *analyse/*

	POST
	/analyse/{model}

The model name is mandatory and specifies the name of the model to be used for analyse. 

An AnaylseData object has the following JSON structure:

	{
	  "text": "string"
	}


## Delete Model

To delete a model remotely you can call a DELETE request to the API resource *model/*

	DELETE
	/{model}

## The ML-Client

The ML-CLient is a Rest Service Client based on Jax-rs to access an ml framework. It can be used to train data or to analyse data.


	MLClient mlClient = new MLClient();
	
	mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);




# The EntityAdapter

The EntityAdapter can be used to  adapt an entity with alternative formating values.  This adapter is useful if a string constant can occur in different formats like a currency. For example the float value 1500.00 can have different presentations in a text fragment:


	1500.00
	1500,00
	1.500,00
	1,500.00
	
The TrainingService implements an Observer Pattern based on CDI Events to call registered EntityAdapters
The Event is defined by the class:

	org.imixs.ml.service.AnalyzeEntityEvent


