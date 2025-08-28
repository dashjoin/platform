# Appendix: Form Input Types
These are possible children of the create, edit, button, and variable widgets.## boolean input
Boolean inputs show a on/off toggle. In this example, the toggle has the label 'Opt in'
```json
{
  "widget" : "boolean",
  "title" : "Opt in"
}
```
Sample output: 
```json
true
```
## string input
String inputs show a text input
```json
{
  "widget" : "string"
}
```
Sample output: 
```json
"test"
```
## number input
Number inputs show a text input that is restricted to entering numbers
```json
{
  "widget" : "number"
}
```
Sample output: 
```json
123
```
## autocomplete input
Autocomplete inputs show a text input that autocompletes to the options specified in the jsonata expression
```json
{
  "widget" : "auto complete",
  "options" : "['one', 'two', 'three']"
}
```
Sample output: 
```json
"one"
```
## select input
Select inputs show a drop-down selection with the options specified in the jsonata expression
```json
{
  "widget" : "select",
  "options" : "['one', 'two', 'three']"
}
```
Sample output: 
```json
"one"
```
## multi select input
Multi Select inputs show a drop-down selection with the options specified in the jsonata expression. Multiple options can be selected yielding an array of values
```json
{
  "widget" : "multi select",
  "options" : "['one', 'two', 'three']"
}
```
Sample output: 
```json
[ "one", "two" ]
```
## key value input
key value inputs display a two column table to enter keys and values. Returns a JSON object
```json
{
  "widget" : "key value"
}
```
Sample output: 
```json
{
  "key" : "value"
}
```
## password input
Shows a password input field
```json
{
  "widget" : "password"
}
```
Sample output: 
```json
"secret"
```
## textarea input
Shows a multi line input field (textarea)
```json
{
  "widget" : "textarea"
}
```
Sample output: 
```json
"line1\nline2"
```
## date input
Shows a date picker input. Returns a YYYY-MM-DD string with the selected date
```json
{
  "widget" : "date"
}
```
Sample output: 
```json
"2025-08-08"
```
## time input
Shows a time picker input. Returns a ISO 8601 date string with the current day and the selected time
```json
{
  "widget" : "time"
}
```
Sample output: 
```json
"2025-08-28T04:03:05.972Z"
```
## datetime input
Shows a datetime picker input
```json
{
  "widget" : "datetime"
}
```
Sample output: 
```json
"2025-08-28T04:03:05.972Z"
```
## file input
Allows uploading a file. returns the file contents
```json
{
  "widget" : "file"
}
```
Sample output: 
```json
"file contents"
```
## binary file input
Allows uploading a file. returns the file contents as a base 64 encoded data URL
```json
{
  "widget" : " binary file"
}
```
Sample output: 
```json
"data:text/plain;base64,ZmlsZSBjb250ZW50cw=="
```
## file with metadata input
Allows uploading a file. returns an object with name, lastModified, size, type, and value (containing the file contents)
```json
{
  "widget" : "file with metadata"
}
```
Sample output: 
```json
{
  "name" : "test.txt",
  "lastModified" : 1756398020293,
  "size" : 13,
  "type" : "text/plain",
  "value" : "file contents"
}
```
## binary file with metadata input
Allows uploading a file. returns an object with name, lastModified, size, type, and value (containing the file contents as a base 64 encoded data URL)
```json
{
  "widget" : " binary file with metadata"
}
```
Sample output: 
```json
{
  "name" : "test.txt",
  "lastModified" : 1756398020293,
  "size" : 13,
  "type" : "text/plain",
  "value" : "data:text/plain;base64,ZmlsZSBjb250ZW50cw=="
}
```
## voice input
Shows a text input field with a voice option
```json
{
  "widget" : " voice"
}
```
Sample output: 
```json
"you spoke into the mic"
```
## qrcode input
Allows scanning a QR Code into the form field
```json
{
  "widget" : " qrcode"
}
```
Sample output: 
```json
"whatever was encoded in the QR code"
```
