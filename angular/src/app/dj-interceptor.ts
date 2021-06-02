import { HttpInterceptor, HttpHandler, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { RequestCounterService } from './request-counter.service';

/**
 * global interceptor that adds a simple basic auth header to all REST calls
 */
@Injectable()
export class DjInterceptor implements HttpInterceptor {

    /**
     * track request count
     */
    constructor(private counter: RequestCounterService) { }

    /**
     * set basic auth
     */
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

        setTimeout(() => {
            this.counter.requestCounter++;
        }, 0);

        request = request.clone({
            setHeaders: {
                Authorization: 'Basic ' + sessionStorage.token
            }
        });
        return next.handle(request).pipe(finalize(() => {
            setTimeout(() => {
                this.counter.requestCounter--;
            }, 0);
        }));
    }
}
