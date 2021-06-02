import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Util } from '../../util';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * displays a text with optional link and icon
 */
@DashjoinWidget({
  name: 'text',
  category: 'Default',
  description: 'Component that displays a text with optional link and icon',
  htmlTag: 'dj-text',
  fields: ['title', 'text', 'href', 'icon']
})
@Component({
  selector: 'app-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.css']
})
export class TextComponent extends DJBaseComponent implements OnInit {

  /**
   * used in case of a "a" element with href, onClick does not work here, need this to feed routerLink
   */
  linkArray(s: string): string[] {
    const lastSlash = s.lastIndexOf('/');
    const lastQuestionmark = s.lastIndexOf('?');
    if (lastSlash < lastQuestionmark) {
      s = s.substring(0, lastQuestionmark);
    }
    const arr = s.split('/').map(x => decodeURIComponent(x));
    if (s.startsWith('/resource/config/Table/')) {
      const table = Util.parseTableID(arr[4]);
      table[0] = '/table';
      return table;
    }
    return arr;
  }

  /**
   * parse and return query parameters
   */
  queryParams(s: string) {
    const lastSlash = s.lastIndexOf('/');
    const lastQuestionmark = s.lastIndexOf('?');
    if (lastSlash < lastQuestionmark) {
      let queryPar = '';
      queryPar = s.substring(lastQuestionmark + 1);
      const res = {};
      for (const part of queryPar.split('&')) {
        const kv = part.split('=');
        res[kv[0]] = decodeURIComponent(kv[1]);
      }
      return res;
    }
  }

  /**
   * noop
   */
  doLayout() {
  }
}
