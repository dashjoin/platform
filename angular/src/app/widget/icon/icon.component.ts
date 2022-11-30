import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { TextComponent } from '../text/text.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * displays an icon with tooltip and hyperlink
 */
@DashjoinWidget({
  name: 'icon',
  category: 'Default',
  description: 'Component that displays an icon with tooltip and hyperlink',
  htmlTag: 'dj-icon',
  fields: ['title', 'roles', 'href', 'icon', 'tooltip']
})
@Component({
  selector: 'app-icon',
  templateUrl: './icon.component.html',
  styleUrls: ['./icon.component.css']
})
export class IconComponent extends TextComponent implements OnInit {

  /**
   * react to icon click, depending on href, use href or routerLink
   */
  icon() {
    const href = this.id(this.layout.href);
    if (href) {
      if (href.startsWith('https://dashjoin.github.io/platform/latest')) {
        if (this.app.editLayout || this.pk1 === 'dj/config/page' || this.table === 'page' ||
          this.pk1 === 'dj/config/widget' || this.table === 'widget') {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#user-interface-layout', '_blank');
        } else if (this.pk1 === 'dj/config/dj-query-catalog' || this.table === 'dj-query-catalog') {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#query-catalog-and-editor', '_blank');
        } else if (this.pk1 === 'dj/config/dj-function' || this.table === 'dj-function') {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#functions', '_blank');
        } else if (this.pk1 === 'dj/config/dj-database' || this.table === 'dj-database') {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#data-and-database-management', '_blank');
        } else if (this.pk1 === 'dj/config/dj-role' || this.table === 'dj-role') {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#access-control', '_blank');
        } else if (this.search) {
          window.open('https://dashjoin.github.io/platform/latest/developer-reference/#search-page', '_blank');
        } else {
          window.open(href, '_blank');
        }
      } else if (this.layout.href.startsWith('http')) {
        window.open(href, '_blank');
      } else {
        if (this.queryParams(href)) {
          this.router.navigate(this.linkArray(href), { queryParams: this.queryParams(href) });
        } else {
          this.router.navigate(this.linkArray(href));
        }
      }
    }
  }

  /**
   * is this the base version (used to determine how to show the logout button)
   */
  isBaseVersion(): boolean {
    return !(window.location.href.includes('my.dashjoin.org') || window.location.href.includes('my.dashjoin.com'));
  }
}
