import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Title } from '@angular/platform-browser';
import { WidgetComponent, Schema } from '@dashjoin/json-schema-form';

@Component({
    templateUrl: 'query-select.component.html',
    styleUrls: ['query-select.component.css']
})
export class QuerySelectComponent implements WidgetComponent {

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

    /**
     * json schema for GUI
     */
    myschema = {
        static: true,
        title: 'Query ID from the catalog',
        type: 'string',
        displayWith: 'fk',
        ref: 'dj/config/dj-query-catalog/ID'
    };

    /**
     * once we start editing, load the names of the existing queries so we can decide whether to show 'add' or 'edit'
     */
    loading = false;

    /**
     * names of the existing queries
     */
    queryNames = [];

    /**
     * constructor
     * @param http client
     */
    constructor(private http: HttpClient, private snackBar: MatSnackBar, private titleService: Title) { }

    /**
     * component change handler
     */
    onChange(event: any) {
        if (this.value !== event && !this.loading) {
            this.loading = true;
            this.http.post<any[]>('/rest/database/all/config/dj-query-catalog', {}).subscribe((res) => {
                for (const r of res) {
                    this.queryNames.push(r.ID);
                }
            });
        }
        this.value = event;
        this.valueChange.emit(this.value);
    }

    /**
     * create query, then open
     */
    add() {

        if (!this.value) {
            // come up with a good default name
            let suggest;

            // get current form value via DOM
            const labels = document.getElementsByTagName('mat-label');
            for (let i = 0; i < labels.length; i++) {
                const l = labels.item(i);
                if (l.firstChild.textContent === 'Element title on the page') {
                    suggest = (l?.parentElement?.parentElement?.previousElementSibling as any)?.value;
                }
            }

            // card title not set
            if (!suggest) {
                if (window.location.hash.startsWith('#/resource/')) {
                    const parts = window.location.hash.split('/');
                    parts.pop();
                    suggest = parts.pop();
                } else {
                    suggest = this.titleService.getTitle();
                }
            }

            // strip spaces
            suggest = suggest.replaceAll(' ', '-');

            // append number until unique
            if (this.queryNames.includes(suggest)) {
                let counter = 1;
                while (this.queryNames.includes(suggest + counter)) {
                    counter++;
                }
                suggest = suggest + counter;
            }

            this.value = suggest;
            this.valueChange.emit(this.value);
        }

        const query = window.location.hash.startsWith('#/resource/') ? {
            ID: this.value,
            type: 'read',
            query: 'select ${ID}',
            arguments: {
                ID: {
                    type: 'string',
                    sample: 'sample'
                }
            }
        } : {
            ID: this.value,
            type: 'read'
        };

        this.http.put('/rest/database/crud/config/dj-query-catalog', query, { headers: { 'Content-Type': 'application/json' }, responseType: 'text' }).subscribe((res) => {
            this.queryNames.push(this.value);
            window.open('/#/resource/config/dj-query-catalog/' + this.value, '_blank');
        }, (error) => {
            this.snackBar.open('Error creating query "' + this.value + '"', 'Ok');
        });
    }

    /**
     * open in new tab
     */
    edit() {
        window.open('/#/resource/config/dj-query-catalog/' + this.value, '_blank');
    }

    /**
     * add or edit query?
     */
    isnew(): boolean {
        if (this.queryNames.length > 0) {
            // query names were loaded, name in list?
            return !this.queryNames.includes(this.value);
        } else {
            // not yet loaded, name empty?
            return !this.value;
        }
    }
}
