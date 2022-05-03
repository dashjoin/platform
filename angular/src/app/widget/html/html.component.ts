import { ChangeDetectorRef, Component, ElementRef, OnInit, Renderer2, ViewEncapsulation } from '@angular/core';
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
  fields: ['title', 'hideframe', 'html', 'css', 'script', 'context']
})
@Component({
  selector: 'app-html',
  templateUrl: './html.component.html',
  styleUrls: ['./html.component.css']
})
export class HTMLComponent extends DJBaseComponent implements OnInit {

  /**
   * add this to the context before printing
   */
  additionalContext: object;

  sanHtml: SafeHtml;

  constructor(protected elRef: ElementRef, protected cdRef: ChangeDetectorRef, protected route: ActivatedRoute, public router: Router,
    private sanitizer: DomSanitizer, private renderer: Renderer2) {
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

    let htmlEl: HTMLElement = this.renderer.createElement('div');
    htmlEl.innerHTML = html;

    let cssEl = this.renderer.createElement('style');
    cssEl.textContent = this.layout.css;

    // Shadow DOM is active by default
    let useShadowDom = true;

    // Check if shadow DOM is disabled. Looks for:
    // <!-- encapsulation:off -->
    if (htmlEl.hasChildNodes()) {
      const n = htmlEl.childNodes[0];
      if (n.nodeType === Node.COMMENT_NODE) {
        if (n.textContent.trim().toLowerCase().startsWith('encapsulation:off')) {
          useShadowDom = false;
        }
      }
    }

    let root = useShadowDom ? this.elRef.nativeElement.attachShadow({ mode: 'open' }) :
      this.elRef.nativeElement;

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

    this.renderer.appendChild(root, cssEl);

    // @font-face definitions do NOT work in shadow DOM!
    // See issues here:
    // https://bugs.chromium.org/p/chromium/issues/detail?id=336876
    // https://github.com/google/material-design-icons/issues/1165
    // -> font files need to be loaded in root document as well
    if (useShadowDom) {
      let imports = [];

      // Find linked CSS imports in HTML
      htmlEl.
        querySelectorAll("link[rel=stylesheet]").
        forEach((a: HTMLElement) => {
          imports.push('@import url(' + a.getAttribute("href") + ');')
        });

      // Find all @import statements in CSS

      // Unfortunately we cannot rely on the browser to parse the CSS...
      // In Chrome it would work, but FF has style==null
      // const cssRules = style.sheet?.cssRules;
      // for (let i = 0; i < cssRules?.length; i++) {
      //   const rule = cssRules[i];
      //   if (rule instanceof CSSImportRule) {
      //     console.log('import', rule)
      //     imports.push(rule.cssText);
      //   }
      // }

      let lines = this.layout.css?.split('\n')
      let cssimports = lines?.filter((a) => { return a.trim().startsWith('@import ') });
      if (cssimports)
        imports.push(...cssimports);

      // Import linked styles in the root document
      if (imports && imports.length > 0) {
        let rootStyle = document.createElement('style');
        rootStyle.textContent = imports.join('\n');
        document.head.appendChild(rootStyle);
      }
    }
    this.renderer.appendChild(root, htmlEl);

    // Process scripts referred to or included in HTML
    const scripts = htmlEl.getElementsByTagName('script');
    for (let i = 0; i < scripts.length; i++) {
      const scr = scripts[i];
      //console.log('Adding script', scr);

      const scrTag = document.createElement('script');
      if (scr.src)
        scrTag.src = scr.src;
      else
        scrTag.innerHTML = scr.innerHTML;

      // Set dj.root for the script's usage
      this.setDjContext(root);
      this.renderer.appendChild(root, scrTag);
    }

    // Process the main script
    const script = this.layout.script;
    if (script) {
      const scrTag = document.createElement('script');
      //console.log('Activating script', script);
      scrTag.innerHTML = script;

      // Set dj.root for the script's usage
      this.setDjContext(root);
      this.renderer.appendChild(root, scrTag);
    }
  }

  /**
   * Initialize the window.dj context
   * @param root 
   */
  setDjContext(root) {
    const w: any = window;
    w.dj = w.dj || {};
    w.dj.root = root;
    w.dj.run = this.runExpression.bind(this);
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
