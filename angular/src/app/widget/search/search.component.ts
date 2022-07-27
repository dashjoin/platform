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
   * avoid reopening of popup after close
   */
  justClosed = false;

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
  initSearchContext() {
    if (this.route.snapshot.paramMap.get('tdatabase') && this.route.snapshot.paramMap.get('ttable')) {
      // if we are on a table page, search defaults to searching this table only
      this.searchdatabase = this.route.snapshot.paramMap.get('tdatabase');
      this.searchtable = this.route.snapshot.paramMap.get('ttable');
    }
    else {
      // if present, get search scope from URL
      this.searchdatabase = this.route.snapshot.paramMap.get('sdatabase');
      this.searchtable = this.route.snapshot.paramMap.get('stable');
    }
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

  /**
   * show the popup and load the data if necessary
   */
  doShow() {

    this.initSearchContext();

    if (this.justClosed) {
      this.justClosed = false;
      return;
    }
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
        this.show = true;
      })
    } else {
      this.show = true;
    }
  };

  /**
   * close the popup, this causes the input to get focus, avoid reopening via justClosed flag
   */
  close() {
    this.justClosed = true;
    this.show = false;
  }
}
