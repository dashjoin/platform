import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { InstanceComponent } from '../../instance/instance.component';
import { ContainerComponent } from '../container/container.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * grid layout comtainer
 */
@DashjoinWidget({
  name: 'grid',
  category: 'Default',
  description: 'Component that draws a an action button',
  htmlTag: 'dj-grid',
  // TODO: standalone grid is broken at the moment, only works as a layout option for page
  isContainer: true,
  fields: null
})
@Component({
  selector: 'app-grid',
  templateUrl: './grid.component.html',
  styleUrls: ['./grid.component.css']
})
export class GridComponent extends ContainerComponent implements OnInit {

  /**
   * init grid options
   */
  initWidget() {
    super.initWidget();
    this.options = {
      draggable: {
        enabled: this.app.editLayout,
      },
      resizable: {
        enabled: this.app.editLayout,
      },
      margin: 4,
      itemChangeCallback: InstanceComponent.itemChange,
      ref: this
    };
  }
}
