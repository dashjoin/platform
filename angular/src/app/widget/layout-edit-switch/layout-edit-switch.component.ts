import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * shows the layout edit switch
 */
@DashjoinWidget({
  name: 'layout-edit-switch',
  category: 'Default',
  description: 'Component that shows the layout edit switch',
  htmlTag: 'dj-layout-edit-switch',
  fields: null
})
@Component({
  selector: 'app-layout-edit-switch',
  templateUrl: './layout-edit-switch.component.html',
  styleUrls: ['../icon/icon.component.css']
})
export class LayoutEditSwitchComponent extends DJBaseComponent implements OnInit {

  /**
   * tooltip preview of which pages were changed
   */
  saveTooltip(): string {
    const changes = [];
    for (const key of Object.keys(this.app.dirtyLayouts.page)) {
      changes.push(key);
    }
    for (const key of Object.keys(this.app.dirtyLayouts.widget)) {
      changes.push(key);
    }
    for (const key of Object.keys(this.app.dirtyLayouts.schema)) {
      changes.push(key);
    }
    if (changes.length === 0) {
      return 'Leave edit mode';
    } else {
      return 'Save layout: ' + changes;
    }
  }

  /**
   * indicates whether the layout is dirty
   */
  isDirty(): boolean {
    return this.saveTooltip() !== 'Leave edit mode';
  }

  /**
   * reload page to leave edit mode
   */
  reload() {
    window.location.reload();
  }
}
