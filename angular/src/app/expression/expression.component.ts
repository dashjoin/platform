import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Schema, WidgetComponent } from '@dashjoin/json-schema-form';
import { debounceTime, map } from 'rxjs/operators';

/**
 * custom component to edit expressions
 */
@Component({
  selector: 'app-expression',
  templateUrl: './expression.component.html',
  styleUrls: ['./expression.component.css']
})
export class ExpressionComponent implements WidgetComponent, OnInit {

  /**
   * constructor
   * @param http needed for backend evaluations
   */
  constructor(private http: HttpClient, private dialog: MatDialog) { }

  /**
   * if set, jsonata IDE is enabled
   */
  static dialogClass: any;

  /**
   * expression field label
   */
  label: string;

  /**
   * expression value
   */
  value: any;

  /**
   * see WidgetComponent
   */
  rootValue: any;

  /**
   * notify parent of edits
   */
  valueChange: EventEmitter<object> = new EventEmitter();

  /**
   * see WidgetComponent
   */
  errorChange: EventEmitter<string> = new EventEmitter();

  /**
   * form schema / layout info
   */
  schema: Schema;

  /**
   * root schema
   */
  rootSchema: Schema;

  /**
   * provide the user feedback about errors / hints
   */
  message: any;

  /**
   * entry form control (throttle evaluation while typing)
   */
  text = new FormControl();

  /**
   * setup form and handlers
   */
  ngOnInit(): void {
    if (!this.value) {
      // value if not set, do nothing
    } else {
      this.text.setValue(this.value);
      this.setMessage('');
    }
    this.text.valueChanges.pipe(
      map(event => {
        try {
          this.value = event;
          this.valueChange.emit(this.value);
        } catch (e) {
          this.setMessage(e.message);
          this.value = event;
          this.valueChange.emit(this.value);
        }
      }),
      debounceTime(500)
    ).subscribe(_ => {
      this.http.post('/rest/expression-preview', {
        expression: this.value,
        data: JSON.parse(sessionStorage.context)
      }).subscribe(res => {
        this.setMessage(JSON.stringify(res, null, 2));
      }, error => {
        const line = error.error?.lastIndexOf('line 1:');
        if (line >= 0) {
          const end = error.error.indexOf('at', line);
          if (end >= 0) {
            const pos = '-'.repeat(Number.parseInt(error.error.substring(line + 7, end), 10)) + '^\n\n';
            this.setMessage(pos + error.error);
            return;
          }
        }
        this.setMessage(error.error);
      });
    });
  }

  /**
   * used to show edit button ngIf
   */
  ide(): boolean {
    return ExpressionComponent.dialogClass;
  }

  /**
   * set the message but limit and format output
   * @param message msg to display
   */
  setMessage(message: string) {
    const lines = message.split('\n');
    while (lines.length < 10) {
      lines.push('');
    }
    let res = '';
    for (const l of lines.slice(0, 10)) {
      res += l + '\n';
    }
    this.message = res;
  }

  /**
   * open editor button clicked
   */
  editor() {

    const dialogRef = this.dialog.open(ExpressionComponent.dialogClass, {
      data: this.value,
      minWidth: '98vw',
      minHeight: '98vh'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.text.setValue(result);
      }
    });
  }
}
