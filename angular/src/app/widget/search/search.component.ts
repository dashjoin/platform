import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

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
  navigate() {
    if (this.search) {
      this.router.navigate(['/search', this.search]);
    }
  }
}
