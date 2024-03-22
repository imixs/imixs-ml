# Find details : https://heidloff.net/article/running-mistral-locally-cpu/#run-model
from llama_cpp import Llama

# Put the location of to the GGUF model that you've download from HuggingFace here
model_path = "mistral-7b-instruct-v0.2.Q4_K_M.gguf"

# Create a llama model
#model = Llama(model_path=model_path)
model = Llama(
    model_path=model_path,
    temperature=0.75,
    max_tokens=2000,
    top_p=1,
    
)

# Prompt creation
# Format:
# <s>[INST] Instruction [/INST] Model answer</s>[INST] Follow-up instruction [/INST]
#
# system_message = "Du bist ein Münchner Reiseführer. "
# user_message = "Was kann man an Ostern in München unternehmen?"

# prompt = f"""<s>[INST] <<SYS>>
# {system_message}
# <</SYS>>
# {user_message} [/INST]"""


prompt = """<s>[INST] You are a helpful code assistant. Your task is to generate a valid JSON object based on the given information. So for instance the following:

name: John
lastname: Smith
address: #1 Samuel St.

would be converted to:[/INST]
{
"address": "#1 Samuel St.",
"lastname": "Smith",
"name": "John"
}
</s>
[INST]
name: Ted
lastname: Pot
address: #1 Bisson St.
[/INST]"""

# Model parameters
max_tokens = 200

# Run the model
print("sending this prompt\n==============\n",prompt,'\n---------\n')
output = model(prompt, max_tokens=max_tokens, echo=True)

# Print the model output
print(output)


print("----------finished-------------------")