import { ChangeDetectorRef, Component, ElementRef, NgZone, OnInit, Input, Output, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * container with no layout
 */
@DashjoinWidget({
  name: 'container',
  category: 'Default',
  description: 'Component that draws a container',
  htmlTag: 'dj-container',
  isContainer: true,
  fields: ['title', 'roles', 'layout', 'if', 'foreach', 'class', 'style', 'hideframe', 'redrawInterval']
})
@Component({
  selector: 'app-container',
  templateUrl: './container.component.html',
  styleUrls: ['./container.component.css']
})
export class ContainerComponent extends DJBaseComponent implements OnInit, OnDestroy {
  /**
   * result of the this.layout.if evaluation
   */
  ifDisplay: boolean;

  /**
   * result of the foreach expression on the container
   */
  foreachResult: any[];

  /**
   * Timer hanndle for redraw interval callback
   */
  redrawTimer //: NodeJS.Timer;

  /**
   * In callback flag
   */
  inRedrawCallback = false;

  /**
   * if displaycondition is false and we are the only app-instance in the parent, then toggle display depending on edit mode
   */
  hideParentPageCard() {
    if (!this.ifDisplay) {
      let count = 0;
      for (const el of this.elRef.nativeElement.parentNode.parentNode.parentNode.childNodes) {
        if ('APP-INSTANCE' === el.nodeName) {
          count++;
        }
      }
      if (count === 1) {
        this.elRef.nativeElement.parentNode.parentNode.parentNode.style.display = this.app.editLayout ? 'block' : 'none';
      }
    }
  }

  /**
   * Starts the redraw interval callback (if configured with interval >0).
   * Limits the interval to 100ms (10x per second)
   */
  initRedraw() {
    this.stopRedraw();
    if (this.layout.redrawInterval) {
      //console.log('redrawInterval ' + this.layout.redrawInterval);
      let millis = this.layout.redrawInterval * 1000;
      // Safeguard:
      if (millis < 100)
        millis = 100;
      this.redrawTimer = setInterval(() => this.redrawCallback(), millis);
    }
  }

  /**
   * Stops the redraw callback
   */
  stopRedraw() {
    if (this.redrawTimer) {
      clearInterval(this.redrawTimer);
      this.redrawTimer = undefined;
    }
  }

  /**
   * Redraw callback.
   * Only allows single call at a time to protect from queueing up requests
   * 
   * @returns
   */
  redrawCallback() {

    if (this.inRedrawCallback) return;

    const w: any = window;
    this.inRedrawCallback = true;
    try {
      // Set redrawCount in dj context
      this.redrawCount++;
      w.dj = w.dj || {};
      w.dj.redrawCount = this.redrawCount;

      // Disable caches for redrawn components
      w.djNoCache = true;

      this.redraw();
      if (this.children) {
        for (const c of this.children) {
          c.redraw();
        }
      }
    } finally {
      this.inRedrawCallback = false;

      // Re-enable caches
      // Note: when not using setTimeout, the async http requests
      // are executed BEFORE resetting the flag
      setTimeout(() => { (window as any).djNoCache = undefined; })
    }
  }

  ngOnDestroy() {
    this.stopRedraw();
  }

  /**
   * handle if and foreach expressions
   */
  async initWidget() {
    const layout = this.layout as any;

    // Show/hide the widget's frame
    try {
      const classList = (this.elRef.nativeElement as HTMLElement).parentElement.parentElement.parentElement.classList;
      // Remove mat-card class from parent tag <mat-card ... >
      // Bit of a hack, but does the job...
      if (this.layout.hideframe)
        classList.remove('mat-card');
    } catch (e) { console.warn(e); }


    if (layout.if) {
      this.ifDisplay = await this.evaluateExpression(layout.if);
      this.hideParentPageCard();
      this.cdRef.detectChanges();
    } else {
      this.ifDisplay = true;
    }
    if (layout.foreach) {
      this.foreachResult = await this.evaluateExpression(layout.foreach);
      this.cdRef.detectChanges();
    }

    this.initRedraw();
  }
}
