# Imixs-ML-spaCy

Imixs-ML-spaCy is a wrapper service to adapt the [spaCy API](https://spacy.io/) for the Imixs-ML framework. The service is based on the [Imixs-ML-Core API](../imixs-ml-core/README.md) which defines the API resources and methods. 

<p align="center"><img src="../images/imixs-ml-architecture-003.png" /></p>


# Docker

The Imixs-ML-spaCy service runs in a Docker container and can be deployed in a container environment like Kubernetes or Docker Swarm.  

The image is based on the [tiangolo/uvicorn-gunicorn-fastapi-docker](https://github.com/tiangolo/uvicorn-gunicorn-fastapi-docker) provided Uvicorn. Uvicorn managed by Gunicorn is a high-performance FastAPI application framework written in Python 3.7. The image has an "auto-tuning" mechanism included. This makes it very easy to add custom Rest-API resources like the Imixs-ML API. 

## Build and Run

To build the image from the Dockerfile run: 

    $ docker build --tag=imixs/imixs-ml-spacy .

To test the image run the container in an interactive mode:
    
	$ docker run --rm --name="imixs-ml-spacy" -it \
	  -e PYTHONUNBUFFERED=1 \
	  -p 8000:8000 \
	  imixs/imixs-ml-spacy


**Note:** When running imixs-ml-spacy in docker-compose you should set the environment variable *PYTHONUNBUFFERED* to 1. This forces python flushing its stdout. 

	imixs-ml-spacy: 
	  image: imixs/imixs-ml-spacy
	  environment:
	    PYTHONUNBUFFERED: 1
	    ports:
	    - "8000:8000" 
      



### The Model

The default model path for the service is set to *models/*. You can of course overwrite the path to provide you own one:

	$ docker run --rm --name="imixs-ml-spacy" -it \
	  -e MODEL_PATH="my-models" \
	  -p 8000:8000 \
	  imixs/imixs-ml-spacy

Each rest API resource expects the name of the model as a parameter. Together with the MODEL_PATH this is the location of the spacy model. 

### Language Support

The spaCy API provides different language models. The model language is set by ISO code of the language class to load. The default value is *en*. You can overwrite the model language by setting the environment variable *MODEL_LANGUAGE*

	$ docker run --rm --name="imixs-ml-spacy" -it \
	  -e MODEL_LANGUAGE="de" \
	  -p 8000:8000 \
	  imixs/imixs-ml-spacy
	  
**Note:** The language is only relevant for creating a new model. You should not change the language for an existing model.	  


### SpaCy Options: MIN_LOSSES & RETAIN_RATE

Imixs-ML-spaCy provides the optional parameters `min_losses` and `retrain_rate`. These parameters are used to refine continues learning.

This parameter `min_losses` defaults to 0. This means, independent how accurate a model update with a given training set was, the model will be updated. This can be in some cases a problem when parts of the training data have an overweight against other parts in the training data set. It means that some kind of data will be trained better and better against the other parts. This is also known as 'overfitting'. This may happen when using very low `min_losses` e.g. <0.0000000001. In such cases it may be helpful to set a higher value (e.g. 0.1) to avoid this effect. 

The param `retrain_rate` (0-100) is a percentage value indicating how strict the min_losses factor should be handled. 0% means: don't retrain results below the min_losses. A higher value determines if good training data should still be retrained (random factor).  A good value is 25% which 
 means every 4th good training result will be retrained independent from the min_losses. 100% means the model will be updated independent form the losses factor. 

The SpaCy Options can also be set via the imixs-ml-workflow api by the '*options*' in the ml-config item 


	<ml-config name="options">min_losses=0.1&retrain_rate=25</ml-config>


## Cancel Training

You can cancel the training process at any time by overwriting the ml.status. For example you will set the ml.status to 'cancel' in case a workflow process was canceled by the user. 
 
	<ml-config name="status">cancel</ml-config>


### Logging

Imixs-ML-Spacy write a separate log file named `imixs.log`. From a running docker container you can view the messages with

	$ docker exec -it imixs-ml-spacy tail -f imixs.log

For example you can also grep only the latest losses from training with:

	$ docker exec -it officealexanderlogistics_imixs-ml-spacy_1 tail -f imixs.log | grep losses={

The Imixs Logger rotate 5 logfiles over the last 7 days

To check all Log files:

	$ docker exec -it imixs-ml-spacy ls -lah

## Access the API from your Web Browser

The Imixs-ML-spaCy wrapper service provides a web front-end to access the api for testing: 

	http://localhost:8000/docs
	
Open this link in your web browser 

<img src="../images/swaggerui.png" />
	
Of course you can also test the service with curl:

	$ curl -X POST "http://localhost:8000/text/?lang=en" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"text\":\"M. Melman is a team member of the open source project Imixs-Workflow.\"}"



## Initialize a New Model with Categories

Because of a restriction an existing SpaCy model can not be extended with new text categories like it is possible with *ner* entities. For that reason the SpaCy wrapper service provides a Rest API call to initialize a new blank model with predefined text categories. This method should be called before an incremental training of text categories. 

	POST
	http://localhost:8000/model/{modelname}
	
	[
	  "cat", "dog"
	]

**Note:** For incremental training of new text entities it is not possible to initialize a new model!

## Testing 

The following example for a POST request...

	{
	  "text": "M. Melman is a team member of the open source project Imixs-Workflow."
	}

...will result in an output like this:

	[
	  {
	    "entity": "person",
	    "text": "Melman"
	  },
	  {
	    "entity": "project",
	    "text": "Imixs-Workflow"
	  }
	]


pip install -U spacy

You can use the Eclipse IDE with the [Pydev plugin](https://www.pydev.org/) for development.
To prepare you environment make sure that pyhton 3.7 and pip3 is installed correctly.

To install pip3 on debian run:


	$ sudo apt install python3-pip
	
Next you can import the spacy and fastapi dependencys

	$ pip3 install --no-cache-dir -r requirements.txt

To verifiy the installed versions run

	$ pip3 list


**Note:** for testing with the nigly build pre release 3.0 of spacy run:

	$ pip3 install -U spacy
	$ pip3 install --no-cache-dir -r requirements.txt
	$ python3 -m spacy download en_core_web_sm


## Health Check

The Rest API provides the HealthCheck endpoint /health.

The Health Check returns the status 'UP' and the list of models available in the current instance. 

	{
	  "status": "UP",
	  "models": [
	    "invoice-de-0.1.0"
	  ]
	}

The Health Check returns the status 'DOWN' if an IO error with the model path occurs. 

	{
	  "status":"DOWN",
	  "exception":"..."
	}


### Kubernetes

To validate the health status in Kubernetes you can do a livenessProbe:

	spec:
	  containers:
	    ...
	    livenessProbe:
	      httpGet:
	        path: /health
	        port: 8000
	      initialDelaySeconds: 30
	      periodSeconds: 10
	      failureThreshold: 3
		...

# Development 

## Eclipse Setup

If you use the [Eclipse Plugin Pydev](https://www.pydev.org/) (which is recommended) make sure you added the spacy lib folder.
In the properties for your pydev project, there's a pane called "PyDev - PYTHONPATH", with a sub-pane called "External Libraries". You can add source folders to the path using that pane. Your project code will then be able to import modules from those source folders. For spacy this should be the folder 

	~/.local/lib/python3.7/site-packages/spacy

Also take care that the Python interpreter settings are 



I had a similar problem before. I solved the problem by this steps:

1. Window > Preferences > PyDev > Interpreters > Python Interpreter
2. Delete the Python Interpreter path you added previously
3. Add "New" Python Interpreter and choose Pyhton 3.7

<img src="../images/python/pydev_setup-01.png" style="width:660px" />

Also make sure that you have added the libraries _fastapi, spacy and pydantic_ to your project properties (PyDev -> external libraries)

<img src="../images/python/pydev_setup-02.png" style="width:660px" />

If you have not yet installed the libraries on your system run

	$ pip install pydantic fastapi spacy

## Docker Hub

To push the latest image to a repository run:

	$ docker build -t imixs/imixs-ml-spacy:latest . 
	$ docker push imixs/imixs-ml-spacy:latest