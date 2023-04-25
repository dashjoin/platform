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
    this.runExpression(line.expression)
      .then(res => {
        line.result = res
        this.saveState()
      })
      .catch(err => {
        line.result = err.error
        this.saveState()
      })
  }

  /**
   * add a new line
   */
  addLine() {
    this.lines.push({ expression: '' })
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
}

/**
 * line state
 */
class Line {
  expression: string
  result?: any
}