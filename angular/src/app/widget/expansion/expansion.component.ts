import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { ContainerComponent } from '../container/container.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * expansion container
 */
@DashjoinWidget({
  name: 'expansion',
  category: 'Default',
  description: 'Component that draws an expansion container',
  htmlTag: 'dj-expansion',
  isContainer: true,
  fields: ['title', 'text', 'roles', 'layout', 'if', 'foreach', 'class', 'style']
})
@Component({
  selector: 'app-expansion',
  templateUrl: './expansion.component.html',
  styleUrls: ['./expansion.component.css']
})
export class ExpansionComponent extends ContainerComponent implements OnInit {
}
