# AI & ML Integration

The Artificial Intelligence and Machine Learning features of the Dashjoin Platform is
delivered as a set of Docker containers that package models and external runtime components as
well as JSONata functions that make these features usable within any part of an application.

The previous developer reference section already lists some of the AI & ML capabilities, however, this
section explains the features in more detail and provides a comprehensive overview.

## JSONata Notebooks

Applying AI & ML functionality usually requires some degree of experimentation. 
The JSONata Notebooks provide this flexibility. A notebook is the combination of
a page with a single notebook widget. The platform ships with a default notebook
available at /page/Notebook. Note that you can create as many notebook pages as you like.

A Notebook consists of a sequence of code blocks that can be run individually via the run icon
or by pressing CTRL ENTER. The result of the call is also stored in the notebook and displayed below the
code block.

The default way of displaying data is JSON. You can display the data as a value, table, on a map,
or as a chart using the following syntax:

```json
{
  "widget": "display",
  "data": {
    "img": "https://dashjoin.com/img/fav.png",
    "width": "32"
  }
}```

```json
{
  "widget": "table",
  "data": $query("northwind", "group")
}
```

```json
{
  "widget": "map",
  "data": $adHocQuery("northwind", "select distinct CITY from EMPLOYEES")."EMPLOYEES.CITY"
}
```

```json
{
  "widget": "chart",
  "chart": "bar",
  "data": $query("northwind", "group")
}
```

If a code block starts with a variable assignment ($variable := ...), the variable can be used
in other code blocks as $variable.

It is possible to upload one or more files into the notebook. This creates new code block,
where the variable $upload is set to a map of file name to file content. This variable
can be used in other code blocks. Assume you upload a file.txt, then the contents of the file
is avaiable as $upload.file_txt. Note that dot is replaced with underscore to avoid having to
escape the field name.

The code block and its result is saved to the browser session, which is lost if the browser
is closed. You can save the current state of the notebook with the save button.
This writes the code blocks to the notebook's page and makes the notebook
available to other users.

## Optical Character Recognition

The optical character recognition (OCR) functionality is available in the dashjoin/ai-image Docker container. It exposes a
REST API on port 8080 where you provide an image URL such as this picture: 

<img width="400px" src="https://d207ibygpg2z1x.cloudfront.net/image/upload/v1540973697/articles_upload/content/ibttqvywe6gihhcu1zzf.jpg">

To start the container, run the following command (we use the host port 8083 to avoid 
clashing with the platform port):

```shell
docker run -p 8083:8080 dashjoin/ai-image
```

The URL is passed in a GET request https://.../image-ocr?url=... and returns the extracted text:

```javascript
$parseJson(
  $openJson("http://.../image-ocr?url=https://d207ibygpg2z1x.cloudfront.net/image/upload/v1540973697/articles_upload/content/ibttqvywe6gihhcu1zzf.jpg")
)
```

```json
"HEY YOU YES YOU\n\nYOU CANDO IT\n"
```

## Image Classification

The image classification functionality is also available in the dashjoin/ai-image Docker container. It exposes a
REST API where you provide an image URL such as this picture of a bird (https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg).

<img width="400px" src="https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg">

The URL is passed in a GET request https://.../image-classify?url=... and returns an array of
classifications and probabilities:

```javascript
$parseJson(
  $openJson("http://.../image-classify?url=https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg")
).{"type": $[1], "prob": $[2]}
```


```json
[
  {
    "type": "goldfinch",
    "prob": 0.9761154055595398
  },
  {
    "type": "bulbul",
    "prob": 0.017567412927746773
  },
  {
    "type": "coucal",
    "prob": 0.0015972057590261102
  }
]
```

## Face Recognition

The optical character recognition (OCR) functionality is also available in the dashjoin/ai-image Docker container. It exposes a
REST API where you provide an image URL such as this picture:

<img width="400px" src="https://media-cldnry.s-nbcnews.com/image/upload/newscms/2020_02/1521975/kristen-welker-today-191221-main-01.jpg">

The URL is passed in a GET request https://.../image-face?url=... and returns the extracted text along with the
coordinates of the face within the image:

```javascript
$parseJson(
  $openJson("http://.../image-face?url=https://media-cldnry.s-nbcnews.com/image/upload/newscms/2020_02/1521975/kristen-welker-today-191221-main-01.jpg")
)
```

```json
[
  {
    "faceid": "Kristen Welker",
    "top": 206,
    "left": 705,
    "bottom": 527,
    "right": 1026
  }
]
```

## Text Translation

The translation services base on a large language model and are available via REST API in the
dashjoin/ai-translation container. You can start the container using the following command
(the REST service runs on port 8080, we use 8084 to avoid port collisions):

```shell
docker run -p 8084:8080 dashjoin/ai-translation
```

The OpenAPI specification can be accessed at 
http://.../docs.
It offers a number of services, which the most important ones being "translate" and "language_detection". 
Translate takes the following parameters:

* target_lang: the code of the language, the text is supposed to be translated to
* text: an array of strings with the original text to be translated
* source_lang: code of the language, text is written in
* beam_size: can be used to trade-off translation time and search accuracy
* perform_sentence_splitting: determines whether the sentences are split into a string array

```javascript
$openJson("http://.../translate?target_lang=de&text=This%20is%20an%20awesome%20translation%20service")
```

```json
{
  "target_lang": "de",
  "source_lang": null,
  "detected_langs": [
    "en"
  ],
  "translated": [
    "Das ist ein toller Übersetzungsdienst"
  ],
  "translation_time": 3.5702028274536133
}
```

Language detection takes a single text parameter with the sample text and works as follows:

```javascript
$openJson("http://.../language_detection?text=example")
```

```json
"en"
```

## Dashjoin AI Assistant / Large Language Model

The Dashjoin AI Assistant makes it easy to integrate state of the art semantic large language model and
text embedding technology into your low code application. The functionality is available via an easy to use
administration UI as well as an end-user chat widget. Furthermore, you can access the features conveniently
via JSONata. Please refer to the documentation of the aichat widget in the developer reference for information
about how the chat widget can be configured. The API section below also shows examples of how to
call the REST services directly.

Dashjoin AI Assistant is available via the docker image dashjoin/ai-llm-kb and offers a range of powerful features around
large language models (LLMs):

* access APIs such as OpenAPI or Mistral in a uniform and secure way
* run LLMs locally and expose them via the same API
* Builtin and convenient retrieval augmented generation (RAG)
* Powerful Jupyter development environment for deploying custom AI code based on LlamaIndex
* Auto-deployment of custom AI code from a Dashjoin app
* Seamless integration of Dashjoin functions as LLM tools for integrating local structured data into the reasoning process

### Configuration

The following environment variables can be used to configure the container:

* DJAI_OPENAI_API_BASE: URL of the LLM API (default https://api.mistral.ai/v1)
* DJAI_OPENAI_API_KEY: API key for Mistral or OpenAI if these services are used
* DJAI_OPENAI_MODEL: LLM model used (default open-mistral-7b)
* DJAI_OPENAI_EMBEDDING_MODEL: embedding model used (defaukt mistral-embed)
* DJAI_AUTH_SECRET: Basic authentication header required to use the container API (default is "Basic YWRtaW46ZGpkamRq" which is base64 encoded authentication header for user admin and password djdjdj)
* DJAI_UI_PASSWORD: Password for the admin user when connecting to the container's admin UI (default djdjdj)
* DJAI_LLM_MODE: llm used (possible values are: ollama, llama-cpp, sagemaker, openai, openailike, azopenai, gemini, default openailike)
* DJAI_EMBEDDING_MODE: embedding mode (possible values are: ollama, huggingface, openai, sagemaker, azopenai, gemini, default openailike)
* DJAI_OLLAMA_URL: URL of the ollama service
* DJAI_DATA_QDRANT_PATH: path of the vector database (default dashjoin/data/default/qdrant)
* DJAI_DATA_PATH: path to local data (default dashjoin/data/default)

Vector DB settings

* DJAI_VECTORDB: vector DB to use (default is qdrant)
* DJAI_PGVECTOR_HOST: rag DB hostname (default postgres)
* DJAI_PGVECTOR_PORT: rag DB port (default 5432)
* DJAI_PGVECTOR_USER: rag DB username (default postgres)
* DJAI_PGVECTOR_PASSWORD: rag DB pwd (default postgres)
* DJAI_PGVECTOR_EMBED_DIM: vector DB dimension (default 768)
* DJAI_PGVECTOR_SCHEMA: rag schema (default public)
* DJAI_PGVECTOR_TABLE: rag table name (default aiembeddings)

Local model settings

* DJAI_OLLAMA_MODEL: llm model (default mistral)
* DJAI_OLLAMA_EMBEDDING_MODEL: embedding model (default nomic-embed-text)
* DJAI_OLLAMA_URL: ollama url (default http://ollama:11434)
* DJAI_OLLAMA_TFS_Z: Tail free sampling is used to reduce the impact of less probable tokens from the output. A higher value (e.g., 2.0) will reduce the impact more, while a value of 1.0 disables this setting (default 1.0)
* DJAI_OLLAMA_TOP_K: Reduces the probability of generating nonsense. A higher value (e.g. 100) will give more diverse answers, while a lower value (e.g. 10) will be more conservative (default 40)
* DJAI_OLLAMA_TOP_P: Works together with top-k. A higher value (e.g., 0.95) will lead to more diverse text, while a lower value (e.g., 0.5) will generate more focused and conservative text (default 0.9)
* DJAI_OLLAMA_REPEAT_LAST_N: Sets how far back for the model to look back to prevent repetition (default 64)
* DJAI_OLLAMA_REPEAT_PENALTY: Sets how strongly to penalize repetitions. A higher value (e.g., 1.5) will penalize repetitions more strongly, while a lower value (e.g., 0.9) will be more lenient (default 1.2)
* DJAI_OLLAMA_REQUEST_TIMEOUT: Time elapsed until ollama times out the request (default 600.0)
* DJAI_OLLAMA_AUTOPULL_MODELS: automatically pull models as needed (default true)

* DASHJOIN_DEVMODE: if set to 1, Jupyter and automatic reload of code changes is active
* JUPYTER_TOKEN: password of the Juyper development environment
* DASHJOIN_APPURL: GIT URL of the Dashjoin app to be activated in the container
* DASHJOIN_APPURL_BRANCH: optional GIT branch to use

### Running AI Assistant

This section shows some minimal configuration examples (relying on defaults wherever possible).

Run the container in production without app:

```shell
docker run 
  -p 8001:8001 
  -e DJAI_OPENAI_API_KEY=your_mistral_key 
  dashjoin/ai-llm-kb
```

Run the container in production with app:

```shell
docker run 
  -p 8001:8001 
  -e DJAI_OPENAI_API_KEY=your_mistral_key 
  -e DASHJOIN_APPURL=https://github.com/dashjoin/dashjoin-demo 
  dashjoin/ai-llm-kb
```

Run the container to develop an app. We export the Jupyter port 8080 to 8002 outside the container:

```shell
docker run 
  -p 8001:8001 
  -p 8002:8080 
  -e JUPYTER_TOKEN=djdjdj 
  -e DASHJOIN_DEVMODE=1 
  -e DJAI_OPENAI_API_KEY=your_mistral_key 
  -e DASHJOIN_APPURL=https://github.com/dashjoin/dashjoin-demo 
  dashjoin/ai-llm-kb
```

### Admin UI

The admin UI allows adding files to and deleting them from the vector database.
You can also test chatting with the LLM (Chat) and retrieval augmented generation (Query KB).

Large Language Model Chat: The large language model chat exposes the generative language capabilities of the underlying model.
The model is trained using a large collection of documents and books. It is able to summarize text,
answer questions you pose, and much more. In the admin UI, use the mode "LLM Chat" to work in this mode.

Retrieval Augmented Generation (RAG): RAG allows inserting knowledge around a specific use case into the large language
model - even if was not trained on these documents. You can upload your documents using the admin
UI. The LLM is then able to answer your questions about these documents.
In the admin UI, select the button "Query KB" to use this mode.

Choosing "Search in KB", only performs a semantic search on the uploaded documents without involving the LLM to answer your query.

### API

The API of both the vector database and the large language model are available via the following
[OpenAPI Definition](https://aikb.run.dashjoin.com/docs).

In order to access the API, we recommend registering a credential set with the service's username and password.
The following examples assume you have setup these credentials under the name "ai".

List all document added to the vector database:

```text
$curl("GET", "https://aikb.run.dashjoin.com/v1/ingest/list", null, {"Authorization": "ai"})
```

Invoke LLM chat:

```text
$curl("POST", "https://aikb.run.dashjoin.com/v1/completions", {
  "include_sources": false,
  "prompt": "How do you fry an egg?",
  "stream": false,
  "system_prompt": "You are a rapper. Always answer with a rap.",
  "use_context": false
}, {"Authorization": "ai"})
```

Invoke RAG:

```text
$curl("POST", "https://aikb.run.dashjoin.com/v1/completions", {
  "include_sources": true,
  "prompt": "wie muss die firewall bei SAP systemen konfiguriert werden?",
  "stream": false,
  "system_prompt": "You can only answer questions about the provided context. If you know the answer but it is not based in the provided context, don't provide the answer, just state the answer is not in the context provided. Answer in German.",
  "use_context": true
}, {"Authorization": "ai"})
```

Upload a Document:

```text
$curl(
  "POST", 
  "https://aikb.run.dashjoin.com/v1/ingest/file",
  {
    "file": base64 encoded data URL
  },
  {
    "Content-Type": "multipart/form-data",
    "Authorization": "ai"
  }
)
```

The base64 encoded data URL can easily be generated by using this button and input widget:

```json
{
  "widget": "button",
  "schema": {
    "type": "object",
    "properties": {
      "file": {
        "widget": "binary file with metadata"
      }
    },
    "print": "$curl('POST', 'https://aikb.run.dashjoin.com/v1/ingest/file', form, {'Content-Type': 'multipart/form-data', 'Authorization': 'ai'})"
  }
}
```

The ingest call returns the following result. Every files gets split into a number of documents:

```json
{
  "object": "list",
  "model": "private-gpt",
  "data": [
    {
      "object": "ingest.document",
      "doc_id": "c202d5e6-7b69-4869-81cc-dd574ee8ee11",
      "doc_metadata": {
        "file_name": "Sales Report Q3 2023.pdf",
        "page_label": "2"
      }
    }
  ]
}
```

You can create a database to keep track of files, owners, and the related document ids. 
A possible schema would be a table called doc with fields id, owner, and filename.
This allows your app to define collections of documents that are associated to specific users:

```text
$res := $curl(... ingest);
$email := email;

$res.data.{
  'id', doc_id,
  'owner': $email,
  'filename': doc_metadata.file_name
}.$create('database', 'doc', $)
```

To use a user's documents, you can retrieve the doc ids from the database and pass them to the LLM as follows:

```text
$curl('POST', 'https://aikb.run.dashjoin.com/v1/completions', {
  "include_sources": false,
  "prompt": "A question that can only be answered with information contained in an uploaded file",
  "stream": false,
  "use_context": true,
  "context_filter": {
    "docs_ids": [$all('sqlite', 'PART', {'OWNER': email}).ID]
  }
},
{'Authorization': 'ai'}
)
```

### Getting Access to AI Assistant

AI Assistant is available for demo purposes in the playground app.
You can book a private instance along with your Dashjoin tenant. Please refer to the [website](http://dashjoin.com) 
to access the shop page.
Note that the services all comply with european GDPR regulations.
Please contact us if you are interested in deploying your own copy using GPU resources in your datacenter.

## Extending AI Assistant

You can package AI functionality into your Dashjoin app and deploy it to Dashjoin AI Assistant.

### Adding Custom AI to an App

Within your app, create a folder "aikb/dashjoin/aikb" with a Python hook "app.py".
This hook is called upon startup where initializations can be performed and custom REST endpoints can be added.
Consider the following example:

```
import logging
from fastapi import FastAPI
from private_gpt.settings.settings import Settings
from private_gpt.ui.ui import PrivateGptUi
from dashjoin.aikb.app_router import app_router

logger = logging.getLogger(__name__)

def initializeApp(app: FastAPI, settings: Settings, ui: PrivateGptUi):
    logger.info("Dashjoin App - initializing")
    app.include_router(app_router)
```

This example simply registers this custom REST service:

```
from fastapi import APIRouter, Depends, Request
from private_gpt.server.utils.auth import authenticated

# Expose the App's REST service
app_router = APIRouter(prefix="/app/v1", dependencies=[Depends(authenticated)])

# Simple REST API that returns an info object
@app_router.get("/info", description="App Version Info", tags=["Custom App API"])
def info() -> object:
    return {
        "name": "Custom App",
        "version": "1.0"
    }
```

The new service is available right after the files are added. You can view the [OpenAPI definition](http://localhost:8001/docs#/Custom%20App%20API/info_app_v1_info_get) and test it using this curl command:

```
curl -u admin:djdjdj -X 'GET' \
  'http://localhost:8001/app/v1/info' \
  -H 'accept: application/json'
```

### Adding LlamaIndex AI Code

The LlamaIndex AI library is already installed on the system. You can add your custom code in your REST service. Consider this minimal OpenAI chat example:

```
import os
from llama_index.llms.openai import OpenAI

...

# set the OpenAI key
os.environ["OPENAI_API_KEY"] = "sk-..."

# instantiate the OpenAI llm
llm = OpenAI(model="gpt-3.5-turbo",temperature=0)

# expose it via REST
@app_router.get("/prompt", description="Simple prompt completion")
def complete(prompt: str) -> str:
    return str(llm.complete(prompt))
```

### Using Jupyter Notebooks

The most convenient way to experiment with Python and AI are Jupyter Notebooks that allow you to interactively
run code snippets. Dashjoin AI Assistant ships with a built-in Jupyter server. Assuming you started the container with the command shown above, simply point your browser to http://localhost:8002 and log in.
Create a new notebook and enter the following example:

```
import os
import requests
from llama_index.core.agent import ReActAgent
from llama_index.core.tools import FunctionTool
from llama_index.llms.openai import OpenAI
from requests.auth import HTTPBasicAuth

os.environ["OPENAI_API_KEY"] = "sk-..."

llm = OpenAI(model="gpt-3.5-turbo",temperature=0)

def get_employee_by_last_name(lastname: str) -> object:
    """Looks up an employee by last name"""
    return requests.post(
        "http://your-dashjoin-platform-address:8080/rest/function/tool",
        json={ "lastname": lastname },
        auth=HTTPBasicAuth("admin", "djdjdj")
    )

tool = FunctionTool.from_defaults(
    get_employee_by_last_name
)

agent = ReActAgent.from_tools(
    [tool],
    llm=llm,
    verbose=True,
)

agent.chat("what is the first name of our employee 'fuller'?")
```

This code answer's the question "what is the first name of our employee 'fuller'?" by using the LLM and equipping it with a tool
to fulfill the task. The tool textually describes what it can do in order to advertise its capabilities to the LLM.
The tool makes a REST call back to the Dashjoin Platform and runs a JSONata invoke function called "tool". It is defined as:

```
{
    "ID": "tool",
    "djClassName": "org.dashjoin.function.Invoke",
    "expression": "$all('northwind', 'EMPLOYEES', {'LAST_NAME': lastname})"
}
```

Running the Notebook shows the following trace:

```
> Running step 0a830c4e-e2c9-479b-b020-bdd027df3ad3. Step input: what is the first name of our employee 'fuller'?
Thought: The current language of the user is English. I need to use a tool to help me answer the question.
Action: get_employee_by_last_name
Action Input: {'lastname': 'Fuller'}
Observation: <Response [200]>
> Running step 31fa0f53-a975-4861-9fd2-89078616a27e. Step input: None
Thought: I can answer without using any more tools. I'll use the user's language to answer
Answer: The first name of the employee with the last name 'Fuller' is 'Andrew'.
```

### Adding LLM Tool Calls to the App

Once we get everything working in Jupyter, we can either parameterize the Notebook and call it via REST (see next section) or we can
convert it to a regular REST endpoint as shown below:

```
import os
import requests
from requests.auth import HTTPBasicAuth
from fastapi import APIRouter, Depends, Request
from private_gpt.server.utils.auth import authenticated
from llama_index.llms.openai import OpenAI
from llama_index.core.agent import ReActAgent
from llama_index.core.tools import FunctionTool

app_router = APIRouter(prefix="/app/v1", dependencies=[Depends(authenticated)])

os.environ["OPENAI_API_KEY"] = "sk-..."

llm = OpenAI(model="gpt-3.5-turbo",temperature=0)

def get_employee_by_last_name(lastname: str) -> object:
    """Looks up an employee by last name and returns the information as a JSON object"""
    return requests.post(
        "http://host.docker.internal:8080/rest/function/tool",
        json={ "lastname": lastname },
        auth=HTTPBasicAuth("admin", "djdjdj")
    )

tool = FunctionTool.from_defaults(
    get_employee_by_last_name
)

agent = ReActAgent.from_tools(
    [tool],
    llm=llm,
    verbose=True,
)

@app_router.get("/tool", description="Find employees using LLM and tool")
def complete(lastname: str) -> str:
    return str(agent.chat("what is the first name of our employee '"+lastname+"'?"))
```

### Installing Python Libraries

Python offers a wide ecosystem of libraries. In case you would like to use a library that is not already included
in Dashjoin AI Assistant, you can install it using Jupyter by running this command:

```
!pip install missing-lib
```

You can add the Notebook to your app and make sure it is run upon container initialization by adding this line to your app.py startup hook:

```
from dashjoin.aikb.util import executeNotebook

...

executeNotebook("dashjoin/aikb/install.ipynb")
```

### Calling Jupyter Notebooks over REST

The tools example aboce showed how code can be developed and tested in Jupyter and how it is then moved to Python.
Using the executeNotebook function, you can even expose the Notebook directly as a REST service:

```
from dashjoin.aikb.util import executeNotebook

# REST APIfication of a Jupyter Notebook
@app_router.post("/nb", name="Jupyter Notebook as a Service", description="Jupyter Notebook as Service", tags=["Custom App API"])
def notebook(args: NotebookArgs) -> object:
    import os
    os.environ["DJ_INPUT_ARGS"] = args.model_dump_json()
    return executeNotebook("app/aikb/dashjoin/aikb/nb.ipynb")
```

In the Notebook, you can access the REST parameters as follows:

```
import os
args = os.environ.get("DJ_INPUT_ARGS")
print(args)
```

The executeNotebook code must be placed in util.py:

```
import logging

logger = logging.getLogger(__name__)

def executeNotebook(nb: str):
    """
    Executes the given Jupyter Notebook and returns the result
    """
    import nbformat
    from nbconvert.preprocessors import ExecutePreprocessor
    import json

    with open(nb) as ff:
        nb_in = nbformat.read(ff, nbformat.NO_CONVERT)
        
    ep = ExecutePreprocessor(timeout=600) #, kernel_name='python3')

    logger.info("Notebook execution", nb)
    nb_out = ep.preprocess(nb_in)
    logger.info("Notebook executed", nb)
    print(json.dumps(nb_out, indent=2))
    return nb_out
```

### Committing Changes to the App

Jupyter comes with a full fledged GIT client. Therefore, you can commit your Python and Jupyter code to the Dashjoin App's GIT repository directly.

## Entity Reconciliation

When integrating data from different sources, entity reconciliation describes the process
of aligning different identifiers for the same entity that are used across the various sources.

There are various approaches that can be applied. In this section, we describe the
reconcileEntity JSONata function. It uses the [Wikidata](https://www.wikidata.org/)
search API in order to reconcile a text with a standardized Wikidata ID.
Wikidata is the database equivalent of Wikipedia, i.e. it is a crowd-sourced
database of entites that can also be found in wikipedia.
Consequently this function should not be used if the texts are very specific
or if the texts are unsuitable to be sent to a 3rd party service.

The function takes three parameters:

* entity: the entity string
* entity-language: the optional language of the entity string (default is en)
* limit: the number of ranked search results (default is 1)

Consider the reconciliation results for "Apple". The term is ambiguous since it can
refer to Apple the fruit and Apple, the tech company.

```javascript
$reconcileEntity("Apple", "en", 2)
```

```json
[
  {
    "id": "Q89",
    "label": "apple",
    "description": "fruit of the apple tree"
  },
  {
    "id": "Q312",
    "label": "Apple",
    "description": "American multinational technology company"
  }
]
```

Note that the most likely match comes first. If the call is repeated with "Apple Inc.", the Wikidata
ID Q312 is the first result.

## Entity Classification

When examining an unknown datasource, it is useful to generate a classification
of the values found in a column. This functionality is offered by the classifyEntities function.

This function extends reconcileEntity by also querying the Wikidata categories.
Given a list of entities, this function retrieves the Wikidata types and
returns all types that are common for all entities. This function has the following parameters:

* entities: the list of entity strings
* entity-language: the optional language of the entity string (default is en)
* limit: the number of ranked search results (default is 1)
* subclass-depth: this number indicates whether superclasses of Wikidata classes should be included (default is 1). Given depth 1, "Gone with the wind" would be classified as a movie. A movie is also a piece of art. Therefore, piece of art would be another potential classification tested with the other entities.

Consider this example. This call tries to find a common classification for "Unicef" and "Apple". The search limit
must be set to 2 in order to get Apple the company and the fruit. Both of these are classified as organizations.

```javascript
$classifyEntities(["Apple", "Unicef"], null, 2, 0)
```

```json
[
  "organization"
]
```

If the call is repeated with IBM instead of Unicef, the results are business, enterprise, public company, and technology company.

## Text Distance and Soundex Similarity

When matching entities, text similarity and distance metrics can also be useful.
Consider an example where many different sources include company names.
When matching these names, typical problems arise from simple typos and different spellings ("Apple" vs. "Apple Inc.").

Similarity and distance metrics can be useful in these circumstances.
Given two sets of strings, the synonym function allows applying these metrics.
It returns a list of matches that can be used as a synonym lookup table. Hence the name. It takes the following parameters:

* algorithms: A map of algorithm name to limit determining whether to include a term / variant pair. The algorithm names can be chosen from [this list](https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/similarity/package-summary.html). Alternatively, you can use "SoundexSimilarity"
* terms: A list of names to test against all variants
* variants: A list of names to test against all terms
* ignoreCase: ignore case when computing the distance (defaults to false)
* ignoreEquality: include term / variant pairs that are equal in the result

The soundex similarity is higher for a pair of strings that sound similar in the english language:

```javascript
$synonym({"SoundexSimilarity":2}, ["roast"], ["ghost", "boast", "hello"])
```

```json
[
  {
    "synonym": "ghost",
    "term": "roast",
    "algotithm": "SoundexSimilarity",
    "value": 3
  },
  {
    "synonym": "boast",
    "term": "roast",
    "algotithm": "SoundexSimilarity",
    "value": 3
  }
]
```

Note that the pair "roast" and "hello" do not sound alike and, therefore, is not included.

The Levenshtein Distance calculates the minimal number of edits that is required to transform one
string into the other. The smaller this number, the more similar the strings are.
This is a good metric to match strings despite typos.
Compared to apple, apples is included with a limit of 1, appl is too different and is not included.

```javascript
$synonym({"LevenshteinDistance": 1}, ["apple"], ["apples", "appl"])
```

```json
[
  {
    "synonym": "apples",
    "term": "apple",
    "algotithm": "LevenshteinDistance",
    "value": 1
  },
  {
    "synonym": "appl",
    "term": "apple",
    "algotithm": "LevenshteinDistance",
    "value": 1
  }
]
```

## Document to Text

In order to include PDF and Word documents into your AI / ML and Low Code processes, it is crucial to be able to convert these assets into plain text. This functionality is available in the Dasjoin NLP container. You can run it as follows:

```shell
docker run -p 8081:8081 dashjoin/nlp
```

The container offers a conversion service that can be called as follows:

```shell
curl -X 'POST' \
  'http://localhost:8081/function/run/doc2text' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '"https://pdfobject.com/pdf/sample.pdf"'
```

This example passes a URL to a PDF document. In turn, the extracted text is returned.

If the document is not available directly via a public URL, you can obtain it via some other means and pass it via a [data URL](https://developer.mozilla.org/en-US/docs/Web/URI/Schemes/data) as follows:

```shell
curl -X 'POST' \
  'http://localhost:8081/function/run/doc2text' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '"data:application/pdf;base64,PGh0bWw+PHA+bXkgdGl0bGU8L3A+PC9odG1sPg=="'
```

Note that the data URL has the prefix: data:application/pdf;base64,
The following data is a base 64 encoded HTML page.

In order to send a document from your upload folder, you can use the following JSONata code:

```
$curl("POST", "http://localhost:8081/function/run/doc2text", "data:application/pdf;base64," & $openText("file:upload/test.html", "BASE_64"))
```

The document is loaded via the openText function using base64 encoding. The data URL prefix is appended and fed into the curl command.
