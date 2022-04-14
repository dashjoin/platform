import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
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
  fields: ['title', 'roles', 'layout', 'if', 'foreach', 'class', 'style', 'hideframe']
})
@Component({
  selector: 'app-container',
  templateUrl: './container.component.html',
  styleUrls: ['./container.component.css']
})
export class ContainerComponent extends DJBaseComponent implements OnInit {
  /**
   * result of the this.layout.if evaluation
   */
  ifDisplay: boolean;

  /**
   * result of the foreach expression on the container
   */
  foreachResult: any[];

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
      else
        classList.add('mat-card');
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
  }
}
