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
   * compute expressions
   */
  async initWidget() {
    if (this.layout.display) {
      this.displayData = await this.evaluateExpression(this.layout.display);
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
}
