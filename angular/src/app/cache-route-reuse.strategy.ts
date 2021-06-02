import { RouteReuseStrategy, ActivatedRouteSnapshot, DetachedRouteHandle } from '@angular/router';

/**
 * caching for angular routes
 * currently inactive. Normally goes in app.module.ts:
 * providers: [{ provide: RouteReuseStrategy, useClass: CacheRouteReuseStrategy }],
 */
export class CacheRouteReuseStrategy implements RouteReuseStrategy {

    /**
     * page cache
     */
    cache: { [key: string]: DetachedRouteHandle } = {};

    /**
     * retrieve URL from route
     */
    url(route: ActivatedRouteSnapshot): string {
        return route.url.map(segment => segment.toString()).join('/');
    }

    /**
     * we can cache everything
     */
    shouldDetach(route: ActivatedRouteSnapshot): boolean {
        return true;
    }

    /**
     * cache the handle
     */
    store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle): void {
        this.cache[this.url(route)] = handle;
    }

    /**
     * if we have the page in the cache we use it
     */
    shouldAttach(route: ActivatedRouteSnapshot): boolean {
        if (this.cache[this.url(route)]) {
            return true;
        }
        return false;
    }

    /**
     * cache lookup
     */
    retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle {
        return this.cache[this.url(route)];
    }

    /**
     * reuse the route if we're going to and from the same route
     */
    shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
        return this.url(future) === this.url(curr);
    }
}
