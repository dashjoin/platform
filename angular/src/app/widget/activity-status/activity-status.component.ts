import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DjInterceptor } from 'src/app/dj-interceptor';
import { RequestCounterService } from '../../request-counter.service';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * shows communication activity
 */
@DashjoinWidget({
  name: 'activity-status',
  category: 'Default',
  description: 'Component that shows communication activity',
  htmlTag: 'dj-activity-status',
  fields: null
})
@Component({
  selector: 'app-activity-status',
  templateUrl: './activity-status.component.html',
  styleUrls: ['./activity-status.component.css']
})
export class ActivityStatusComponent extends DJBaseComponent implements OnInit {

  constructor(
    public counter: RequestCounterService,
    protected elRef: ElementRef, protected cdRef: ChangeDetectorRef, protected route: ActivatedRoute, public router: Router) {
    super(elRef, cdRef, route, router);
  }
}
