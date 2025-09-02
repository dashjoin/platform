# Appendix: JSONata function library
## confirm
displays a confirmation dialog and returns true if the user clicked yes
```
$confirm('Are you sure') ? 'you clicked yes'
```
## setVariable
Sets the browser session variable x to 1. The new value can be read in other places via variable.x
```
$setVariable('x', 1)
```
## prompt
Prompts the user for an input. Returns the input or undefined if the prompt is cancelled
```
(
  $name := $prompt('What is your name?');
  $name ? ('Hello ' & $name);
)
```
## alert
Shows a model popup with a message
```
$alert('FYI')
```
## dialog
Shows a dialog to enter 'to' and 'text'. If the user submits via 'ok', an object with these fields is returned
```
$dialog({
  'title':'Send Message', 
  'message':'Please enter your message', 
  'inputs':['to', 'content'], 
  'buttons':[{'label':'ok', 'type':'submit'},'cancel'], 
  'options':{'alert':'info'}
})
```
## notify
Shows a message in the snackbar at the bottom of the screen
```
$notify('Hi from the snackbar!')
```
## refresh
refreshes the screen just (just like hitting the refresh icon in the toolbar)
```
$refresh()
```
## reload
reloads the browser page
```
$reload()
```
## navigate
points the browser to the URL
```
$navigate('http://dashjoin.com')
```
## navigate
scrolls the page to the DOM element with the given id. In this case, we scroll to the widget that has the title 'Top'
```
$navigate({'id': 'dj-Top'})
```
## clearCache
clears the HTTP cache - can be used in conjunction with expressions that trigger side effects on the backend
```
$clearCache()
```
## progress
shows a progress indicator with the message 'working...' for 1 second until the expression execution completes
```
(
  $progress({'message':'working...'});
  $sleep(1000)
)
```
## sleep
sleeps for 1000 milliseconds
```
$sleep(1000)
```
## speak
performs a text to english speech for 'Test'
```
$speak('Test', 'en')
```
## stopSpeech
stops any text to speech that's in progress
```
$stopSpeech()
```
## translate
Lookup the text in the translation file and return the match for the current locale
```
$translate('ra.action.select_all_button')
```
