import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

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
  fields: null
})
export class SidenavSwitchComponent extends DJBaseComponent {


}
