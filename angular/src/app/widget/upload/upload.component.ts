import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { UploadDialogComponent } from '../../upload-dialog/upload-dialog.component';
import { Observable } from 'rxjs';
import { DashjoinWidget } from '../widget-registry';
import { Util } from '../../util';

/**
 * upload dialog
 */
@DashjoinWidget({
  name: 'upload',
  category: 'Default',
  description: 'Component that displays a button to open the upload dialog',
  htmlTag: 'dj-upload',
  fields: null
})
@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent extends DJBaseComponent implements OnInit {

  /**
   * opens the upload dialog
   */
  upload() {
    const dialogRef = this.dialog.open(UploadDialogComponent, { width: '90%', data: Util.parseDatabaseID(this.pk1)[1] });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        (result as Observable<void>).subscribe(_ => {
          this.snackBar.open('Done', 'Ok', { duration: 3000 });
          this.eventChange.emit({ type: 'redraw' });
        }, this.errorHandler);
      }
    });
  }
}
