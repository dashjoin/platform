import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewEncapsulation } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { Expression } from '../../expression/expression';
import { DashjoinWidget } from '../widget-registry';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';

@DashjoinWidget({
  name: 'html',
  category: 'Default',
  description: 'Component that displays HTML',
  htmlTag: 'dj-html',
  fields: ['title', 'html', 'css', 'context']
})
@Component({
  selector: 'app-html',
  templateUrl: './html.component.html',
  styleUrls: ['./html.component.css'],
  //encapsulation: ViewEncapsulation.ShadowDom
})
export class HTMLComponent extends DJBaseComponent implements OnInit {

  /**
   * add this to the context before printing
   */
  additionalContext: object;

  sanHtml: SafeHtml;

  constructor(protected elRef: ElementRef, protected cdRef: ChangeDetectorRef, protected route: ActivatedRoute, public router: Router, private sanitizer: DomSanitizer) {
    super(elRef, cdRef, route, router);
  }

  /**
   * evaluate expression if provided
   */
  async initWidget() {
    if (this.layout.context) {
      this.additionalContext = await this.evaluateExpression(this.layout.context);
    }

    let html = this.id(this.layout.html);

    let useShadowDom = html.startsWith('<!--ShadowDOM-->');

    let root = useShadowDom ? this.elRef.nativeElement.attachShadow({ mode: 'open' }) :
      this.elRef.nativeElement;

    let wrapper = document.createElement('div');
    wrapper.innerHTML = html;

    let style = document.createElement('style');
    style.textContent = this.layout.css;

    root.appendChild(style);
    root.appendChild(wrapper);
  }

  /**
   * template string replacement based on context + value + additionalContext
   */
  id(name: string): string {
    const ctx = this.context();
    ctx.value = this.value;
    ctx.context = this.additionalContext;
    return Expression.template(name, ctx, null);
  }
}
