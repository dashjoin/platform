import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * edit form for a record
 */
@DashjoinWidget({
  name: 'edit',
  category: 'Default',
  description: 'Component that displays an edit form for a record',
  htmlTag: 'dj-edit',
  fields: ['title', 'deleteConfirmation']
})
@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css']
})
export class EditComponent extends DJBaseComponent implements OnInit {

  /**
   * init with special handling for config/Table
   */
  ngOnInit(): void {
    super.ngOnInit();

    // Special case: data context = metadata if we're on a table page
    // TODO: this should not be hardcoded but come from widget/page config !
    if (this.database === 'config' && this.table === 'Table') {
      this.data = 'dj/config/Table';
    }
  }

  /**
   * get edit schema
   */
  async initWidget() {
    if (this.layout.schema) {
      this.schema = this.layout.schema;
    } else {
      this.app.getSchema(this.database, this.table).subscribe({
        next: res => { this.schema = JSON.parse(JSON.stringify(res)); },
        error: this.errorHandler.bind(this)
      });
    }
  }

  /**
   * update instance event handler
   */
  async update() {
    try {
      // clone and remove keys that are not part of the form
      const clone = JSON.parse(JSON.stringify(this.value));
      for (const p of Object.keys(clone)) {
        if (this.schema.properties && !Object.keys(this.schema.properties).includes(p)) {
          delete clone[p];
        }
      }

      const done = await this.getData().update(this.getKey(), clone);
      if (!done) { throw new Error('Error during update'); }

      this.snackBar.open('Done', 'Ok', { duration: 3000 });
      this.eventChange.emit({ type: 'update' });
    } catch (e) {
      this.errorHandler(e);
    }
  }
}
