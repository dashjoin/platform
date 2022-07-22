import { HttpClient, HttpClientXsrfModule, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, Input, OnInit } from '@angular/core';
import { InstanceComponent } from '../instance/instance.component';
import { DJRuntimeService } from '../djruntime.service';
import { DJData, DJDataGetOptions, DJDataMeta } from './data';
import { DepInjectorService } from '../dep-injector.service';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Router } from '@angular/router';
import { DeleteConfirmDialogComponent } from '../delete-confirm-dialog/delete-confirm-dialog.component';
import { Util } from '../util';

/**
 * widget base class
 */
@Component({
  selector: 'app-djbase',
  templateUrl: './djbase.component.html',
  styleUrls: ['./djbase.component.css']
})
export class DJBaseComponent extends InstanceComponent implements OnInit {

  /**
   * constructor
   * @param router  router for page navigation
   */
  constructor(protected elRef: ElementRef, protected cdRef: ChangeDetectorRef, protected route: ActivatedRoute, public router: Router) {
    super(elRef, cdRef, route, router);
    this.runtime = DepInjectorService.getInjector().get(DJRuntimeService);
    // this.app = DepInjectorService.getInjector().get(AppService);
    // this._http = DepInjectorService.getInjector().get(HttpClient);

    this.getConfigFromAttribute();
  }

  stringHashCode(str: string) {
    let hash = 0
    for (let i = 0; i < str.length; ++i)
      hash = hash << 5 - hash + str.charCodeAt(i)
    return (hash | 0) + 2147483647 + 1;
  }

  /**
   * Returns a "constant" ID for this component.
   * Should survive page reloads.
   * 
   * @returns Component ID
   */
  getComponentId() {
    if (!this.layout) return 'unknown';

    // Calculate the component ID as hash from its JSON definition
    const state = JSON.stringify(this.layout) + ' ' + this.layoutPos;

    return this.layout.widget + '-' + this.stringHashCode(state);
  }

  /**
   * TODO
   */
  @Input() data: string; // I.e. 'dj/northwind/CUSTOMERS' or 'dj/query/northwind/list'

  /**
   * TODO
   */
  @Input() jsoncfg: string;

  // layout: Widget;

  /**
   * metadata object
   */
  meta: DJDataMeta;
  // queryMeta: Table;

  /**
   * table base data columns
   */
  columns: string[];

  /**
   * table base data
   */
  all: any[];

  /**
   * estimated query result size
   */
  allLength = 100000;

  /**
   * last pagination state
   */
  allPage: PageEvent;

  /**
   * last sort state
   */
  allSort: Sort;

  /**
   * Redraw counter
   */
  redrawCount = 0;

  /**
   * TODO
   */
  protected runtime: DJRuntimeService;

  /**
   * TODO
   */
  dataSnapshot: any;

  /**
   * TODO
   */
  getConfigFromAttribute() {
    const node = this.elRef.nativeElement;
    this.jsoncfg = node.jsoncfg;
    if (this.jsoncfg) {
      const ctx = unescape(this.jsoncfg);
      this.layout = JSON.parse(ctx);
      this.app.log('layout decoded', this.layout);
    }
  }

  // _http: HttpClient;

  // http() { return this._http; }

  /**
   * TODO
   */
  printContext(msg?) {
    this.app.log(msg || '', 'layout', this.layout, 'value', this.value);
  }

  /**
   * called during the ngOnInit lifecycle
   */
  async initWidget() {
    // default is noop
  }

  /**
   * init and get data
   */
  ngOnInit(): void {
    // do not call ngOnInit here since this will call doLayout() and cause an NPE on the missing html anchor
    // super.ngOnInit();
    this.init();
    this.dataSnapshot = this.getData();
    this.initWidget().then();
  }

  /**
   * Data setting:
   * 1) data input property (fed from HTML attribute)
   * 2) data query from layout.query
   * 3) data table from layout.table
   */
  getData(): DJData<any> {

    let data = this.data;

    if (!data && this.layout?.query) {
      if (this.layout.graph)
        data = 'dj/queryGraph/' + this.layout.database + '/' + this.layout.query;
      else
        data = 'dj/query/' + this.layout.database + '/' + this.layout.query;
    }

    if (!data && this.layout?.database) {
      data = 'dj/' + this.layout.database + '/' + (this.layout as any).table;
    }

    if (!data && this.database === 'config' && this.table === 'Table') {
      data = this.pk1;
    }

    if (!data && this.database && this.table) {
      data = 'dj/' + this.database + '/' + Util.encodeTableOrColumnName(this.table);
    }

    if (!data && this.search) {
      data = 'dj/search/' + this.search;
    }

    this.app.log('data', data);
    return data ? this.runtime.getData(data) : this.runtime.getCurrentData();
  }

  /**
   * evaluate jsonata expression
   */
  jsonata(expr) {
    if (!expr) {
      return expr;
    }
    try {
      const res = this.runtime.jsonata(expr, this.dataSnapshot || this.getData());
      // console.log('jsonata', expr, res, this.dataSnapshot || this.getData());
      return res;
    } catch (error) {
      return expr;
    }
  }

  /**
   * Returns the primary key of this object.
   *
   * @returns Primary key, if composite concatenated with '/'
   */
  getKey(): string {
    const pks = this.pkArr();
    if (pks.length === 0) {
      return undefined;
    }
    return pks.map(s => encodeURIComponent(s)).join('/');
  }

  /**
   * enact sort / page change
   */
  async allChange() {
    if (!this.dataSnapshot) {
      this.app.log('dataSnapshot not set!'); return;
    }
    const offset = this.allPage.pageIndex * this.allPage.pageSize;
    const limit = this.allPage.pageSize;

    const opts: DJDataGetOptions = { pageSize: limit, cursor: { cursor: offset } };

    if (this.allSort && this.allSort.direction !== '') {
      opts.sort = [{ field: this.allSort.active, order: this.allSort.direction === 'asc' ? 'asc' : 'desc' }];
    }

    // Handle optional arguments for the data source (i.e. query parameters)
    if (this.layout.arguments) {
      // Evaluate the expression
      opts.arguments = await this.evaluateExpression(this.layout.arguments);
    }

    const page = await this.dataSnapshot.get(opts);
    this.app.log(page);
    this.all = page.data;
    if (page.data.length < limit) {
      this.allLength = offset + page.data.length;
    }
    // this.computeColumnsFromAll();
  }

  /**
   * Evaluates the given JSONata expression with the current context.
   * Optionally you can provide the cache category for this expression.
   * By default it uses the current DB/table context.
   */
  async evaluateExpression(expr: string, category?: string) {
    const ctx = this.context();
    let cat = category;
    if (!cat) {
      if (ctx.database && ctx.table)
        cat = encodeURIComponent(ctx.database) + '/' + encodeURIComponent(ctx.table);
      else
        cat = 'expression';
    }
    const res = await this.http.post<any>('/rest/expression', { expression: expr, data: ctx }, {
      headers: new HttpHeaders({ 'x-dj-cache': cat, 'Content-Type': 'application/json' })
    }).toPromise();
    return res;
  }

  /**
   * non cache version to be used from button.onCall() etc.
   */
  async runExpression(expr: string) {
    const ctx = this.context();
    const res = await this.http.post<any>('/rest/expression', { expression: expr, data: ctx }, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    }).toPromise();
    return res;
  }

  /**
   * table sort event
   */
  sortData(event: Sort) {
    this.allSort = event;
    this.allChange();
  }

  /**
   * pagination event
   */
  async page(event: PageEvent) {
    this.allPage = event;
    await this.allChange();
    this.computeColumnsFromAll();
  }

  /**
   * look at the data received and detect the columns contained in there
   */
  computeColumnsFromAll() {
    if (!this.all) { return; }
    this.columns = [];
    for (const row of this.all) {
      for (const key of Object.keys(row)) {
        if (this.columns.indexOf(key) < 0) {
          this.columns.push(key);
        }
      }
    }
    if (this.columns.length === 0) {
      this.columns.push('No data to display');
    }
  }

  /**
   * delete instance event handler
   * TODO: move to edit.component
   */
  delete(url): void {

    // if we are deleting in an editRelated widget, it is an update, delete otherwise (triggers browser back)
    const type = this.layout.widget === 'edit' ? 'delete' : 'update';

    if (this.layout.deleteConfirmation) {
      const confirm = (url.startsWith('/rest/database/crud/config/Table')) ||
        (url.startsWith('/rest/database/crud/config/Property')) ? 'delete' : '';
      this.dialog.open(DeleteConfirmDialogComponent, {
        data: { display: this.layout.deleteConfirmation, confirm }
      }).afterClosed().subscribe(res => {
        if (res === confirm) {
          this.getData().delete(this.getKey()).then(res2 => {
            //          this.http().delete(url).subscribe(res2 => {
            if (url.startsWith('/rest/database/crud/config/Property/')) {
              delete this.app.cache[decodeURIComponent(url.substr('/rest/database/crud/config/Property/'.length))];
            }
            this.snackBar.open('Done', 'Ok', { duration: 3000 });
            this.eventChange.emit({ type });
          }, this.errorHandler);
        }
      });
    } else {
      this.getData().delete(this.getKey()).then(res => {
        // this.http().delete(url).subscribe(res => {
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
        this.eventChange.emit({ type });
      }, this.errorHandler);
    }
  }

  localName(s: string): string {
    return Util.localName(s);
  }
}
