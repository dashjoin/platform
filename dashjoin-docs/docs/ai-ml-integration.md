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
or by pressing CTRL ENTER. The result of the call is also stored in die notebook and displayed below the
code block.

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

The optical character recognition (OCR) functionality is available as a Docker container. It exposes a
REST API where you provide an image URL such as this picture (https://d207ibygpg2z1x.cloudfront.net/image/upload/v1540973697/articles_upload/content/ibttqvywe6gihhcu1zzf.jpg).

<img width="400px" src="https://d207ibygpg2z1x.cloudfront.net/image/upload/v1540973697/articles_upload/content/ibttqvywe6gihhcu1zzf.jpg">

The URL is passed in a GET request https://.../image-ocr?url=... and returns the extracted text:

```json
$parseJson($openJson("https://.../image-ocr?url=https://d207ibygpg2z1x.cloudfront.net/image/upload/v1540973697/articles_upload/content/ibttqvywe6gihhcu1zzf.jpg"))
```

```json
"HEY YOU YES YOU\n\nYOU CANDO IT\n"
```

## Image Classification

The image classification functionality is available as a Docker container. It exposes a
REST API where you provide an image URL such as this picture of a bird (https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg).

<img width="400px" src="https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg">

The URL is passed in a GET request https://.../image-classify?url=... and returns an array of
classifications and probabilities:

```json
$parseJson($openJson("https://.../image-classify?url=https://mein-vogelhaus.com/wp-content/uploads/2020/04/Einheimische-Vogelarten-Stieglitz.jpg")).{"type": $[1], "prob": $[2]}
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

The optical character recognition (OCR) functionality is available as a Docker container. It exposes a
REST API where you provide an image URL such as this picture (https://media-cldnry.s-nbcnews.com/image/upload/newscms/2020_02/1521975/kristen-welker-today-191221-main-01.jpg).

<img width="400px" src="https://media-cldnry.s-nbcnews.com/image/upload/newscms/2020_02/1521975/kristen-welker-today-191221-main-01.jpg">

The URL is passed in a GET request https://.../image-face?url=... and returns the extracted text along with the
coordinates of the face within the image:

```json
$parseJson($openJson("https://ml-img-classify-qtmq6xeijq-ew.a.run.app/image-face?url=https://media-cldnry.s-nbcnews.com/image/upload/newscms/2020_02/1521975/kristen-welker-today-191221-main-01.jpg"))
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

## Large Language Model

## Entity Reconciliation

## Entity Classification

