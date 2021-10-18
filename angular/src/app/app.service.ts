import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { from, Observable } from 'rxjs';
import { map, publishReplay, refCount, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Util } from './util';
import { DJRuntimeService } from './djruntime.service';
import { DepInjectorService } from './dep-injector.service';
import { Schema } from '@dashjoin/json-schema-form';

/**
 * caching services and authentication guard for all routes except login and logout
 */
@Injectable({
  providedIn: 'root'
})
export class AppService implements CanActivate {

  /**
   * need routing and communication
   */
  constructor(private http: HttpClient, private router: Router) {
    this.initWidgets();
  }

  /**
   * make which pages were edited
   */
  dirtyLayouts = { page: {}, widget: {}, schema: {} };

  /**
   * layout editor active
   */
  editLayout = false;

  /**
   * sidenav toggle
   */
  sidenavOpen = window.innerWidth > 700;

  /**
   * layout clipboard
   */
  clipboard: any;

  /**
   * http cache for REST request on config/Table (i.e. schema requests)
   */
  cache: { [key: string]: Observable<any> } = {};

  /**
   * http cache for REST request on config/widget (i.e. layout requests)
   */
  widget: { [key: string]: Observable<any> } = {};

  /**
   * http cache for REST request on config/page (i.e. layout requests)
   */
  page: { [key: string]: Observable<any> } = {};

  /**
   * label cache
   */
  labels: { [key: string]: Observable<string> } = {};
  labels2: { [key: string]: Observable<string> } = {};

  labelsProm: { [key: string]: Promise<string> } = {};

  /**
   * the first time we access widgets, make sure they are loaded from the backend
   */
  widgetsLoaded = false;

  /**
   * the first time we access widgets, make sure they are loaded from the backend
   */
  private initWidgets() {
    if (this.widgetsLoaded) {
      return;
    }
    this.http.get<any>('/rest/database/crud/config/widget').subscribe(res => {
      for (const w of res) {
        this.widget[w.ID] = of(w);
      }
      this.widgetsLoaded = true;
    }, error => console.error(error));
  }

  /**
   * get schema observable from cache or http
   */
  getSchema(database: string, table: string): Observable<any> {
    if (table == null) {
      return of(null);
    }
    const type = 'dj' + '/' + database + '/' + Util.encodeTableOrColumnName(table);
    if (!this.cache[type]) {
      this.cache[type] = this.http.get<any>('/rest/database/crud/config/Table/' + encodeURIComponent(type))
        .pipe(publishReplay(1), refCount());
    }
    return this.cache[type];
  }

  /**
   * get layout observable from cache or http
   */
  getWidget(widget: string): Observable<any> {
    if (!this.widget[widget]) {
      this.widget[widget] = this.http.get<any>('/rest/database/crud/config/widget/' + encodeURIComponent(widget))
        .pipe(publishReplay(1), refCount());
    }
    return this.widget[widget];
  }

  /**
   * get layout observable from cache or http
   */
  getPage(page: string): Observable<any> {
    this.log(page);
    if (!this.page[page]) {
      this.page[page] = this.http.get<any>('/rest/database/crud/config/page/' + encodeURIComponent(page))
        .pipe(publishReplay(1), refCount());
    }
    return this.page[page];
  }

  /**
   * save schema
   */
  setSchema(database: string, table: string): Observable<any> {
    return this.getSchema(database, table).pipe(switchMap(save => {
      delete save.order;
      const type = 'dj' + '/' + database + '/' + Util.encodeTableOrColumnName(table);
      return this.http.post<any>('/rest/database/crud/config/Table/' + encodeURIComponent(type), save, {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      });
    }));
  }

  /**
   * save layout
   */
  setWidget(widget: string): Observable<any> {
    return this.getWidget(widget).pipe(switchMap(save => {
      this.log(save);
      return this.http.post<any>('/rest/database/crud/config/widget/' + encodeURIComponent(widget), save, {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      });
    }));
  }

  /**
   * save layout
   */
  setPage(page: string): Observable<any> {
    return this.getPage(page).pipe(switchMap(save => {
      this.log(save);
      if (save.layout) {
        return this.http.post<any>('/rest/database/crud/config/page/' + encodeURIComponent(page), save, {
          headers: new HttpHeaders({ 'Content-Type': 'application/json' })
        });
      } else {
        delete this.page[page];
        return this.http.delete('/rest/database/crud/config/page/' + encodeURIComponent(page));
      }
    }));
  }

  /**
   * layouts are stored in three structures: 1) include widgets, 2) dashboard pages and 3) table metadata.
   * This method chooses the appropriate structure based on the caller's parameters
   *
   * @param mode layout mode such as page, widget, default, etc.
   * @param name layout name (e.g. widget name)
   * @param database current database (for looking up the table schema)
   * @param table current table (for looking up the table schema)
   * @param pk1 current object pk (for looking up the table schema)
   * @param pos position of this component in the current layout
   */
  getLayout(mode: string, name: string, database: string, table: string, pk1: string, pos: string): { o: Observable<any>, path: string } {
    switch (mode) {
      case 'page': {
        return { o: this.getPage(name), path: 'layout' };
      }
      case 'widget': {
        return { o: this.getWidget(name), path: 'layout' };
      }
      case 'instance': {
        return { o: this.getSchema(database, table), path: 'instanceLayout' };
      }
      case 'default': {
        return { o: this.getPage('default'), path: 'layout' };
      }
      case 'table': {
        return { o: this.getSchema(Util.parseTableID(pk1)[1], Util.parseTableID(pk1)[2]), path: 'tableLayout' };
      }
      case 'defaulttable': {
        return { o: this.getSchema('config', 'Table'), path: 'instanceLayout' };
      }
      default: {
        throw new Error('illegal layout option: ' + mode);
      }
    }
  }

  /**
   * marks a layout to be "dirty", as in changed and needs to be saved
   */
  dirty(mode: string, name: string, database: string, table: string, pk1: string, pos: string): void {
    switch (mode) {
      case 'page': {
        this.dirtyLayouts.page[name] = name;
        return;
      }
      case 'widget': {
        this.dirtyLayouts.widget[name] = name;
        return;
      }
      case 'instance': {
        this.dirtyLayouts.schema[database + '/' + table] = { database, table };
        return;
      }
      case 'default': {
        const x = 'default';
        this.dirtyLayouts.page[x] = 'default';
        return;
      }
      case 'table': {
        database = Util.parseTableID(pk1)[1];
        table = Util.parseTableID(pk1)[2];
        this.dirtyLayouts.schema[database + '/' + table] = { database, table };
        return;
      }
      case 'defaulttable': {
        database = 'config';
        table = 'Table';
        this.dirtyLayouts.schema[database + '/' + table] = { database, table };
        return;
      }
      default: {
        throw new Error('illegal layout option: ' + mode);
      }
    }
  }

  getDirtyChanges() {
    const changes = [];
    for (const key of Object.keys(this.dirtyLayouts.page)) {
      changes.push(key);
    }
    for (const key of Object.keys(this.dirtyLayouts.widget)) {
      changes.push(key);
    }
    for (const key of Object.keys(this.dirtyLayouts.schema)) {
      changes.push(key);
    }
    return changes;
  }

  /**
   * get label observable for an entire object from cache or http.
   * if not present, compute it using the dj-label template from the schema
   */
  getObjectLabel(database: string, table: string, ids: string[], object: any): Observable<string> {

    if (true) return this.getObjectLabelNG(database, table, ids, object);

    // compute the cache pk
    const pk = encodeURIComponent(database) + '/' + encodeURIComponent(table) + '/' + ids.map(id => encodeURIComponent(id)).join('/');

    if (!object) {
      // dashboard page like /page/Info
      const parts = this.router.url.split('/');
      return of(parts.pop());
    }
    if (!this.labels[pk]) {
      this.labels[pk] = new Observable((observer) => {
        const schema = this.getSchema(database, table);
        schema.subscribe(s => {
          let label: string = s['dj-label'];

          if (label) {
            console.log('labelDef', label);
            label = label.replace(/\${([^{}]*)}/g, (x) => {
              x = x.substring(2);
              x = x.substring(0, x.length - 1);
              if (x.startsWith('*'))
                x = x.substring(1);
              return object[x];
            });
          } else {
            label = this.defaultLabel(ids);
          }

          observer.next(label);
          observer.complete();
        });
      });
    }
    return this.labels[pk];
  }


  async getObjectLabelAsync(database: string, table: string, ids: string[], object: any,
    ownType?: string, loadObject = false): Promise<string> {

    // console.log('objLabelAsync', database, table, ids);

    // compute the cache pk
    const pk = (ownType ? ownType + '#' : '') +
      encodeURIComponent(database) + '/' + encodeURIComponent(table) + '/' + ids.map(id => encodeURIComponent(id)).join('/');
    // console.log('labelKey', pk);
    if (!object && !loadObject) {
      // dashboard page like /page/Info
      const parts = this.router.url.split('/');
      return parts.pop();
    }

    const rv = this.labelsProm[pk];
    if (rv) return rv;

    const relatedKeys = [];

    let label: string;
    let schema: any;
    try {
      schema = await this.getSchema(database, table).toPromise();
      label = schema['dj-label'];
    } catch (ex) {
      // ignore
    }

    if (label) {

      if (loadObject && !object) {
        const data = 'dj/' + encodeURIComponent(database) + '/' + encodeURIComponent(table);
        const key = ids.map(id => encodeURIComponent(id)).join('/');

        const d = this.runtime.getData(data);
        try {
          object = await d.read(key);
        } catch (ex) {
          // ignore
        }
      }

      // console.log('labelDef', label);
      if (!object)
        label = this.defaultLabel(ids);
      else
        label = label.replace(/\${([^{}]*)}/g, (x) => {
          x = x.substring(2);
          x = x.substring(0, x.length - 1);
          if (!x.startsWith('*'))
            return object[x];

          // Resolve ${*property}
          // Replace with __<num>__ which will later be replaced with the real value
          x = x.substring(1);

          const res = object[x];
          const prop = schema?.properties[x];
          const related = prop.ref;

          // If the related key is of our ownType,
          // remove it from the label
          if (related.startsWith(ownType))
            return '';

          // console.log('relatedprop ' + related, s);
          if (related) {
            const arr = related.split('/');
            arr[3] = res;
            relatedKeys.push(arr);
            return '__' + relatedKeys.length + '__';
          }
          return res;
        });
    } else {
      label = this.defaultLabel(ids);
    }

    let count = 1;
    // Replace __<num>__ placeholders with related labels that we gather async
    while (relatedKeys.length > 0) {
      const key = relatedKeys.shift();
      let relatedLabel: string;
      try {
        relatedLabel = await this.getIdLabelNG(key).toPromise();
      } catch (ex) {
        relatedLabel = key;
      }
      label = label.replace('__' + count + '__', relatedLabel);
      count++;
    }

    this.labelsProm[pk] = Promise.resolve(label);
    return this.labelsProm[pk];
  }


  getObjectLabelNG(database: string, table: string, ids: string[], object: any,
    ownType?: string, resolve = false): Observable<string> {
    return from(this.getObjectLabelAsync(database, table, ids, object, ownType, resolve))
      .pipe(publishReplay(1), refCount());
  }

  /**
   * get the label for the ID (used in table display where the object is not entirely present)
   */
  getIdLabel(link: string[]): Observable<string> {
    // sometimes, the link part array is ['', 'resource', ...] instead of ['/resource', ...] throwing off the indices
    if (link[0] === '') {
      link.shift();
      link[0] = '/' + link[0];
    }

    let database = null;
    let table = null;
    if (link.shift() === '/table') {
      // /table/dbname/tablename becomes /resource/config/Table/encode(dj/dbname/tablename)
      link = ['dj/' + link.shift() + '/' + link.shift()];
      database = 'config';
      table = 'Table';
    } else {
      database = link.shift();
      table = link.shift();
    }

    // compute the cache pk
    const pk = encodeURIComponent(database) + '/' + encodeURIComponent(table) + '/' + link.map(id => encodeURIComponent(id)).join('/');

    if (this.labels[pk]) {
      return this.labels[pk];
    }
    return of(this.defaultLabel(link));
  }

  runtime: DJRuntimeService;
  setRuntime(runtime) { this.runtime = runtime; }

  getIdLabelNG(link: string[], resolve = false, ownType?: string): Observable<string> {

    // sometimes, the link part array is ['', 'resource', ...] instead of ['/resource', ...] throwing off the indices
    if (link[0] === '') {
      link.shift();
      link[0] = '/' + link[0];
    }

    let database = null;
    let table = null;
    if (link.shift() === '/table') {
      // /table/dbname/tablename becomes /resource/config/Table/encode(dj/dbname/tablename)
      link = ['dj/' + link.shift() + '/' + link.shift()];
      database = 'config';
      table = 'Table';
    } else {
      database = link.shift();
      table = link.shift();
    }

    // compute the cache pk
    const pk = (ownType ? ownType + '#' : '') +
      encodeURIComponent(database) + '/' + encodeURIComponent(table) + '/' + link.map(id => encodeURIComponent(id)).join('/');

    if (this.labels2[pk]) {
      return this.labels2[pk];
    }

    this.labels2[pk] = this.getObjectLabelNG(database, table, link, undefined, ownType, true);

    return this.labels2[pk];
  }

  /**
   * given the key array, returns the default label.
   * Does a special handling for single keys like config/Table/Orders where label would be Orders
   */
  defaultLabel(ids: string[]): string {
    if (ids.length === 1) {
      const key = ids[0];
      if (typeof (key) === 'string' && key.startsWith('dj/')) {
        try {
          return Util.parseColumnID(key)[3];
        } catch (ignore) {
          try {
            return Util.parseTableID(key)[2];
          } catch (ignore) {
            try {
              return Util.parseDatabaseID(key)[1];
            } catch (ignore) {
              return key;
            }
          }
        }
      } else {
        return key;
      }
    } else {
      return ids.join('/');
    }
  }

  /**
   * debug flag to turn on display of value and layout
   */
  debug(): boolean {
    return false;
  }

  /**
   * if (debug) console.log()
   */
  log(msg, msg2?, msg3?, msg4?, msg5?) {
    if (this.debug()) {
      console.log(msg);
      if (msg2) {
        console.log(msg2);
      }
      if (msg3) {
        console.log(msg3);
      }
      if (msg4) {
        console.log(msg4);
      }
      if (msg5) {
        console.log(msg5);
      }
    }
  }

  /**
   * guard all routes except login / logout. redirect to login page
   * if not authenticated
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {

    // Remember this route (as starting point for logging in/session expires)
    sessionStorage.djLastUrl = state.url;

    if (sessionStorage.token) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
