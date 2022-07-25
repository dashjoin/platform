import { Component, EventEmitter, OnInit } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { map, Observable, of } from 'rxjs';
import { ExpressionComponent } from './expression.component';

/**
 * custom component to edit expressions
 */
@Component({
  selector: 'app-stream-expression',
  templateUrl: './stream.expression.component.html',
  styleUrls: ['./expression.component.css']
})
export class StreamExpressionComponent extends ExpressionComponent implements OnInit {

  /**
   * cursor data
   */
  cursor: any;

  /**
   * entry form control (throttle evaluation while typing)
   */
  foreach = new UntypedFormControl();

  /**
   * setup form and handlers
   */
  ngOnInit(): void {

    if (!this.value) {
      this.value = {};
    }

    this.setup(this.value.expression, this.text, () => {
      if (this.value.foreach) {
        if (this.cursor) {
          return of({
            expression: '$.' + this.value.expression,
            data: this.cursor
          });
        } else {
          return this.http.post('/rest/expression-preview', {
            expression: this.value.foreach,
            data: null,
            foreach: true
          }).pipe(map(res => {
            this.cursor = res;
            return {
              expression: '$.' + this.value.expression,
              data: this.cursor
            }
          }))
        }
      } else {
        return of({
          expression: this.value.expression,
          data: JSON.parse(sessionStorage.context)
        });
      }
    }, event => {
      this.value.expression = event;
      this.valueChange.emit(this.value);
    })

    this.setup(this.value.foreach, this.foreach, () => {
      return of({
        expression: this.value.foreach,
        data: null,
        foreach: true
      });
    }, event => {
      this.value.foreach = event;
      this.valueChange.emit(this.value);
    }, res => this.cursor = res);
  }

  /**
   * open editor button clicked
   */
  editor() {

    let context: Observable<any>;
    if (this.value.foreach) {
      if (this.cursor) {
        context = of(this.cursor[0]);
      } else {
        context = this.http.post('/rest/expression-preview', {
          expression: this.value.foreach,
          data: null,
          foreach: true
        }).pipe(map(res => { return res[0] }));
      }
    } else {
      context = of(JSON.parse(sessionStorage.context));
    }

    context.subscribe(ctx => {
      const dialogRef = this.dialog.open(ExpressionComponent.dialogClass, {
        data: { expression: this.value.expression, context: ctx },
        minWidth: '98vw',
        minHeight: '98vh'
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.text.setValue(result.expression);
        }
      });
    });
  }

  /**
   * open editor button clicked
   */
  editorForeach() {

    const dialogRef = this.dialog.open(ExpressionComponent.dialogClass, {
      data: { expression: this.value.foreach, context: JSON.parse(sessionStorage.context), foreach: true },
      minWidth: '98vw',
      minHeight: '98vh'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.foreach.setValue(result.expression);
      }
    });
  }
}
