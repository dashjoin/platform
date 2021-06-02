import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Choice, ChoiceHandler, Schema } from '@dashjoin/json-schema-form';
import { Observable, of, pipe } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from '../app.service';
import { Util } from '../util';

/**
 * handles autocomplete for editing foreign keys
 */
export class ForeignKeyChoiceHandler implements ChoiceHandler {

    /**
     * makes backend requests
     */
    http: HttpClient;

    /**
     * allows looking up labels
     */
    app: AppService;

    /**
     * indicates whether the field was activated (via focus event)
     */
    loaded = false;

    /**
     * constructor
     */
    constructor(http: HttpClient, app: AppService) {
        this.app = app;
        this.http = http;
    }

    /**
     * initial option
     */
    load(value: any, schema: Schema): Observable<Choice[]> {
        this.loaded = true;
        return this.filter(value, schema, value, null);
    }

    /**
     * delegate to REST keys service
     */
    filter(value: any, schema: Schema, current: string, choices: Observable<Choice[]>): Observable<Choice[]> {
        if (!this.loaded) {
            return choices;
        }
        const parts = Util.parseColumnID((schema as any).ref);
        if (!current) { current = ''; }
        return this.http.get<Choice[]>('/rest/database/keys/' + parts[1] + '/' + parts[2] + '?limit=10&prefix=' + current, {
            headers: new HttpHeaders({ 'Content-Type': 'application/json' })
        });
    }

    /**
     * initial option
     */
    choice(value: any, schema: Schema): Observable<Choice> {
        const link = (schema as any).ref.split('/');
        // replace dj with /resource
        link[0] = '/resource';
        // replace ID with value
        link[3] = value;
        return this.app.getIdLabel(link).pipe(map(name => ({ name, value })));
    }

    /**
     * request is sent once there is no key stroke for half a sec
     */
    debounceTime(): number {
        return 500;
    }
}

/**
 * like ForeignKeyChoiceHandler but strips dj/ prefix and omits config DB
 */
export class DatabaseNameChoiceHandler extends ForeignKeyChoiceHandler {

    /**
     * call super implementation and map using stripDjArr
     */
    filter(value: any, schema: Schema, current: string, choices: Observable<Choice[]>): Observable<Choice[]> {
        return super.filter(value, schema, current, choices).pipe(map(arr => this.stripDjArr(arr)));
    }

    /**
     * converts Choice[] by stripping dj/
     */
    stripDjArr(arr: Choice[]): Choice[] {
        return arr.map(c => this.stripDj(c)).filter(c => c.name !== 'config');
    }

    /**
     * converts Choice by stripping dj/
     */
    stripDj(c: Choice): Choice {
        if (c.name?.startsWith('dj/')) {
            return { name: c.name?.substring(3), value: c.value?.substring(3) };
        } else {
            return c;
        }
    }
}
