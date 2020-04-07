# Imixs-ML

Imixs-ML is a microservice project with the goal to provide a NPL Named Entity Recognition Service for Business Process Management.

The projekt is at the moment experimental only.


# Docker

In this project we provide a Docker image which is based on [tiangolo/uvicorn-gunicorn-fastapi-docker](https://github.com/tiangolo/uvicorn-gunicorn-fastapi-docker). This image comes with Uvicorn managed by Gunicorn for high-performance FastAPI web applications in Python 3.7. The image has an "auto-tuning" mechanism included, so that it is very easy to add custom Rest-API resources. 

To build the image from the Dockerfile run: 


    $ docker build --tag=imixs/imixs-ml .

To test the image run the container in an interactive mode:
    
	$ docker run --rm --name="imixs-ml" -it \
			-p 8000:8000 \
			imixs/imixs-ml


Open the api documentation via:

	http://localhost:8000/docs
	
You can test the service with curl:


	$ curl -X POST "http://localhost:8000/text/?lang=en" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"text\":\"M. Melman is a team member of the open source project Imixs-Workflow.\"}"


You your can test from the swagger UI http://localhost:8000/docs

<img src="images/swaggerui.png" />

The following example for a POST request...

	{
	  "text": "M. Melman is a team member of the open source project Imixs-Workflow."
	}

...will result in an output like this:

	{
	  "message": "M. Melman is a team member of the open source project Imixs-Workflow.",
	  "lang": "en",
	  "ents": [
	    {
	      "text": "M. Melman",
	      "label_": "PERSON"
	    },
	    {
	      "text": "Imixs-Workflow",
	      "label_": "ORG"
	    }
	  ]
	}



# Contribute

_Imixs-ML_ is open source and your are sincerely invited to participate in it. 
If you want to contribute to this project please [report any issues here](https://github.com/imixs/imixs-ml/issues). 
All source are available on [Github](https://github.com/imixs/imixs-ml). 