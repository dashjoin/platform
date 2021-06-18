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
  fields: ['title', 'roles', 'layout', 'if', 'foreach', 'class', 'style']
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
  initWidget() {
    /* tslint:disable */
    if (this.layout['if']) {
      this.http.get<any>('/rest/expression/' + encodeURIComponent(JSON.stringify(
        { expression: this.layout['if'], data: this.context() }))).subscribe(res => {
          this.ifDisplay = res;
          this.hideParentPageCard();
          this.cdRef.detectChanges();
        }, this.errorHandler);
    } else {
      this.ifDisplay = true;
    }
    if (this.layout['foreach']) {
      this.http.get<any>('/rest/expression/' + encodeURIComponent(JSON.stringify(
        { expression: this.layout['foreach'], data: this.context() }))).subscribe(res => {
          this.foreachResult = res;
          console.log(res)
          this.cdRef.detectChanges();
        }, this.errorHandler);
    }
    /* tslint:enable */
  }
}
