import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * toolbar spacer
 */
@DashjoinWidget({
  name: 'spacer',
  category: 'Default',
  description: 'Component that draws a toolbar spacer',
  htmlTag: 'dj-spacer',
  fields: null
})
@Component({
  selector: 'app-spacer',
  templateUrl: './spacer.component.html',
  styleUrls: ['./spacer.component.css']
})
export class SpacerComponent extends DJBaseComponent implements OnInit {
}
