"""
The .....

@author: ralph.soika@imixs.com 
@version:  1.0 
""" 
# This package provides data classes used to exchange prompt and configuration data.
#
from pydantic import BaseModel
from dataclasses import dataclass, field
from typing import List
import json


class PromptEntity(BaseModel):
    system_message: str
    user_message: str
   
##########################################################
# This dataclass is used to exchange the prompt data between the rest api and the LLM.
# 
# The class uses the dataclass decorator (introduced in Python 3.7) that allows an easy definition of classes that only store data, 
# similar to NamedTuples, but with additional features and a familiar class syntax.
##########################################################
@dataclass
class XMLPrompt:
    system_message: str = field(
        metadata={
            "example": "Du bist ein hilfreicher Java Code Assistent.",
            "name": "system_message", 
            "type": "Element"
        }
    )
    user_message: str = field(
        metadata={
            "example": "Was ist die Imixs-Workflow engine?",
            "name": "user_message", 
            "type": "Element"
        }
    )    

    output: str = field(
        metadata={
            "name": "output", 
            "type": "Element"
        }
    )    