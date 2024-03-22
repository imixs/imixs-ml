# Find details : https://heidloff.net/article/running-mistral-locally-cpu/#run-model
from llama_cpp import Llama
llm = Llama(
   model_path="./mistral-7b-instruct-v0.2.Q4_K_M.gguf"
)
output = llm(
   "Q: Generate a list of 5 funny cat names. A: ",
   max_tokens=500,
   stop=["Q:", "\n"],
   echo=True
)
print(output)
