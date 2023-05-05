import { HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Util } from '../../util';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';
import { ConfirmationDialogComponent } from '../../confirmation-dialog/confirmation-dialog.component';

/**
 * action button with an optional form to enter call parameters
 */
@DashjoinWidget({
  name: 'button',
  category: 'Default',
  description: 'Component that draws an action button',
  htmlTag: 'dj-button',
  fields: ['title', 'text', 'print', 'navigate', 'properties', 'clearCache', 'deleteConfirmation']
})
@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.css']
})
export class ButtonComponent extends DJBaseComponent implements OnInit {

  /**
   * generate create schema
   */
  async initWidget() {
    this.layoutProperties2createSchema();
  }

  /**
   * button widget action (navigate or print)
   */
  call() {
    if (this.layout.deleteConfirmation) {
      this.dialog.open(ConfirmationDialogComponent, {
        data: {
          message: this.layout.deleteConfirmation,
          buttonText: {
            ok: 'OK',
            cancel: 'Cancel'
          }
        }
      }).afterClosed().subscribe(confirmed => {
        if (confirmed) {
          this.call2();
        }
      });
    } else {
      this.call2();
    }
  }

  call2() {
    this.runExpression(this.layout.print ? this.layout.print : this.layout.navigate, this.layout.clearCache)
      .then(res => {
        if (this.layout.clearCache)
          this.eventChange.emit({ type: 'update' });
        if (this.layout.print) {
          this.snackBar.open(res as string, 'Ok', { duration: 3000 });
        } else {
          if ((res as string).startsWith('http://') || (res as string).startsWith('https://')) {
            window.location.href = (res as string);
          } else {
            this.router.navigate(Util.url2array(res));
          }
        }
      }, this.errorHandler);

    if (this.value?.djClassName === 'com.dashjoin.function.ETL' || this.value?.djClassName === 'org.dashjoin.mapping.ETL' || this.value?.djClassName === 'org.dashjoin.mapping.ETLStream') {
      this.snackBar.open('ETL started', 'Ok', { duration: 3000 });
    }
  }

  /**
   * creates / merges the createSchema for button and variable widgets
   */
  layoutProperties2createSchema() {
    if (this.layout.properties) {
      // init createSchema, syncing it with this.layout.properties
      const tmp: any = {};
      for (const [k, v] of Object.entries(this.layout.properties)) {
        if (v === 'date') {
          tmp[k] = { type: 'string', widget: 'date' };
        } else if (v === 'upload') {
          tmp[k] = { type: 'string', widget: 'upload' };
        } else if (v === 'upload64') {
          tmp[k] = { type: 'string', widget: 'upload64' };
        } else {
          tmp[k] = { type: v };
        }
      }
      if (!this.layout.createSchema) {
        this.createSchema = { type: 'object', ID: null, name: null, parent: null, properties: tmp };
      } else {
        this.createSchema = this.layout.createSchema;
        const added = Object.keys(tmp).filter(item => !Object.keys(this.createSchema.properties).includes(item));
        const deleted = Object.keys(this.createSchema.properties).filter(item => !Object.keys(tmp).includes(item));
        for (const add of added) {
          this.createSchema.properties[add] = tmp[add];
        }
        for (const del of deleted) {
          delete this.createSchema.properties[del];
        }
      }
    }
  }
}
