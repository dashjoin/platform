import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppService } from '../app.service';
import { Router, Routes } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LogoutService } from '../logout.service';
import { MainComponent } from '../app.component';
import { InstanceComponent } from '../instance/instance.component';
import { Util } from '../util';

/**
 * login component
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements AfterViewInit {

  /**
   * services for communication, routing and error display
   */
  constructor(private http: HttpClient, private router: Router, private snackBar: MatSnackBar, private elementRef: ElementRef) { }

  /**
   * input field for the username - init code sets the focus here
   */
  @ViewChild('input') input: ElementRef;

  /**
   * default user defined in realms.properties
   */
  user = 'admin';

  /**
   * default pwd defined in realms.properties
   */
  password = 'dj';

  /**
   * set focus on username input field
   */
  ngAfterViewInit(): void {
    this.elementRef.nativeElement.ownerDocument.body.style.backgroundColor = 'white';
    this.elementRef.nativeElement.ownerDocument.body.style.height = '100vh';
    setTimeout(() => this.input.nativeElement.focus());
  }

  /**
   * called from login button onclick
   */
  login() {
    sessionStorage.token = btoa(this.user + ':' + this.password);
    this.http.get<string[]>('/rest/manage/roles').subscribe(res => {
      sessionStorage.user = this.user;
      sessionStorage.roles = JSON.stringify(res);
      this.snackBar.dismiss();
      this.http.post<any[]>('/rest/database/all/config/dj-role', {}).subscribe(roles => {
        for (const role of roles) {
          if (res.includes(role.ID) && role.homepage) {
            this.setRouter(role.homepage);
            return;
          }
        }
        // no match, use default
        this.setRouter('/page/Home');
      }, error => {
        // error, use default
        this.setRouter('/page/Home');
      });
    }, error => {
      delete sessionStorage.token;
      this.snackBar.open(Util.errorMsg(error), 'Ok');
    });
  }

  /**
   * set home session variable, reset routes and navigate
   */
  setRouter(home: string) {
    sessionStorage.home = home;
    this.router.resetConfig(routes);
    if (sessionStorage.djLastUrl) {
      const last = sessionStorage.djLastUrl;
      delete sessionStorage.djLastUrl;
      this.router.navigate(Util.url2array(last));
    } else {
      this.router.navigate(['/']);
    }
  }
}

/**
 * All defined routes for the application.
 *
 * Note: defined once here,
 * moved from app-routing to avoid circular dependency errors
 */
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'logout', component: LoginComponent, canActivate: [LogoutService] },
  { path: 'queryeditor', component: MainComponent, canActivate: [AppService] },
  { path: 'page/:database', component: InstanceComponent, canActivate: [AppService] },
  { path: 'search/:search', component: InstanceComponent, canActivate: [AppService] },
  { path: 'search/:sdatabase/:search', component: InstanceComponent, canActivate: [AppService] },
  { path: 'search/:sdatabase/:stable/:search', component: InstanceComponent, canActivate: [AppService] },
  { path: 'table/:tdatabase/:ttable', component: InstanceComponent, canActivate: [AppService] },
  { path: 'resource/:database/:table/:pk1', component: InstanceComponent, canActivate: [AppService] },
  { path: 'resource/:database/:table/:pk1/:pk2', component: InstanceComponent, canActivate: [AppService] },
  { path: 'resource/:database/:table/:pk1/:pk2/:pk3', component: InstanceComponent, canActivate: [AppService] },
  { path: 'resource/:database/:table/:pk1/:pk2/:pk3/:pk4', component: InstanceComponent, canActivate: [AppService] },
  { path: '**', redirectTo: sessionStorage.home ? sessionStorage.home : '/page/Home' },
];
