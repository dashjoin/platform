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

## Large Language Model

The chat and instruction functionality is available in the dashjoin/ai-llm container.
To start it, you need to mount your language model (e.g. the popular [llama model](https://github.com/facebookresearch/llama)) 
and a configuration into the container as follows. The file ts_server.cfg determines important parameters:

```json
{
  log_filename: "ts_server.log",
  /* if true, enable GPU usage */
  //  cuda: true,
  /* cuda device index, use it if multiple GPUs */
  //  device_index: 0,

  /* maximum number of threads, only matters when running on CPU */
  n_threads: 1,
  
  /* models to load. 'name' is the identifier used in the JSON
     request. 'filename' is the file containing the model description
   */
  models: [
    { name: "djmodel",  filename: "/opt/dashjoin/ai/model.bin" },
  ],
  local_port: 8080, /* port on which the server listen to */
  log_start: true, /* print "Started." when the server is ready */
  gui: true, /* start a simple GUI when exploring the root path
               (e.g. http://localhost:8080 here) (default = false) */
}
```

Start the docker container as follows:

```shell
docker run 
    -p 8080:8080 
    -v $PWD/ts_server.cfg:/opt/dashjoin/ai/ts_server.cfg
    -v $PWD/my_model.bin:/opt/dashjoin/ai/model.bin \
    dashjoin/ai-llm
```

To test the language model, create the following JSONata function (note that /model/ in the URL selects the 
model key in the config file, also note that you can deploy multiple models into the container):

```json
{
    "djClassName": "org.dashjoin.function.RestJson",
    "ID": "dashjoin-llm",
    "type": "read",
    "method": "POST",
    "contentType": "application/json",
    "url": "http://.../v1/engines/djmodel/completions",
    "returnText": true
}
```

To call the function use:

```javascript
$parseJson("[" & $replace($call("dashjoin-llm", {
  "prompt": "Game of Thrones is",
  "temperature": 1,
  "top_k": 40,
  "top_p": 0.9,
  "max_tokens": 200,
  "stream": true,
  "stop": null
}), /\}\s*\{/, "},{") & "]")
```

The result is a stream of concatenated JSON. $call returns this stream as a string.
The example uses JSONata regular expressions to convert this into a proper JSON
array of objects before parsing it via $parseJson.

```json
[
  ...
  {"text":" the things I liked about the show and thought","reached_end":false},
  {"text":" everyone else should be able to see. But","reached_end":false},
  {"text":" it’s also, in many","reached_end":false},
  {"text":" ways, kind of","reached_end":true,"input_tokens":4,"output_tokens":200},
]
```

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
