import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { ContainerComponent } from '../container/container.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * toolbar container
 */
@DashjoinWidget({
  name: 'toolbar',
  category: 'Default',
  description: 'Component that draws a toolbar container',
  htmlTag: 'dj-toolbar',
  isContainer: true,
  fields: null
})
@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class ToolbarComponent extends ContainerComponent implements OnInit {
}
