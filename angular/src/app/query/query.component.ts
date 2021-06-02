import { Component, OnInit, EventEmitter, Inject } from '@angular/core';
import { WidgetComponent, Schema } from '@dashjoin/json-schema-form';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

/**
 * custom json-schema-form component that allows editing the query and popping up the query editor as a dialog
 */
@Component({
  selector: 'app-query',
  templateUrl: './query.component.html',
  styleUrls: ['./query.component.css']
})
export class QueryComponent implements WidgetComponent, OnInit {

  /**
   * see WidgetComponent
   */
  label: string;

  /**
   * see WidgetComponent
   */
  value: { ID: string, database: string, query: string };

  /**
   * see WidgetComponent
   */
  rootValue: any;

  /**
   * see WidgetComponent
   */
  valueChange: EventEmitter<{ database: string, query: string }> = new EventEmitter();

  /**
   * see WidgetComponent
   */
  errorChange: EventEmitter<string> = new EventEmitter();

  /**
   * see WidgetComponent
   */
  schema: Schema;

  /**
   * see WidgetComponent
   */
  rootSchema: Schema;

  /**
   * holds the result of order computation in order to decide which field shows up
   */
  orderedProperties: any;

  /**
   * inject dialog
   */
  constructor(private dialog: MatDialog) { }

  /**
   * only show query related fields
   */
  ngOnInit(): void {
    this.orderedProperties = [];
    for (const [key, value] of Object.entries(this.schema.properties)) {
      if (key !== 'query') {
        const o = {};
        o[key] = value;
        this.orderedProperties.push(o);
      }
    }
    setTimeout(() => {
      this.errorChange.emit(this.value?.ID ? null : 'ID required');
    }, 0);
  }

  /**
   * change detected in the text field, emit to parent
   */
  change(query: { database: string, query: string }) {
    (this.value as any).query = query;
    this.valueChange.emit(this.value);
  }

  /**
   * change in the other form elements, make sure we emit the change in order to update the state of the submit button
   */
  formChange(key: string, event: any) {
    this.value[key] = event;
    this.valueChange.emit(this.value);
    this.errorChange.emit(this.value?.ID ? null : 'ID required');
  }

  /**
   * open editor button clicked
   */
  editor() {

    const base = window.prompt('Enter the name of the database to query', this.value.database ? this.value.database : '');
    if (base) {
      this.value.database = base;
      this.valueChange.emit(this.value);
    }
  }
}
