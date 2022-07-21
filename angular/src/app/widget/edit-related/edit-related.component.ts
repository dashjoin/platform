import { Component, OnInit } from '@angular/core';
import { EditWidgetDialogComponent } from '../../edit-widget-dialog/edit-widget-dialog.component';
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
      // Thus we set data manually:
      this.data = 'dj/' + this.database + '/' + Util.encodeTableOrColumnName(this.table);

      // search key (where FK = this)
      const arg = {};
      arg[parts[3]] = (this.pk1);

      this.getData().get({ arguments: arg }).then(res => {
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
   * Returns the key of the related entity (using the selected table row)
   * @returns key
   */
  relatedKey(): string {
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
        this.snackBar.open('Done', 'Ok', { duration: 3000 });
        this.eventChange.emit({ type });
      }, this.errorHandler);
    }
  }
}
