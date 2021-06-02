import { Injectable } from '@angular/core';

/**
 * service with a single request counter set from DjInterceptor
 */
@Injectable({
  providedIn: 'root'
})
export class RequestCounterService {

  /**
   * http requests in flights
   */
  requestCounter = 0;
}
