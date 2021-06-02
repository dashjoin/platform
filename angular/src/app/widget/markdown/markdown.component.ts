import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import marked from 'marked';
import { DashjoinWidget } from '../widget-registry';
import { Expression } from '../../expression/expression';

/**
 * markdown widget
 */
@DashjoinWidget({
  name: 'markdown',
  category: 'Default',
  description: 'Component that displays markdown',
  htmlTag: 'dj-markdown',
  fields: ['title', 'markdown', 'context']
})
@Component({
  selector: 'app-markdown',
  templateUrl: './markdown.component.html',
  styleUrls: ['./markdown.component.css']
})
export class MarkdownComponent extends DJBaseComponent implements OnInit {

  /**
   * add this to the context before printing
   */
  additionalContext: object;

  /**
   * evaluate expression if provided
   */
  initWidget() {
    if (this.layout.context) {
      this.http.get<any>('/rest/expression/' + encodeURIComponent(JSON.stringify(
        { expression: this.layout.context, data: this.context() }))
      ).subscribe(res => {
        this.additionalContext = res;
      }, this.errorHandler);
    }
  }

  /**
   * generate markdown HTML from markdown widget
   */
  markdown(): string {
    if (this.layout.markdown) {
      return marked.setOptions({}).parse(this.id(this.layout.markdown));
    }
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
