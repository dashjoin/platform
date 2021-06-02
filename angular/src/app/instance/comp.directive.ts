import { Directive, ViewContainerRef } from '@angular/core';

/**
 * directive to allow for dynamic component display: <ng-template appCompHost>
 */
@Directive({
  selector: '[appCompHost]',
})
export class CompDirective {
  /**
   * constructor to allow dynamic rendering of child components
   * @param viewContainerRef  container anchor
   */
  constructor(public viewContainerRef: ViewContainerRef) { }
}
