import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * side navigation container
 */
@Component({
  selector: 'app-sidenav-switch',
  templateUrl: './sidenav-switch.component.html',
  styleUrls: ['../icon/icon.component.css']
})
@DashjoinWidget({
  name: 'sidenav-switch',
  category: 'Default',
  description: 'Component that shows the sidenav switch',
  htmlTag: 'dj-sidenav-switch',
  fields: ['roles']
})
export class SidenavSwitchComponent extends DJBaseComponent {


}
