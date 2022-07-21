import { HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EditWidgetDialogComponent } from '../../edit-widget-dialog/edit-widget-dialog.component';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { LinksComponent } from '../links/links.component';
import { DashjoinWidget } from '../widget-registry';
import { DeleteConfirmDialogComponent } from '../../delete-confirm-dialog/delete-confirm-dialog.component';
import { Util } from '../../util';

/**
 * displays an editor for related records
 */
@DashjoinWidget({
  name: 'editRelated',
  category: 'Default',
  description: 'Component that displays an editor for related records',
  htmlTag: 'dj-edit-related',
  fields: ['title', 'text', 'prop', 'columns', 'deleteConfirmation']
})
@Component({
  selector: 'app-edit-related',
  templateUrl: './edit-related.component.html',
  styleUrls: ['../table/table.component.css']
})
export class EditRelatedComponent extends LinksComponent implements OnInit {

  /**
   * get metadata
   */
  async initWidget() {
    if (this.layout.prop) {
      const parts = Util.parseColumnID(this.layout.prop);
      if (this.layout.schema) {
        this.queryMeta = this.layout.schema;
      } else {
        this.app.getSchema(parts[1], parts[2]).subscribe(res => this.queryMeta = res);
      }
      if (this.layout.createSchema) {
        this.createSchema = this.layout.createSchema;
      } else {
        this.app.getSchema(parts[1], parts[2]).subscribe(res => this.createSchema = res);
      }

      // set DB + table so that this.getData() works
      this.database = encodeURIComponent(parts[1]);
      this.table = encodeURIComponent(parts[2]);
      // Note: getData() has special handling for config/Table, which we don't want here...
      // This we set data manually:
      this.data = 'dj/' + this.database + '/' + Util.encodeTableOrColumnName(this.table);

      // search key (where FK = this)
      const arg = {};
      arg[parts[3]] = (this.pk1);

      console.log('all', '/rest/database/all/' + encodeURIComponent(parts[1]) + '/' + encodeURIComponent(parts[2]), arg);

      this.getData().get({ arguments: arg }).then(res => {
        console.log('res', res.data)
        // this.http.post<object[]>('/rest/database/all/' + encodeURIComponent(parts[1]) + '/' + encodeURIComponent(parts[2]), arg, {
        //   headers: new HttpHeaders({
        //     'Content-Type': 'application/json',
        //     'x-dj-cache': encodeURIComponent(parts[1]) + '/' + encodeURIComponent(parts[2])
        //   })
        // }).subscribe(res => {
        this.all = res.data;
        if (this.layout.columns) {
          this.columns = this.layout.columns;
        } else {
          this.computeColumnsFromAll();
        }
        this.selectColumns = ['__dj_select', ...this.columns];
        this.createValue = arg;
      }, this.errorHandler);
    }
  }

  /**
   * computes the CRUD URL for the related entity (using the selected table row)
   */
  _relatedUrl(): string {
    let res = '/rest/database/crud/' + Util.parseTableID(this.queryMeta.ID)[1] + '/' + Util.parseTableID(this.queryMeta.ID)[2];
    for (const p of Object.values(this.queryMeta.properties)) {
      if (p.pkpos === 0) {
        res = res + '/' + encodeURIComponent(this.selected[p.name]);
      }
    }
    console.log('relatedUrl', res, this.queryMeta);
    return res;
  }

  relatedKey(): string {
    console.log('meta', this.queryMeta)
    for (const p of Object.values(this.queryMeta.properties)) {
      if (p.pkpos === 0) {
        return encodeURIComponent(this.selected[p.name]);
      }
    }
    throw new Error('Table has no primary key: ' + this.database + '/' + this.table);
  }

  /**
   * opens a popup allowing to edit a 1:n child
   */
  editRelated(): void {
    const dbTable = this.database + '/' + this.table;
    const key = this.relatedKey();

    const dialogRef = this.dialog.open(EditWidgetDialogComponent,
      { data: { value: this.selected, schema: this.queryMeta } });
    dialogRef.afterClosed().subscribe(data => {
      if (!data) { return; }
      this.getData().update(key, data).then(res => {
        // this.http.post(this.relatedUrl(), data, {
        //   headers: new HttpHeaders({ 'Content-Type': 'application/json' })
        // }).subscribe(res => {
        if (dbTable === 'config/Property/') {
          delete this.app.cache[decodeURIComponent(key)];
        }
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
        this.eventChange.emit({ type: 'update' });
      }, this.errorHandler);
    });
  }

  /**
   * create instance event handler
   */
  async create(onTable: string): Promise<void> {
    const parts = Util.parseTableOrColumnID(onTable);
    console.log('create', parts)
    this.getData().create(this.createValue).then(res => {
    // this.http.put('/rest/database/crud/' + encodeURIComponent(parts[1]) + '/' + encodeURIComponent(parts[2]), this.createValue, {
    //   headers: new HttpHeaders({ 'Content-Type': 'application/json' }), responseType: 'text'
    // }).subscribe(res => {
      this.snackBar.open('Done', 'Ok', { duration: 3000 });
      if (res.startsWith('dj/config/Property/')) {
        res = decodeURIComponent(res.substr('dj/config/Property/'.length));
        delete this.app.cache[res];
        this.eventChange.emit({ type: 'redraw' });
      } else {
        this.eventChange.emit({ type: 'create' });
      }
    }, this.errorHandler);
  }

  /**
   * delete instance event handler
   */
  delete(): void {

    const dbTable = this.database + '/' + this.table;

    const key = this.relatedKey();

    // if we are deleting in an editRelated widget, it is an update, delete otherwise (triggers browser back)
    const type = this.layout.widget === 'edit' ? 'delete' : 'update';

    if (this.layout.deleteConfirmation) {
      const confirm = (dbTable === 'config/Table' ||
        dbTable === 'config/Property') ? 'delete' : '';
      this.dialog.open(DeleteConfirmDialogComponent, {
        data: { display: this.layout.deleteConfirmation, confirm }
      }).afterClosed().subscribe(res => {
        if (res === confirm) {
          this.getData().delete(key).then(res2 => {
            //          this.http.delete(url).subscribe(res2 => {
            if (dbTable === 'config/Property/') {
              delete this.app.cache[decodeURIComponent(key)];
            }
            this.snackBar.open('Done', 'Ok', { duration: 3000 });
            this.eventChange.emit({ type });
          }, this.errorHandler);
        }
      });
    } else {
      this.getData().delete(key).then(res => {
//        this.http.delete(url).subscribe(res => {
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
        this.eventChange.emit({ type });
      }, this.errorHandler);
    }
  }
}
