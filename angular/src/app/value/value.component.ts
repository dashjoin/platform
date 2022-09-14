import { KeyValue } from '@angular/common';
import { Component, OnInit, Input } from '@angular/core';
import { DisplayComponent } from '../widget/display/display.component';

@Component({
  selector: 'app-value',
  templateUrl: './value.component.html',
  styleUrls: ['./value.component.css']
})
export class ValueComponent {

  @Input() displayData: any;

  @Input() parent: DisplayComponent;

  /**
   * table columns
   */
  columns: string[] = [];

  /**
   * determine how the data should be layed out
   */
  getLayout(): 'single' | 'list' | 'keyvalue' | 'table' {
    if (this.objectType(this.displayData) === 'img') {
      return 'single';
    }
    if (this.objectType(this.displayData) === 'link') {
      return 'single';
    }
    if (this.objectType(this.displayData) === 'href') {
      return 'single';
    }
    if (this.objectType(this.displayData) === 'object') {
      return 'keyvalue';
    }
    if (this.objectType(this.displayData) === 'array') {
      for (const i of this.displayData) {
        if (this.objectType(i) === 'object') {
          // special case: array[object] is table
          for (const row of this.displayData) {
            for (const field of Object.keys(row)) {
              if (!this.columns.includes(field)) {
                this.columns.push(field);
              }
            }
          }
          return 'table';
        }
      }
      return 'list';
    }
    if (this.objectType(this.displayData) === 'simple') {
      return 'single';
    }
  }

  /**
   * return the type of data
   */
  objectType(data: any): 'img' | 'link' | 'href' | 'object' | 'array' | 'simple' {
    return ValueComponent.objectTypeS(data);
  }

  /**
   * return the type of data
   */
  static objectTypeS(data: any): 'img' | 'link' | 'href' | 'object' | 'array' | 'simple' {
    if (!data) {
      return 'simple';
    }
    if (Array.isArray(data)) {
      return 'array';
    } else if (typeof data === 'object') {
      if (Object.keys(data).length === 3 && data.database && data.table && data.pk1) {
        return 'link';
      }
      if (Object.keys(data).length === 4 && data.database &&
        data.table && data.pk1 && data.page) {
        return 'link';
      }
      if (Object.keys(data).length === 1 && data.href) {
        return 'href';
      }
      if (Object.keys(data).length === 2 && data.href && data.label) {
        return 'href';
      }
      if (Object.keys(data).length === 1 && data.img) {
        return 'img';
      }
      if (Object.keys(data).length === 2 && data.img && data.width) {
        return 'img';
      }
      if (Object.keys(data).length === 2 && data.img && data.height) {
        return 'img';
      }
      if (Object.keys(data).length === 3 && data.img && data.width && data.height) {
        return 'img';
      }
      return 'object';
    } else {
      return 'simple';
    }
  }
}
