# Find details : https://heidloff.net/article/running-mistral-locally-cpu/#run-model
from llama_cpp import Llama

# Put the location of to the GGUF model that you've download from HuggingFace here
model_path = "mistral-7b-instruct-v0.2.Q4_K_M.gguf"

# Create a llama model
model = Llama(model_path=model_path)

# Prompt creation
system_message = "Du bist ein Münchner Reiseführer. "
user_message = "Was kann man an Ostern in München unternehmen?"

prompt = f"""<s>[INST] <<SYS>>
{system_message}
<</SYS>>
{user_message} [/INST]"""

# Model parameters
max_tokens = 200

# Run the model
output = model(prompt, max_tokens=max_tokens, echo=True)

# Print the model output
print(output)


print("-----------------------------")

user_message = "Was kann man an Ostern in Paris unternehmen?"

prompt = f"""<s>[INST] <<SYS>>
{system_message}
<</SYS>>
{user_message} [/INST]"""

output = model(prompt, max_tokens=max_tokens, echo=True)
print(output)


print("----------FERTIG-------------------")