import { Component, EventEmitter, Inject } from '@angular/core';
import { MatDialog, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Schema, WidgetComponent } from '@dashjoin/json-schema-form';

/**
 * json schema for component for editing a mapping
 */
@Component({
  selector: 'app-mapping',
  templateUrl: './mapping.component.html',
  styleUrls: ['./mapping.component.css']
})
export class MappingComponent implements WidgetComponent {

  /**
   * see WidgetComponent
   */
  label: string;

  /**
   * see WidgetComponent
   */
  value: { [key: string]: any };

  /**
   * see WidgetComponent
   */
  rootValue: any;

  /**
   * see WidgetComponent
   */
  valueChange: EventEmitter<{ [key: string]: any }> = new EventEmitter();

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
   * inject dialog
   */
  constructor(private dialog: MatDialog, private snackBar: MatSnackBar) { }

  /**
   * open editor button clicked
   */
  editor() {

    const dialogRef = this.dialog.open(MappingDialogComponent, { data: JSON.stringify(this.value, null, 2), minWidth: '98vw' });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        try {
          this.value = JSON.parse(result);
          this.valueChange.emit(this.value);
        } catch (e) {
          this.snackBar.open('Invalid JSON', 'Ok');
        }
      }
    });
  }

  /**
   * TODO: remove later
   */
  print(): string {
    return JSON.stringify(this.value, null, 2);
  }
}

/**
 * mapping dialog component
 */
@Component({
  templateUrl: './dialog.component.html',
  styleUrls: ['./mapping.component.css']
})
export class MappingDialogComponent {
  constructor(
    private dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }
}
