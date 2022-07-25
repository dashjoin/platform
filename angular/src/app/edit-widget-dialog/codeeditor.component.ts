import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { WidgetComponent, Schema } from '@dashjoin/json-schema-form';

@Component({
    templateUrl: 'codeeditor.component.html',
    styleUrls: ['codeeditor.component.css']
})
export class CodeEditorComponent implements WidgetComponent {

    constructor(public http: HttpClient, public dialog: MatDialog) { }

    /**
     * if set, code IDE is enabled
     */
    static dialogClass: any;

    /**
     * the name of the input field
     */
    label: string;

    /**
     * the input value
     */
    value: any;

    /**
     * root form value (can be used in custom components)
     */
    rootValue: any;

    /**
     * JSON schema to use
     */
    schema: Schema;

    /**
     * root JSON schema to use when looking up $ref (simply passed along the tree)
     */
    rootSchema: Schema;

    /**
     * emit changes done by the user in the component
     */
    valueChange: EventEmitter<string> = new EventEmitter();

    /**
     * emit validation errors
     */
    errorChange: EventEmitter<string> = new EventEmitter();

    ide() {
        return CodeEditorComponent.dialogClass;
    }

    editor() {
        const dialogRef = this.dialog.open(CodeEditorComponent.dialogClass, {
            data: {
                title: this.schema.title,
                code: this.value,
                options: (this.schema as any).widgetOptions
            },
            minWidth: '98vw',
            minHeight: '98vh'
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.onChange(result);
            }
        });
    }

    onChange(value) {
        this.value = value;
        this.valueChange.emit(this.value);
    }
}