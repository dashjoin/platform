import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AppService } from '../app.service';
import { forkJoin, Observable, of } from 'rxjs';
import { WidgetRegistry } from '../widget/widget-registry';
import { catchError } from 'rxjs/operators';

/**
 * dialog to edit a JSON node in a layout structure
 */
@Component({
  selector: 'app-edit-widget-dialog',
  templateUrl: './edit-widget-dialog.component.html'
})
export class EditWidgetDialogComponent {

  /**
   * the value to edit
   */
  value: any;

  /**
   * underlying schema is set dynamically from constructor
   */
  schema: any;

  /**
   * list of "public" widgets and the options they offer
   */
  widgets = {
    // all: ['title'],
    // button: ['title', 'text', 'print', 'navigate', 'properties'],
    // card: ['title', 'text', 'roles', 'layout', 'if', 'foreach', 'class', 'style'],
    // chart: ['title', 'database', 'query', 'arguments', 'chart'],
    // container: ['title', 'roles', 'layout', 'if', 'foreach', 'class', 'style'],
    // create: ['title', 'text'],
    // display: ['title', 'display'],
    // edit: ['title', 'deleteConfirmation'],
    // variable: ['title', 'properties'],
    // editRelated: ['title', 'text', 'prop', 'columns', 'deleteConfirmation'],
    // expansion: ['title', 'text', 'roles', 'layout', 'if', 'foreach', 'class', 'style'],
    // grid: ['title', 'roles', 'if', 'foreach'],
    // icon: ['title', 'href', 'icon', 'tooltip'],
    // links: ['title'],
    // markdown: ['title', 'markdown'],
    // table: ['title', 'database', 'query', 'arguments'],
    // text: ['title', 'text', 'href', 'icon'],
  };

  /**
   * generate case field for json schema form component
   * @param prop  prop to compute case for
   */
  generateCase(prop: string): string[] {
    const res = [];
    for (const [k, v] of Object.entries(this.widgets)) {
      if ((v as string[]).includes(prop)) {
        res.push(k);
      }
    }
    return res;
  }

  /**
   * dialog constrcutor
   * @param dialogRef   disloag ref
   * @param data        data to edit
   */
  constructor(
    app: AppService, public dialogRef: MatDialogRef<EditWidgetDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { value: any, schema: any, editedWidget: string }) {
    // deep clone object so we have the possibility to cancel editing
    this.value = JSON.parse(JSON.stringify(data.value));
    if (data.schema) {
      this.schema = data.schema;
    } else {
      for (const w of WidgetRegistry.getInstance().getWidgetInfos()) {
        if (w.fields) {
          this.widgets[w.name] = w.fields;
        }
      }
      const jobs = [];
      for (const w of Object.keys(app.widget)) {
        if (data.editedWidget !== w) {
          jobs.push(app.getWidget(w).pipe(catchError(error => of(error))));
        }
      }
      forkJoin(jobs).subscribe(res => {
        for (const w of res) {
          if ((w as any)?.ID) {
            if ((w as any).ID === 'dj-toolbar' || (w as any).ID === 'dj-sidenav') {
              // ignore toolbar and sidenav since they cannot be deleted via layout editor
              continue;
            }
            // lookup root widget the custom widget uses and copy its props to the widget
            this.widgets[(w as any).ID] = this.unpack(w);
          }
        }

        this.schema = {
          title: 'Edit widget',
          type: 'object',
          layout: 'vertical',
          switch: 'widget',
          class: ['mat-elevation-z0'],
          static: true,
          properties: {
            widget: {
              static: true,
              title: 'Widget',
              type: 'string',
              widget: 'custom',
              widgetType: 'imagelist',
              enum: Object.keys(this.widgets).sort(),
            },
            icon: {
              static: true,
              title: 'Icon',
              type: 'string',
              case: this.generateCase('icon')
            },
            tooltip: {
              static: true,
              title: 'Tooltip',
              type: 'string',
              case: this.generateCase('icon')
            },
            href: {
              static: true,
              title: 'Hyperlink',
              type: 'string',
              case: this.generateCase('href')
            },
            query: {
              static: true,
              title: 'Query ID from the catalog',
              type: 'string',
              displayWith: 'fk',
              ref: 'dj/config/dj-query-catalog/ID',
              case: this.generateCase('query')
            },
            chart: {
              static: true,
              title: 'Chart type',
              type: 'string',
              enum: ['bar', 'line', 'doughnut', 'radar', 'polarArea'],
              case: this.generateCase('chart')
            },
            database: {
              static: true,
              title: 'Database',
              type: 'string',
              displayWith: 'fkdb',
              ref: 'dj/config/dj-database/ID',
              case: this.generateCase('database')
            },
            markdown: {
              static: true,
              widget: 'textarea',
              style: {
                width: '600px',
                height: '300px',
                'font-family': 'courier'
              },
              title: 'Markdown text',
              type: 'string',
              case: this.generateCase('markdown')
            },
            text: {
              static: true,
              title: 'Element text',
              type: 'string',
              case: this.generateCase('text')
            },
            deleteConfirmation: {
              static: true,
              title: 'Optional delete confirmation text',
              type: 'string',
              case: this.generateCase('deleteConfirmation')
            },
            title: {
              static: true,
              title: 'Element title on the page',
              type: 'string',
              case: this.generateCase('title')
            },
            columns: {
              static: true,
              title: 'Columns to display in related table',
              type: 'array',
              items: { type: 'string' },
              case: this.generateCase('columns')
            },
            prop: {
              static: true,
              title: 'Related table primary key',
              type: 'string',
              displayWith: 'fk',
              ref: 'dj/config/Property/ID',
              style: {
                width: '600px'
              },
              case: this.generateCase('prop')
            },
            display: {
              static: true,
              title: 'Expression',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('display')
            },
            arguments: {
              static: true,
              title: 'Query arguments expression',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('arguments')
            },
            graph: {
              static: true,
              title: 'Query is a graph query',
              type: 'boolean',
              case: this.generateCase('graph')
            },
            roles: {
              static: true,
              title: 'Restrict access to these roles',
              layout: 'select',
              type: 'array',
              choicesUrl: '/rest/database/all/config/dj-role',
              jsonata: 'ID',
              items: { type: 'string' },
              case: this.generateCase('roles')
            },
            print: {
              static: true,
              title: 'Run this when clicked and display the result',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('print')
            },
            navigate: {
              static: true,
              title: 'Run this when clicked and navigate to the URL returned',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('navigate')
            },
            if: {
              static: true,
              title: 'Show this container only if the following is true',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('if')
            },
            foreach: {
              static: true,
              title: 'Show child for each result',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('foreach')
            },
            layout: {
              static: true,
              title: 'Layout direction of the container\'s elements',
              type: 'string',
              enum: ['vertical', 'horizontal'],
              case: this.generateCase('layout')
            },
            pageLayout: {
              static: true,
              title: 'Overall page layout',
              type: 'string',
              enum: ['1 column', '2 column', '3 column', 'T 2 column', 'T 3 column', 'horizontal', 'grid'],
              case: this.generateCase('pageLayout')
            },
            class: {
              type: 'array',
              items: { type: 'string' },
              layout: 'chips',
              style: { width: '365px' },
              static: true,
              title: 'CSS classes applied to child elements',
              case: this.generateCase('class')
            },
            style: {
              type: 'object',
              additionalProperties: { type: 'string' },
              layout: 'vertical',
              static: true,
              title: 'CSS / chart styles applied to child elements',
              case: this.generateCase('style')
            },
            context: {
              static: true,
              title: 'Expression',
              type: 'string',
              widget: 'custom',
              widgetType: 'expression',
              case: this.generateCase('context')
            },
            properties: {
              type: 'object',
              additionalProperties: {
                type: 'string', enum: ['boolean', 'integer', 'number', 'string', 'date']
              },
              layout: 'vertical',
              static: true,
              title: 'Optional input for button action',
              case: this.generateCase('properties')
            }
          }
        };
      });
    }
  }

  /**
   * traverse the layout tree until a native widget shows up
   */
  unpack(w: any): any {
    // lookup root widget the custom widget uses and copy its props to the widget
    const layout = (w as any).layout;
    if (layout) {
      if (this.widgets[layout.widget]) {
        return this.widgets[layout.widget];
      } else {
        return this.unpack(layout);
      }
    } else {
      // new widget - default to text widget
      return (this.widgets as any).text;
    }
  }
}
