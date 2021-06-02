import { Component, OnInit } from '@angular/core';
import { environment } from './../environments/environment';

/**
 * main router component
 */
@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {

}

/**
 * main component displaying the query editor with default DB and query
 */
@Component({
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class MainComponent {

  /**
   * pre-selected query
   */
  query = 'select "EMPLOYEES"."EMPLOYEE_ID", "EMPLOYEES"."LAST_NAME" from "EMPLOYEES"';

  /**
   * pre-selected DB
   */
  database = 'dj/northwind';
}
