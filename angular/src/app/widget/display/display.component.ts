import { KeyValue } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { TextComponent } from '../text/text.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * displays values, arrays, or tables
 */
@DashjoinWidget({
  name: 'display',
  category: 'Default',
  description: 'Component that displays values, arrays, or tables',
  htmlTag: 'dj-display',
  fields: ['title', 'display', 'icons']
})
@Component({
  selector: 'app-display',
  templateUrl: './display.component.html',
  styleUrls: ['../table/table.component.css']
})
export class DisplayComponent extends TextComponent implements OnInit {

  /**
   * structure for display widget
   */
  displayData: any;

  /**
   * table columns
   */
  columns: string[] = [];

  /**
   * compute expressions
   */
  async initWidget() {
    if (this.layout.display) {
      this.displayData = await this.evaluateExpression(this.layout.display);

      if (this.displayType() === 'object[]') {
        for (const row of this.displayData) {
          for (const field of Object.keys(row)) {
            if (!this.columns.includes(field)) {
              this.columns.push(field);
            }
          }
        }
      }
    }
  }

  /**
   * used for display widget: transforms camelCase into Camel Case
   */
  camelCase2display(s: string) {

    // If string consists of multiple words, it's no camel case
    if (s.indexOf(' ') > 0)
      return s;

    return s.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
  }

  /**
   * Maps the text key to Material icon
   *
   * Looks up the icon from the _dj_icons map (if defined).
   * Otherwise directly takes the key as icon.
   * 
   * See https://fonts.google.com/icons for list of icons
   */
  iconKey(s: string): string {
    if (this.layout.icons?.[s]) {
      return this.layout.icons[s];
    }
    if (this.layout.icons?.['*']) {
      return this.layout.icons['*'];
    }
    return 'arrow_forward';
  }

  // Preserve original property order
  originalOrder = (a: KeyValue<number, string>, b: KeyValue<number, string>): number => {
    return 0;
  }

  /**
   * is the data to be displayed a URL?
   */
  isUrl(x: any): boolean {
    if (typeof x === 'string') {
      return x.startsWith('http://') || x.startsWith('https://');
    } else {
      return false;
    }
  }

  /**
   * detects the type of displayData such that the UI can select the appropriate visualization
   */
  displayType(): 'link' | 'string' | 'string[]' | 'object' | 'object[]' | 'img' {
    if (!this.displayData) {
      return 'string';
    }
    if (Array.isArray(this.displayData)) {
      if (this.displayData.length > 0) {
        return typeof this.displayData[0] === 'object' ? 'object[]' : 'string[]';
      } else {
        return 'string[]';
      }
    } else {
      if (typeof this.displayData === 'object') {
        if (Object.keys(this.displayData).length === 3 && this.displayData.database && this.displayData.table && this.displayData.pk1) {
          return 'link';
        }
        if (Object.keys(this.displayData).length === 4 && this.displayData.database &&
          this.displayData.table && this.displayData.pk1 && this.displayData.page) {
          return 'link';
        }
        if (Object.keys(this.displayData).length === 1 && this.displayData.img) {
          return 'img';
        }
        if (Object.keys(this.displayData).length === 2 && this.displayData.img && this.displayData.width) {
          return 'img';
        }
        if (Object.keys(this.displayData).length === 2 && this.displayData.img && this.displayData.height) {
          return 'img';
        }
        if (Object.keys(this.displayData).length === 3 && this.displayData.img && this.displayData.width && this.displayData.height) {
          return 'img';
        }
      }
      return typeof this.displayData === 'object' ? 'object' : 'string';
    }
  }
}
