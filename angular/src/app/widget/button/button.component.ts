import { HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Util } from '../../util';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * action button with an optional form to enter call parameters
 */
@DashjoinWidget({
  name: 'button',
  category: 'Default',
  description: 'Component that draws a an action button',
  htmlTag: 'dj-button',
  fields: ['title', 'text', 'print', 'navigate', 'properties']
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
    this.runExpression(this.layout.print ? this.layout.print : this.layout.navigate)
      .then(res => {
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

    if (this.value.djClassName === 'com.dashjoin.function.ETL' || this.value.djClassName === 'org.dashjoin.mapping.ETL' || this.value.djClassName === 'org.dashjoin.mapping.ETLStream') {
      this.snackBar.open('ETL started. Reload the page to get the current status', 'Ok', { duration: 3000 });
      setTimeout(() => {
        window.location.reload();
      }, 3000);
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
