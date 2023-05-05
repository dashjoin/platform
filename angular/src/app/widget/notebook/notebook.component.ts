import { Component, OnInit } from '@angular/core';
import { DashjoinWidget } from '../widget-registry';
import { DisplayComponent } from '../display/display.component';
import { HttpHeaders } from '@angular/common/http';

/**
 * notebook widget
 */
@DashjoinWidget({
  name: 'notebook',
  category: 'Default',
  description: 'Dashjoin JSONata Notebook',
  htmlTag: 'dj-notebook',
  fields: []
})
@Component({
  selector: 'app-notebook',
  templateUrl: './notebook.component.html',
  styleUrls: ['./notebook.component.css']
})
export class NotebookComponent extends DisplayComponent implements OnInit {

  /**
   * notebook state
   */
  lines: Line[] = []

  /**
   * init and load state
   */
  ngOnInit(): void {
    super.ngOnInit()
    this.loadState()
  }

  /**
   * load notebook state from session storage or widget layout
   */
  loadState() {
    const id = 'djnotebook-' + this.database
    const session = sessionStorage.getItem(id)
    if (session)
      this.lines = JSON.parse(session)
    else if (this.layout.columns)
      this.lines = this.layout.columns.map(item => { return { expression: item } })
    else
      this.lines = []
    if (this.lines.length === 0)
      this.lines.push({ expression: '' })
  }

  /**
   * write state to session store
   */
  saveState() {
    const id = 'djnotebook-' + this.database
    sessionStorage.setItem(id, JSON.stringify(this.lines))
  }

  /**
   * run command
   */
  run(line: Line, i: number) {
    this.runExpression(this.vars(line))
      .then(res => {
        line.result = res
        this.saveState()

        // parse variable
        line.variable = undefined
        if (line.expression) {
          let v = line.expression.trim()
          if (v.startsWith('$'))
            v = v.substring(1)
          const parts = v.split(':=')
          if (parts.length > 1) {
            if (/^[0-9a-zA-Z_]+$/.test(parts[0].trim()))
              line.variable = parts[0].trim()
          }
        }
      })
      .catch(err => {
        line.result = err.error
        this.saveState()
      })
  }

  /**
   * replace $var with notebook.var
   */
  vars(l: Line): string {
    let e = l.expression
    for (const line of this.lines)
      if (line.variable && line != l) {
        // tokenize by $var
        const parts = e.split('$' + line.variable)
        const nonPrefix = [parts[0]]
        for (let i = 1; i < parts.length; i++) {
          const first = parts[i].charAt(0)
          // check if the string after the $var split starts with letter digit or _
          if (/^[0-9a-zA-Z_]$/.test(first))
            // no, something line $var2 undo the split
            nonPrefix[nonPrefix.length - 1] = nonPrefix[nonPrefix.length - 1] + '$' + line.variable + parts[i]
          else
            nonPrefix.push(parts[i])
        }
        e = nonPrefix.join('notebook.' + line.variable)
      }
    return e
  }

  /**
   * add a new line
   */
  addLine() {
    this.lines.push({ expression: '' })
    this.saveState()
  }


  /**
   * add a line with upload content
   */
  upload(files: FileList) {
    const result = {}
    for (let i = 0; i < files.length; i++) {
      const reader = new FileReader();
      reader.onload = () => {
        const name = files.item(i).name.replace('.', '_')
        try {
          result[name] = JSON.parse(reader.result as any)
        } catch (err) {
          result[name] = reader.result
        }
      }
      if (files.item(i).name.toLocaleLowerCase().endsWith('.xlsx'))
        reader.readAsDataURL(files.item(i))
      else
        reader.readAsText(files.item(i))
    }

    this.lines.push({ expression: '$upload := ...', result, variable: 'upload', upload: true })
    this.saveState()
  }

  /**
   * delete line
   */
  deleteLine(index: number) {
    this.lines.splice(index, 1)
    this.saveState()
  }

  /**
   * save notebook
   */
  write() {
    return this.http.post<any>('/rest/database/crud/config/page/' + encodeURIComponent(this.database), {
      ID: this.database,
      layout: {
        widget: 'page',
        pageLayout: 'horizontal',
        children: [{
          widget: 'notebook',
          title: 'Dashjoin Notebook',
          columns: this.lines.map(line => line.expression)
        }]
      }
    }, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    }).subscribe(res => this.snackBar.open('Done', 'Ok', { duration: 3000 }), this.errorHandler)
  }

  /**
   * restore notebook to on disk state
   */
  restoreNotebook() {
    const id = 'djnotebook-' + this.database
    sessionStorage.removeItem(id)
    this.loadState()
  }

  /**
   * json pretty print
   */
  print(line: Line) {
    return JSON.stringify(line.result, null, 2)
  }

  /**
   * handle ctrl enter
   */
  triggerFunction(event, i: number) {
    if (event.ctrlKey && event.key === 'Enter') {
      this.run(this.lines[i], i)
    }
  }

  /**
   * overrides get context - include variable from other lines
   */
  context() {
    const res = super.context()
    res.notebook = {}
    for (const line of this.lines)
      if (line.variable)
        res.notebook[line.variable] = line.result
    return res
  }
}

/**
 * line state
 */
class Line {

  /**
   * expression for this line
   */
  expression: string

  /**
   * expression result (undefined if it has not been run)
   */
  result?: any

  /**
   * if the expression starts with $var := , this value is "var"
   */
  variable?: string

  /**
   * is this an uploaded line?
   */
  upload?: boolean
}