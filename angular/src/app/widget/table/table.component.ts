import { Component, OnInit, Input } from '@angular/core';
import { Table } from '../../model';
import { LinksComponent } from '../links/links.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * table widget
 */
@DashjoinWidget({
  name: 'table',
  category: 'Default',
  description: 'Component that displays a table',
  htmlTag: 'dj-table',
  fields: ['title', 'database', 'query', 'arguments', 'graph']
})
@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent extends LinksComponent implements OnInit {

  /**
   * default row display
   */
  @Input() rows = 10;

  /**
   * default pagination
   */
  @Input() pagination: boolean;

  /**
   * True if the data source is sortable
   */
  @Input() sortable: boolean;

  /**
   * get schema and call doLayout
   */
  async ngOnInit() {
    try {
      super.ngOnInit();

      this.meta = await this.getData().getMeta();
      this.queryMeta = this.meta?.schema as Table;

      // Set pagination and sorting capabilities from meta data
      // (preset @Input values have priority)
      this.pagination ||= this.meta?.paging;
      this.sortable ||= this.meta?.sortCaps?.sortableFields != null;

      if (this.meta.size)
        this.allLength = this.meta.size;
    } catch (e) {
      this.errorHandler(e);
    }
  }

  /**
   * on the search page, combine link and id column into one
   */
  computeColumnsFromAll() {
    super.computeColumnsFromAll();

    if (this.search) {
      // remove id column from the display; it is used as the href label for url
      this.columns.shift();
    }
  }

  /**
   * trigger initial display
   */
  async initWidget() {
    try {
      this.page({ pageIndex: 0, pageSize: this.rows, length: null });
    } catch (e) {
      this.errorHandler(e);
    }
  }

  /**
   * special path json structure
   */
  isPath(value: any): boolean {
    if (value)
      if (this.typeof(value, 'object'))
        if (Object.keys(value).length === 2)
          if (value.start?._dj_resource) {
            if (Array.isArray(value.steps))
              return true;
          }
    return false;
  }

  /**
   * use the property title if present
   */
  localName(s: string): string {
    if (this.queryMeta?.properties?.[s]?.title)
      return this.queryMeta.properties[s].title;
    return super.localName(s);
  }
}
