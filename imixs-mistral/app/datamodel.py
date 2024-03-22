"""
The datamodel defines the Data classes used for prompts.


@author: ralph.soika@imixs.com 
@version:  1.0 
""" 
# An Entity class defines a Entity object with its name and its position in the data objects text.

from pydantic import BaseModel
from typing import List
import json


class PromptEntity(BaseModel):
    system_message: str
    user_message: str
   
