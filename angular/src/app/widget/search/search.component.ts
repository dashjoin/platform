import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';
import { Util } from '../../util';

/**
 * search input box
 */
@DashjoinWidget({
  name: 'search',
  category: 'Default',
  description: 'Component that displays a search input box',
  htmlTag: 'dj-search',
  fields: null
})
@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent extends DJBaseComponent implements OnInit {

  /**
   * show popup containing db and table dropdowns
   */
  show = false;

  /**
   * data structure for the db and table dropdowns
   */
  db2table: { [key: string]: string[] };

  /**
   * search on this DB, * for all DBs
   */
  searchdatabase: string;

  /**
   * search on this table, * for all tables
   */
  searchtable: string;

  /**
   * init searchtable and db from URL parameters
   */
  async initWidget() {
    this.searchdatabase = this.route.snapshot.paramMap.get('sdatabase');
    this.searchtable = this.route.snapshot.paramMap.get('stable');
    if (!this.searchdatabase) this.searchdatabase = '*';
    if (!this.searchtable) this.searchtable = '*';
  }

  /**
   * perform the search
   */
  navigate() {
    if (this.search) {
      if (this.searchdatabase === '*')
        this.router.navigate(['/search', this.search]);
      else
        if (this.searchtable === '*')
          this.router.navigate(['/search', this.searchdatabase, this.search]);
        else
          this.router.navigate(['/search', this.searchdatabase, this.searchtable, this.search]);
    }
  }

  doShow() {
    if (!this.db2table) {
      this.db2table = {};
      this.http.get<string[]>('/rest/database/tables').subscribe((res) => {
        for (const table of res) {
          const part = Util.parseTableID(table);
          if (!this.db2table[part[1]]) {
            this.db2table[part[1]] = [];
          }
          this.db2table[part[1]].push(part[2]);
        }
        console.log(this.db2table)
        this.show = true;
      })
    } else {
      this.show = true;
    }
  };

  focus() {
    console.log('focus')
  }

  blur() {
    console.log('blur')
  }
}
