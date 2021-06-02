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
  fields: ['title', 'display']
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
   * compute expressions
   */
  initWidget() {
    this.http.get<any>('/rest/expression/' + encodeURIComponent(JSON.stringify(
      { expression: this.layout.display, data: this.context() }))
    ).subscribe(res => {
      this.displayData = res;
    }, this.errorHandler);
  }

  /**
   * used for display widget: transforms camelCase into Camel Case
   */
  camelCase2display(s: string) {
    return s.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
  }

  /**
   * some default key to icon associations
   */
  iconKey(s: string): string {
    if (s === 'status') { return 'error_outline'; }
    if (s === 'user') { return 'account_circle'; }
    if (s === 'roles') { return 'person_search'; }
    if (s === 'buildTime') { return 'access_time'; }
    if (s === 'runtime') { return 'build'; }
    if (s === 'version') { return 'content_copy'; }
    if (s === 'vendor') { return 'store'; }
    if (s === 'subscription') { return 'payment'; }
    if (s === 'name') { return 'check_circle_outline'; }
    return s;
  }

  /**
   * detects the type of displayData such that the UI can select the appropriate visualization
   */
  displayType(): 'link' | 'string' | 'string[]' | 'object' | 'object[]' {
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
      }
      return typeof this.displayData === 'object' ? 'object' : 'string';
    }
  }
}
