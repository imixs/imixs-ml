# Imixs-ML

Imixs-ML is a machine learning framework for [Imixs-Workflow](https://www.imixs.org). The project provides several microservices and APIs which integrate into an Imixs-Workflow environment. This project provides a generic machine learning API to adapt different ML frameworks like the [spaCy API](https://spacy.io/) or [Apache mxnet](https://mxnet.apache.org/). In this way the Imixs-ML project combines the concepts of machine learning with the concepts of business process management. This is a powerful and very flexible solution to digitize business processes.

The core concept of Imixs-ML is based on *Natural language processing (NLP)* which is a subfield of machine learning. 
With the *Named entity recognition (NER)* a given stream of text can be analysed and text entities  such as people, places or even invoice data such as dates and invoice totals can be extracted.
Imixs-ML adapts this concept in its core API and provides classes to integrate NER into the processing life cycle of a Imixs-Workflow system.

<p align="center"><img src="images/imixs-ml-architecture-001.png" /></p>

The project consists of the following modules:

 * [Imixs-ML Core](./imixs-ml-core/README.md) - java core libraries to be used to access different machine learning frameworks
 * [Imixs-ML Workflow](./imixs-ml-workflow/README.md) - Adapter classes to integrate machine learning into the workflow processing life cycle
 * [Imixs-ML spaCy](./imixs-ml-spacy/README.md) - a Rest API for [spacy.io](https://spacy.io/)
 * [Imixs-ML Training](./imixs-ml-training/README.md) - a external training service to train models from existing workflow data
 
# Imixs-ML Core

The module Imixs-ML Core defines the core interface to access a machine learning framework. It is providing java object classes and a client to access a ML framework.

Learn more in the section [Imixs-ML Core](./imixs-ml-core/README.md).  

# Imixs-ML Workflow

The Imixs-ML Workflow defines Adapter Classes to be used in a Imixs BPMN workflow model.

Learn more in the section [Imixs-ML API](./imixs-ml-workflow/README.md).  


# Imixs-ML spaCy

Imixs-ML-spaCy is a wrapper service to adapt the [spaCy API](https://spacy.io/) for the Imixs-ML framework. 

Learn more in the section [Imixs-ML spaCy](./imixs-ml-spacy/README.md). 


# Imixs-ML Training

Imixs-ML-Training is separate training microserve to be use to train existing wokflow data. 

Learn more in the section [Imixs-ML spaCy](./imixs-ml-training/README.md). 


# Contribute

_Imixs-ML_ is open source and your are sincerely invited to participate in it. 
If you want to contribute to this project please [report any issues here](https://github.com/imixs/imixs-ml/issues). 
All source are available on [Github](https://github.com/imixs/imixs-ml). 


## License

Imixs-ML is free software, because we believe that an open exchange of experiences is fundamental for the development of valuable software. All results of this project are provided under the [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0.en.html). 
