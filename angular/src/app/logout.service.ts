import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Injectable } from '@angular/core';
import { AppService } from './app.service';

/**
 * guard for the logout route that deletes the session token
 */
@Injectable({
    providedIn: 'root'
})
export class LogoutService implements CanActivate {

    /**
     * constructor
     * @param app upon logon, user caches must be deleted
     */
    constructor(private app: AppService) { }

    /**
     * delete token and allow route
     */
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        delete sessionStorage.token;
        this.app.dirtyLayouts = { page: {}, widget: {}, schema: {} };
        this.app.editLayout = false;
        this.app.cache = {};
        this.app.widget = {};
        this.app.page = {};
        return true;
    }
}
