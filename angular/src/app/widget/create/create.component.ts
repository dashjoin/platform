import { Component, OnInit } from '@angular/core';
import { Table } from '../../model';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * Component that allows creating new records
 */
@DashjoinWidget({
  name: 'create',
  category: 'Default',
  description: 'Component that allows creating new records',
  htmlTag: 'dj-create',
  fields: ['title', 'text']
})
@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent extends DJBaseComponent implements OnInit {

  /**
   * create instance event handler
   */
  async onCreate() {
    try {
      let res = await this.getData().create(this.createValue);
      this.snackBar.open('Done', 'Ok', { duration: 3000 });
      if (res.startsWith('dj/config/Property/')) {
        res = decodeURIComponent(res.substr('dj/config/Property/'.length));
        delete this.app.cache[res];
        this.eventChange.emit({ type: 'redraw' });
      } else {
        this.eventChange.emit({ type: 'create' });
      }
    } catch (e) {
      this.errorHandler(e);
    }
  }

  /**
   * get create schema
   */
  async initWidget() {
    if (this.layout?.createSchema) {
      this.createSchema = this.layout.createSchema;
    } else {
      const schema = (await this.getData().getMeta()).schema as Table;
      // copy the schema since it might be edited and we do not want to change a cached version
      this.createSchema = JSON.parse(JSON.stringify(schema));
      this.app.log('got schema', this.createSchema);
    }
  }
}
