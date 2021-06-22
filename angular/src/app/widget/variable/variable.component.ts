import { Component, OnInit } from '@angular/core';
import { DashjoinWidget } from '../widget-registry';
import { ButtonComponent } from '../button/button.component';

/**
 * draws a form for editing context variable
 */
@DashjoinWidget({
  name: 'variable',
  category: 'Default',
  description: 'Component that draws a form for editing context variable',
  htmlTag: 'dj-variable',
  fields: ['title', 'properties']
})
@Component({
  selector: 'app-variable',
  templateUrl: './variable.component.html',
  styleUrls: ['./variable.component.css']
})
export class VariableComponent extends ButtonComponent implements OnInit {

  /**
   * update user settings event handler
   */
  setVariable() {
    sessionStorage.setItem('variable', JSON.stringify(this.createValue));
    this.eventChange.emit({ type: 'update' });
  }

  /**
   * copy create value to be displayed in form and generate create schema
   */
  async initWidget() {
    this.createValue = JSON.parse(sessionStorage.getItem('variable'));
    this.layoutProperties2createSchema();
  }
}
