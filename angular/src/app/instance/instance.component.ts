import {
  Component, OnInit, Input, Output, EventEmitter, ViewChildren,
  QueryList, ChangeDetectorRef, ElementRef, ViewChild, ComponentFactoryResolver, Type
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { JsonSchemaFormService } from '@dashjoin/json-schema-form';
import { MatSnackBar } from '@angular/material/snack-bar';
import { QueryComponent } from '../query/query.component';
import { AppService } from '../app.service';
import { forkJoin, from, Observable, of } from 'rxjs';
import { Title } from '@angular/platform-browser';
import { Location } from '@angular/common';
import { DjEvent } from './dj-event';
import { Layout } from './layout';
import { MatDialog } from '@angular/material/dialog';
import { EditWidgetDialogComponent } from '../edit-widget-dialog/edit-widget-dialog.component';
import { DatabaseNameChoiceHandler, ForeignKeyChoiceHandler, LocalNameChoiceHandler } from './foreign-key-choice-handler';
import { GridsterConfig } from 'angular-gridster2';
import { Widget } from './widget';
import { ExpressionComponent } from '../expression/expression.component';
import { StreamExpressionComponent } from '../expression/stream.expression.component';
import { Expression } from '../expression/expression';
import { MappingComponent } from '../mapping/mapping.component';
import { DepInjectorService } from '../dep-injector.service';
import { CompDirective } from './comp.directive';
import { WidgetRegistry } from '../widget/widget-registry';
import { DJBaseComponent } from '../djbase/djbase.component';
import { filter } from 'rxjs/operators';
import { Util } from '../util';
import { Table } from '../model';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { WidgetListComponent } from '../edit-widget-dialog/widgetlist.component';
import { IconSelectComponent } from '../edit-widget-dialog/icon-select.component';
import { CodeEditorComponent } from '../edit-widget-dialog/codeeditor.component';
import { DJRuntimeService } from '../djruntime.service';

/**
 * main component driving the page layout
 */
@Component({
  selector: 'app-instance',
  templateUrl: './instance.component.html',
  styleUrls: ['./instance.component.css']
})
export class InstanceComponent implements OnInit {

  /**
   * used to display errors and ok messages
   */
  protected snackBar: MatSnackBar;

  /**
   * httpI http interface
   */
  public http: HttpClient;

  /**
   * main app service for caching
   */
  public app: AppService;

  /**
   * sets the page title
   */
  protected titleService: Title;

  /**
   * navigation service
   */
  protected location: Location;

  /**
   * formService json schema service for registering custom components
   */
  public formService: JsonSchemaFormService;

  /**
   * opens dialogs
   */
  protected dialog: MatDialog;

  /**
   * used for dynamic rendering
   */
  protected componentFactoryResolver: ComponentFactoryResolver;

  protected runtime: DJRuntimeService;

  /**
   * inject services
   * @param router  router service for navigation
   */
  constructor(protected elRef: ElementRef, protected cdRef: ChangeDetectorRef, protected route: ActivatedRoute, public router: Router) {
    this.initDeps();
  }

  /**
   * for dyn rendering
   */
  @ViewChild(CompDirective, { static: true }) compHost: CompDirective;

  /**
   * container children for event propagation
   */
  @ViewChildren('children') children: QueryList<InstanceComponent>;

  /**
   * page toolbar for event propagation
   */
  @ViewChild('djToolbar') djToolbar: InstanceComponent;

  /**
   * is this the page's root component?
   */
  root: boolean;

  /**
   * search term
   */
  search: string;

  /**
   * database we're currently navigating
   */
  database: string;

  /**
   * table we're currently navigating
   */
  table: string;

  /**
   * pk we're currently navigating
   */
  pk1: string;

  /**
   * pk we're currently navigating
   */
  pk2: string;

  /**
   * pk we're currently navigating
   */
  pk3: string;

  /**
   * pk we're currently navigating
   */
  pk4: string;

  /**
   * page query parameter
   */
  lastpage: string;

  /**
   * backend service URL for this instance
   */
  url: string;

  /**
   * orginial sublayout for editing
   */
  comp: any;

  /**
   * parent layout - needed for all move / add / delete ops
   */
  parentComp: any;

  /**
   * defines the type of layout this component is part of: is one of
   *
   * dashboard: a standalone dashboard page like page/Home
   * instance: an instance page like dj/northwind/EMPLOYEE/7 using custom layout defined in dj/config/Table/dj_northwind_EMPLOYEE[layout]
   * default: an instance page like dj/northwind/EMPLOYEE/7 using the default layout in dj/config/widget/default
   * table: a table page like config/Table/dj_northwind_EMPLOYEE
   *   using the custom layout defined in config/Table/dj_northwind_EMPLOYEE[tableLayout]
   * defaulttable: a table page like config/Table/dj_northwind_EMPLOYEE
   *   using the default layout defined in config/Table/dj_config_Table[layout]
   */
  @Input() layoutMode: string;

  /**
   * Name of the layout (e.g. Home or EMPLOYEE)
   */
  @Input() layoutName: string;

  /**
   * simplified JSON pointer like 0,1,0 which translates into the "real" JSON pointer /children[0]/children[1]/children[0]
   */
  @Input() layoutPos: string;

  /**
   * passed inside the layout component tree
   * mode and name are static, pos is adjusted accordingly
   */
  currentLayoutWidget: { layoutMode: string, layoutName: string, layoutPos: string };

  /**
   * this structure "remembers" the layout if the current root node was included
   * by a parent widget. Allows editing both the root node of the included widget
   * as well as the including widget
   */
  layoutWidget: { layoutMode: string, layoutName: string, layoutPos: string }[] = [];

  /**
   * layout passed from parent
   */
  @Input() layout: Widget;

  /**
   * current instance value passed from parent
   */
  @Input() value: any;

  /**
   * emit event to the parent
   */
  @Output() eventChange = new EventEmitter<DjEvent>();

  /**
   * query metadata
   */
  queryMeta: Table;

  /**
   * like columns but with a fist element '__dj_select'
   */
  selectColumns: string[];

  /**
   * selected table row
   */
  selected: any;

  /**
   * JSON schema in case we're editing
   */
  schema: Table = { type: 'string', ID: null, name: null, parent: null, properties: null };

  /**
   * user entry value when we're creating a new instance
   */
  createValue = {};

  /**
   * user entry schema when we're creating a new instance
   */
  createSchema: Table = { type: 'string', ID: null, name: null, parent: null, properties: null };

  /**
   * grid options
   */
  options: GridsterConfig;

  /**
   * widgets at the top of the page which are shown with 100% width below each other (row wrap layout)
   */
  rowKids: any[];

  /**
   * n layout columns below
   */
  colKids: any[][];

  /**
   * holds json schema form validity
   */
  error: string;

  /**
   * check widget equality except for fields changed by gridster
   */
  static eq(i: object, item: object) {
    for (const f of Object.keys(i)) {
      if (f !== 'x' && f !== 'y' && f !== 'rows' && f !== 'cols') {
        if (typeof i[f] === 'object' && typeof item[f] === 'object') {
          if (JSON.stringify(i[f]) !== JSON.stringify(item[f])) {
            return false;
          }
        } else {
          if (i[f] !== item[f]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * react to resize / move event in grid
   */
  static itemChange(item, itemComponent) {
    // we pass a this reference to this.options which shows up here...
    // simulate menu open to load this.comp for editing
    (this as any).ref.menuOpened().subscribe(() => {
      for (const i of (this as any).ref.comp.children) {
        if (InstanceComponent.eq(i, item)) {
          i.x = item.x;
          i.y = item.y;
          i.rows = item.rows;
          i.cols = item.cols;
          (this as any).ref.dirty();
        }
      }
    });
  }

  /**
   * perform dependency injection
   */
  initDeps() {
    if (this.snackBar) {
      return;
    }
    const di = DepInjectorService.getInjector();
    if (!di) {
      throw new Error('No DI!');
    }
    this.snackBar = di.get(MatSnackBar);
    this.http = di.get(HttpClient);
    this.app = di.get(AppService);
    this.titleService = di.get(Title);
    this.location = di.get(Location);
    this.formService = di.get(JsonSchemaFormService);
    this.dialog = di.get(MatDialog);
    this.componentFactoryResolver = di.get(ComponentFactoryResolver);
    this.runtime = di.get(DJRuntimeService);
  }

  /**
   * assemble [pk1, ... , pkn]
   */
  pkArr(): string[] {
    const res = [];
    if (this.pk1) {
      res.push(this.pk1);
    }
    if (this.pk2) {
      res.push(this.pk2);
    }
    if (this.pk3) {
      res.push(this.pk3);
    }
    if (this.pk4) {
      res.push(this.pk4);
    }
    return res;
  }

  /**
   * compute URL, get layout if we're the root and do layout
   */
  ngOnInit() {
    if (this.layout) {
      this.init();
      this.doLayout();
    } else {
      const handler = (_ => {
        this.root = true;
        this.init();
        if (this.table) {
          this.http.get(this.url).subscribe(res => {
            // value loaded
            if (this.database === 'config' && this.table === 'Table') {
              // we are on a table page
              const tmp = Util.parseTableID(this.pk1);
              this.app.getSchema(tmp[1], tmp[2]).subscribe(mycache => {
                // read myself since there might be unsaved layout edits in the app.cache
                if (mycache.tableLayout) {
                  this.setLayout(mycache.tableLayout, 'table', null);
                  this.value = res;
                  this.doLayout();
                  this.label().subscribe(l => this.titleService.setTitle(l));
                } else {
                  // use the default table layout
                  this.app.getSchema('config', 'Table').subscribe(table => {
                    this.setLayout(table.instanceLayout, 'defaulttable', null);
                    this.value = res;
                    this.doLayout();
                    this.label().subscribe(l => this.titleService.setTitle(l));
                  });
                }
                const contextAndValue = this.context();
                contextAndValue.value = this.value;
                sessionStorage.context = JSON.stringify(contextAndValue);
              });
            } else {

              const view = this.route.snapshot.queryParamMap.get('page');
              if (view) {
                this.app.getPage(view).subscribe(p => {
                  this.setLayout(p.layout, 'page', view);
                  this.value = res;
                  this.doLayout();
                  this.label().subscribe(l => this.titleService.setTitle(l));
                }, this.errorHandler);
              } else {

                // we are on an instance page
                this.app.getSchema(this.database, this.table).subscribe(table => {
                  // table loaded
                  if (table.instanceLayout) {
                    // if present use table.layout
                    this.setLayout(table.instanceLayout, 'instance', null);
                    this.value = res;
                    this.doLayout();
                    this.label().subscribe(l => this.titleService.setTitle(l));
                  } else {
                    // default otherwise
                    this.app.getPage('default').subscribe(def => {
                      this.setLayout(def.layout, 'default', null);
                      this.value = res;
                      this.doLayout();
                      this.label().subscribe(l => this.titleService.setTitle(l));
                    }, this.errorHandler);
                  }
                  const contextAndValue = this.context();
                  contextAndValue.value = this.value;
                  sessionStorage.context = JSON.stringify(contextAndValue);
                }, this.errorHandler);
              }
            }
          }, this.errorHandler);
        } else {
          this.app.getPage(this.database).subscribe(widget => {
            if (!widget.layout) {
              widget.layout = {
                widget: 'page', pageLayout: 'horizontal',
                children: [{ widget: 'text', title: 'New page' }]
              };
            }
            this.setLayout(widget.layout, 'page', this.database);
            this.value = null;
            this.doLayout();
            this.label().subscribe(l => this.titleService.setTitle(decodeURIComponent(l)));
            sessionStorage.context = JSON.stringify(this.context());
          }, this.errorHandler);
        }
      });
      this.route.paramMap.subscribe(handler);
      this.route.queryParamMap.pipe(filter(p => p.get('page') !== this.lastpage)).subscribe(handler);
    }
  }

  /**
   * init page context from route
   */
  init() {
    if (!((this.formService.registry as any).query)) {
      this.formService.registerComponent('query', QueryComponent);
    }
    this.formService.registerComponent('expression', ExpressionComponent);
    this.formService.registerComponent('stream-expressions', StreamExpressionComponent);
    if (!((this.formService.registry as any).mapping)) {
      this.formService.registerComponent('mapping', MappingComponent);
    }
    this.formService.registerComponent('imagelist', WidgetListComponent);
    this.formService.registerComponent('icon', IconSelectComponent);
    this.formService.registerComponent('codeeditor', CodeEditorComponent);

    this.formService.registerDisplayWith('fk', new ForeignKeyChoiceHandler(this.http, this.app));
    this.formService.registerDisplayWith('fkdb', new DatabaseNameChoiceHandler(this.http, this.app));
    this.formService.registerDisplayWith('fkln', new LocalNameChoiceHandler(this.http, this.app));

    this.database = this.route.snapshot.paramMap.get('database');
    this.search = this.route.snapshot.paramMap.get('search');
    if (this.search) { this.database = 'search'; }
    this.table = this.route.snapshot.paramMap.get('table');
    this.pk1 = this.route.snapshot.paramMap.get('pk1');
    this.pk2 = this.route.snapshot.paramMap.get('pk2');
    this.pk3 = this.route.snapshot.paramMap.get('pk3');
    this.pk4 = this.route.snapshot.paramMap.get('pk4');
    this.lastpage = this.route.snapshot.queryParamMap.get('page');

    if (this.route.snapshot.paramMap.get('tdatabase') && this.route.snapshot.paramMap.get('ttable')) {
      this.database = 'config';
      this.table = 'Table';
      this.pk1 = 'dj/' +
        Util.encodeTableOrColumnName(this.route.snapshot.paramMap.get('tdatabase')) + '/' +
        Util.encodeTableOrColumnName(this.route.snapshot.paramMap.get('ttable'));
    }
    this.url = '/rest/database/crud/' + encodeURIComponent(this.database) + '/' +
      encodeURIComponent(this.table) + '/' + encodeURIComponent(this.pk1);
    if (this.pk2) {
      this.url = this.url + '/' + encodeURIComponent(this.pk2);
    }
    if (this.pk3) {
      this.url = this.url + '/' + encodeURIComponent(this.pk3);
    }
    if (this.pk4) {
      this.url = this.url + '/' + encodeURIComponent(this.pk4);
    }
    this.app.log('init url', this.url);

    // allow setting currently unset variable keys via query parameters 
    if (this.root) {
      let variable = JSON.parse(sessionStorage.getItem('variable'));
      let dirty = false;
      for (let key of this.route.snapshot.queryParamMap.keys) {
        if (!variable) {
          variable = {};
        }
        variable[key] = this.route.snapshot.queryParamMap.get(key);
        dirty = true;
      }
      if (dirty) {
        sessionStorage.setItem('variable', JSON.stringify(variable));
      }
    }
  }

  /**
   * deep copy layout to make sure the UI is reloaded
   * (value changes must layout often stays the same)
   */
  setLayout(l: any, lmode: string, lname: string) {

    if (this.layoutMode && !this.root) {
      this.layoutWidget.push({ layoutMode: this.layoutMode, layoutName: this.layoutName, layoutPos: this.layoutPos });
    }

    this.layoutMode = lmode;
    this.layoutName = lname;
    this.layoutPos = '';

    // deep copy
    this.layout = JSON.parse(JSON.stringify(l));

    // replace ${VAR} with value.VAR
    for (let [k, v] of Object.entries(this.layout)) {
      if (typeof (v) === 'string' && v.indexOf('${') >= 0 && this.value) {
        for (const [vk, vv] of Object.entries(this.value)) {
          if (v === '${' + vk + '}') {
            // exact match, preserve the type of vv
            v = vv;
            break;
          } else {
            while ((v as string).indexOf('${' + vk + '}') >= 0) {
              // the template contains other parts, interpret as string
              v = (v as string).replace('${' + vk + '}', '' + vv);
            }
          }
        }
        this.layout[k] = v;
      }
    }
  }

  /**
   * do the layout (perform additional REST request and computations)
   */
  doLayout() {
    if (this.layout.widget === 'page') {
      this.options = {
        draggable: {
          enabled: this.app.editLayout,
        },
        resizable: {
          enabled: this.app.editLayout,
        },
        margin: 4,
        itemChangeCallback: InstanceComponent.itemChange,
        ref: this
      };
      if (this.layout.widget === 'page') {
        const [r, c] = Layout.compute(this.layout.pageLayout, this.layout.children);
        this.rowKids = r;
        this.colKids = c;
      }
    } else {
      let nc: Type<DJBaseComponent> = null;
      for (const type of WidgetRegistry.getInstance().getWidgets()) {
        if (WidgetRegistry.getInstance().getWidgetInfo(type).name === this.layout.widget) {
          nc = type;
        }
      }

      if (nc) {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(nc);
        const viewContainerRef = this.compHost.viewContainerRef;
        viewContainerRef.clear();
        const componentRef = viewContainerRef.createComponent(componentFactory);
        componentRef.instance.value = this.value;
        componentRef.instance.layout = this.layout;
        componentRef.instance.layoutName = this.layoutName;
        componentRef.instance.layoutMode = this.layoutMode;
        componentRef.instance.layoutPos = this.layoutPos;
        componentRef.instance.eventChange.subscribe(event => this.eventChange.emit(event));
        return;
      }

      // a custom widget may have additional fields that get passed on to the template
      const retainFields: any = {};
      for (const [k, v] of Object.entries(this.layout)) {
        if (k !== 'widget') {
          retainFields[k] = v;
        }
      }
      this.app.getWidget(this.id(this.layout.widget)).subscribe(widget => {

        if (!widget.layout) {
          widget.layout = { widget: 'text', text: 'New Widget' };
        }

        // copy layout
        const copy = JSON.parse(JSON.stringify(widget.layout));

        // copy retain fields from including widget
        for (const [k, v] of Object.entries(retainFields)) {
          if (k !== 'widget') {
            if (v) {
              // if the parent's value is null, ignore it an use the widget value
              copy[k] = v;
            }
          }
        }
        this.setLayout(copy, 'widget', this.layout.widget);
        this.doLayout();
      }, this.errorHandler);
    }
  }

  /**
   * cannot call JSON.stringify from template
   */
  stringify(o: any): string {
    return JSON.stringify(o, null, 2);
  }

  /**
   * common REST error handler. decreases call counter and logs error in the snack bar
   */
  errorHandler = (error: any) => {
    if (error.status === 404 && !this.layout) {
      this.snackBar.open(Util.errorMsg(error), 'Open Home', { duration: 3000 }).onAction().subscribe(() => this.router.navigate(['/']));
    } else {
      this.snackBar.open(Util.errorMsg(error), 'OK');
    }
  }

  /**
   * get the label for the current object
   */
  label(): Observable<string> {
    return this.app.getObjectLabel(this.database, this.table, this.pkArr(), this.value);
  }

  /**
   * get the label for a referenced object
   */
  labelId(id: string[]): Observable<string> {
    this.app.setRuntime(this.runtime);
    return this.app.getIdLabelNG(id);
    //      return this.app.getIdLabel(id);
  }

  linkedLabelId(id: string[]): Observable<string> {
    this.app.setRuntime(this.runtime);
    return this.app.getIdLabelNG(id, true, this.schema.ID);
    //      return this.app.getIdLabel(id);
  }

  /**
   * listen to child events and forward to root
   */
  onEvent(event: DjEvent) {
    if (this.root) {
      this.event(event);
    } else {
      this.eventChange.emit(event);
    }
  }

  /**
   * process event and possibly broadcast it to children
   */
  event(event: DjEvent): void {
    if (event.type === 'redraw') {
      this.layout = null;
      this.ngOnInit();
      return;
    }

    // if update, re-read the value and do layout
    if (event.type === 'update' || event.type === 'create') {
      this.layout = null;
      this.ngOnInit();
      return;
    }

    // if my current resource was deleted, go back
    if (event.type === 'delete') {
      this.location.back();
      return;
    }

    // grid needs to react to layout events
    if (this.layout.widget === 'grid' || (this.layout.widget === 'page' && this.layout.pageLayout === 'grid')) {
      if (event.type === 'layout') {
        this.options.draggable.enabled = event.busy;
        this.options.resizable.enabled = event.busy;
        this.options.api.optionsChanged();
      }
    }

    if (event.type === 'layout') {
      if (this.root && (this.layoutMode === 'default' || this.layoutMode === 'defaulttable')) {
        if (event.busy) {
          this.customize();
        }
      }
    }

    if (this.children) {
      for (const c of this.children) {
        c.event(event);
      }
    }

    if (this.djToolbar) {
      this.djToolbar.event(event);
    }
  }

  /**
   * can move the comp up? am I first already
   */
  canUp(): boolean {
    if (this.layoutMode === 'defaulttable' || this.layoutMode === 'default') { return false; }
    return this.parentComp && this.parentComp.children && this.parentComp.children.indexOf(this.comp) > 0;
  }

  /**
   * can move the comp down? am I last already
   */
  canDown(): boolean {
    if (this.layoutMode === 'defaulttable' || this.layoutMode === 'default') { return false; }
    return this.parentComp && this.parentComp.children && this.parentComp.children.indexOf(this.comp) < this.parentComp.children.length - 1;
  }

  /**
   * can delete comp if not root
   */
  canDelete(): boolean {
    if (this.layoutMode === 'defaulttable' || this.layoutMode === 'default') { return false; }
    return this.parentComp;
  }

  /**
   * can add to containers
   */
  canAdd(): boolean {
    if (this.layoutMode === 'defaulttable' || this.layoutMode === 'default') { return false; }
    return this.comp?.children || this.parentComp?.children;
  }

  /**
   * can we paste here
   */
  canPaste(): boolean {
    return this.canAdd() && localStorage.getItem('djClipboard') !== null;
  }

  /**
   * move up in parent container's child list
   */
  up(alltheway: boolean) {
    const idx = this.parentComp.children.indexOf(this.comp);
    if (alltheway) {
      this.parentComp.children.splice(idx, 1);
      this.parentComp.children.unshift(this.comp);
    } else {
      this.parentComp.children[idx] = this.parentComp.children[idx - 1];
      this.parentComp.children[idx - 1] = this.comp;
    }
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * move down in parent container's child list
   */
  down(alltheway: boolean) {
    const idx = this.parentComp.children.indexOf(this.comp);
    if (alltheway) {
      this.parentComp.children.splice(idx, 1);
      this.parentComp.children.push(this.comp);
    } else {
      this.parentComp.children[idx] = this.parentComp.children[idx + 1];
      this.parentComp.children[idx + 1] = this.comp;
    }
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * paste clipboard
   */
  paste() {
    if (this.comp.children) {
      this.comp.children.unshift(JSON.parse(localStorage.getItem('djClipboard')));
    } else {
      const idx = this.parentComp.children.indexOf(this.comp);
      this.parentComp.children.splice(idx + 1, 0, JSON.parse(localStorage.getItem('djClipboard')));
    }
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * copy clipboard
   */
  copy() {
    localStorage.setItem('djClipboard', JSON.stringify(this.comp));
  }

  /**
   * cut to clipboard
   */
  cut() {
    localStorage.setItem('djClipboard', JSON.stringify(this.comp));
    this.parentComp.children.splice(this.parentComp.children.indexOf(this.comp), 1);
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * just remove
   */
  remove() {
    this.parentComp.children.splice(this.parentComp.children.indexOf(this.comp), 1);
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * add a widget
   */
  add() {
    if (this.comp.children) {
      this.comp.children.unshift({ widget: 'text', text: 'New Widget' });
    } else {
      const idx = this.parentComp.children.indexOf(this.comp);
      this.parentComp.children.splice(idx + 1, 0, { widget: 'text', text: 'New Widget' });
    }
    this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    this.dirty();
  }

  /**
   * edit this widget
   */
  edit() {
    this.app.log(this.comp);
    const editedWidget = this.layoutMode === 'widget' ? (this.layoutName === '${pk1}' ? this.pk1 : this.layoutName) : null;
    const dialogRef = this.dialog.open(EditWidgetDialogComponent, { minWidth: '50%', data: { value: this.comp, editedWidget } });
    dialogRef.afterClosed().subscribe(data => {
      if (data) {
        if (!WidgetRegistry.getInstance().getWidgetInfoFromName(data.widget)?.isContainer && data.widget !== 'page') {
          // remove children unless it is one of these
          data.children = undefined;
        } else {
          // init children if it is one of these and no children present yet
          if (!data.children) {
            data.children = [];
          }
        }
        // delete all fields from comp
        for (const f of Object.keys(this.comp)) {
          delete this.comp[f];
        }
        // copy all fields from data to comp
        for (const [f, v] of Object.entries(data)) {
          this.comp[f] = v;
        }
        this.dirty();
        this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
      }
    });
  }

  /**
   * edit layout handler
   * @param layout  chosen layout
   */
  editLayout(layout: string) {
    this.menuOpened().subscribe(() => {
      this.comp.pageLayout = layout;
      this.dirty();
      this.root ? this.onEvent({ type: 'redraw' }) : this.eventChange.emit({ type: 'redraw' });
    });
  }

  /**
   * create custom layout
   */
  customize() {
    if (this.layoutMode === 'default') {
      this.app.getSchema(this.database, this.table).subscribe(table => {
        table.instanceLayout = JSON.parse(JSON.stringify(this.layout));
        table.instanceLayout.children.push({ widget: 'text', text: 'custom instance layout' });
        this.layout = null;
        this.ngOnInit();
        this.app.dirtyLayouts.schema[this.database + '/' + this.table] = { database: this.database, table: this.table };
        this.app.log(this.app.dirtyLayouts);
      });
    } else {
      this.app.getSchema(Util.parseTableID(this.pk1)[1], Util.parseTableID(this.pk1)[2]).subscribe(table => {
        table.tableLayout = JSON.parse(JSON.stringify(this.layout));
        table.tableLayout.children.push({ widget: 'text', text: 'custom table layout' });
        this.layout = null;
        this.ngOnInit();
        const database = Util.parseTableID(this.pk1)[1];
        const tbl = Util.parseTableID(this.pk1)[2];
        this.app.dirtyLayouts.schema[database + '/' + tbl] = { database, table: tbl };
        this.app.log(this.app.dirtyLayouts);
      });
    }
  }

  /**
   * Delete custom layout
   */
  restore() {
    if (this.layoutMode === 'instance') {
      this.app.getSchema(this.database, this.table).subscribe(table => {
        table.instanceLayout = null;
        this.layout = null;
        this.ngOnInit();
        this.app.dirtyLayouts.schema[this.database + '/' + this.table] = { database: this.database, table: this.table };
        this.app.log(this.app.dirtyLayouts);
      });
    } else if (this.layoutMode === 'table') {
      this.app.getSchema(Util.parseTableID(this.pk1)[1], Util.parseTableID(this.pk1)[2]).subscribe(table => {
        table.tableLayout = null;
        this.layout = null;
        this.ngOnInit();
        const database = Util.parseTableID(this.pk1)[1];
        const tbl = Util.parseTableID(this.pk1)[2];
        this.app.dirtyLayouts.schema[database + '/' + tbl] = { database, table: tbl };
        this.app.log(this.app.dirtyLayouts);
      });
    } else {
      this.app.dirtyLayouts.page[this.database] = this.database;
      this.app.getPage(this.database).subscribe(page => {
        page.layout = null;
      });
      this.location.back();
    }
  }

  /**
   * menu on the current layout was opened
   */
  menuOpened() {
    return this.menuOpenedT({ layoutMode: this.layoutMode, layoutName: this.layoutName, layoutPos: this.layoutPos });
  }

  /**
   * menu with a specific context was opened (e.g. for editing include parents rather than include itself)
   */
  menuOpenedT(t: { layoutMode: string, layoutName: string, layoutPos: string }) {
    this.currentLayoutWidget = t;
    const l: { o: Observable<any>, path: string } =
      this.app.getLayout(t.layoutMode, this.id(t.layoutName), this.database, this.table, this.pk1, t.layoutPos);
    l.o.subscribe(res => {
      this.parentComp = null;
      this.comp = res[l.path];
      this.app.log('menuOpened', l.path, t.layoutPos);
      for (const n of t.layoutPos.split(',')) {
        if (n) {
          this.parentComp = this.comp;
          this.comp = this.comp.children[n];
        }
      }
    });
    return l.o;
  }

  /**
   * menu title for the current layout
   */
  menuTitle(): string {
    return this.menuTitleT(this.currentLayoutWidget);
  }

  /**
   * menu title for current or include parent
   */
  menuTitleT(t: { layoutMode: string, layoutName: string, layoutPos: string }): string {
    if (!t) {
      return '';
    }
    switch (t.layoutMode) {
      case 'page': {
        return 'Page "' + t.layoutName + '"';
      }
      case 'widget': {
        return 'Widget "' + this.id(t.layoutName) + '"';
      }
      case 'instance': {
        return '"' + this.table + '" record';
      }
      case 'default': {
        return 'Default record';
      }
      case 'table': {
        return '"' + Util.parseTableID(this.pk1)[2] + '" table';
      }
      case 'defaulttable': {
        return 'Default table';
      }
      default: {
        throw new Error('illegal layout option: ' + t.layoutMode);
      }
    }
  }

  /**
   * used in the template since typeof is not available
   */
  typeof(val: any, type: string): boolean { return typeof val === type; }

  /**
   * template string replacement based on context+value
   */
  id(name: string): string {
    const ctx = this.context();
    ctx.value = this.value;
    return Expression.template(name, ctx, null);
  }

  /**
   * compute display text by delegating to local expression eval
   * @param def default value
   */
  text(def?: string): string {
    const ctx = this.context();
    ctx.value = this.value;
    return Expression.template(this.layout.text, ctx, def);
  }

  /**
   * save edited layouts
   */
  saveConfirm() {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: 'Save the changed layout?',
        submessage: 'This will overwrite the layout for ' + this.app.getDirtyChanges(),
        buttonText: {
          ok: 'Save',
          cancel: 'No'
        }
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.save();
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
      }
    });
  }

  reloadConfirm() {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        message: 'Leave edit mode?',
        submessage: 'This will discard all your changes for ' + this.app.getDirtyChanges(),
        buttonText: {
          ok: 'Leave and Discard',
          cancel: 'Continue Editing'
        }
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        window.location.reload();
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
      }
    });
  }

  save() {
    const jobs = [];
    this.app.editLayout = false;
    this.formService.editMode = false;
    for (const p of Object.keys(this.app.dirtyLayouts.page)) {
      jobs.push(this.app.setPage(p));
    }
    for (const p of Object.keys(this.app.dirtyLayouts.widget)) {
      jobs.push(this.app.setWidget(p));
    }
    for (const p of Object.values(this.app.dirtyLayouts.schema)) {
      jobs.push(this.app.setSchema((p as any).database, (p as any).table));
    }
    forkJoin(jobs).subscribe(res => {
      this.snackBar.open('Done', 'Ok', { duration: 3000 });
    }, this.errorHandler);
    this.app.dirtyLayouts = { page: {}, widget: {}, schema: {} };
    this.eventChange.emit({ type: 'layout', busy: false });
  }

  /**
   * a page layout edit was made, this method marks the layout as dirty (i.e. needs to be saved)
   */
  dirty() {
    this.app.dirty(this.currentLayoutWidget.layoutMode, this.id(this.currentLayoutWidget.layoutName),
      this.database, this.table, this.pk1, this.currentLayoutWidget.layoutPos);
  }

  /**
   * checks whether the session role is contained in the roles defined in the layout
   */
  isInRole(w: Widget): boolean {
    if (w.roles) {
      const sr = sessionStorage.roles ? JSON.parse(sessionStorage.roles) : undefined;
      for (const role of w.roles) {
        if (sr?.includes(role)) {
          return true;
        }
        if (this.app.editLayout && sr?.includes('admin')) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  /**
   * event handler that indicates that either the createSchema or the edit schema
   * were changed in the layout editor
   */
  schemaChange(create: boolean): void {

    if (this.layoutMode === 'defaulttable' || this.layoutMode === 'default') {
      this.snackBar.open('You cannot change the default layout. Choose customize from the page context menu.', 'Ok');
      return;
    }

    // copy values to comp so they get saved
    this.menuOpened().subscribe(() => {
      if (create) {
        this.createSchema = Util.cleanJsonSchema(JSON.parse(JSON.stringify(this.createSchema)));
        this.comp.createSchema = this.createSchema;
      } else {
        this.schema = Util.cleanJsonSchema(JSON.parse(JSON.stringify(this.schema)));
        this.comp.schema = this.schema;
      }

      this.dirty();
    });
  }

  /**
   * assembles the data context
   */
  context(): any {
    return {
      database: this.database,
      search: this.search,
      table: this.table,
      pk1: this.pk1,
      pk2: this.pk2,
      pk3: this.pk3,
      pk4: this.pk4,
      user: sessionStorage.user,
      email: sessionStorage.email,
      roles: sessionStorage.roles ? JSON.parse(sessionStorage.roles) : undefined,
      form: this.createValue,
      href: window.location.href,
      variable: JSON.parse(sessionStorage.getItem('variable'))
      // date: new Date(),
      // value: this.value,
    };
  }

  /**
   * Default implementation of redraw.
   * Might be overridden by components
   */
  redraw() {
    //console.log('redraw ' + window['djNoCache'], this);
    this.ngOnInit();
  }

  /**
   * Hash code (for getComponentId)
   * @param str 
   * @returns 
   */
  stringHashCode(str: string) {
    let hash = 0
    for (let i = 0; i < str.length; ++i)
      hash = Math.imul(hash, 31) + str.charCodeAt(i)
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
}
