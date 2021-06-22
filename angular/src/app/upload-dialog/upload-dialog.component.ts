import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatRadioChange } from '@angular/material/radio';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DeleteConfirmDialogComponent } from '../delete-confirm-dialog/delete-confirm-dialog.component';

/**
 * displays the data upload dialog
 */
@Component({
  selector: 'app-upload-dialog',
  templateUrl: './upload-dialog.component.html',
  styleUrls: ['./upload-dialog.component.css']
})
export class UploadDialogComponent {

  /**
   * constructor
   * @param dialog      open confirmation dialog
   * @param http        http backend communication
   * @param snackBar    error display
   * @param dialogRef   self reference for closing the dialog
   * @param database    the database we are uploading to
   */
  constructor(
    public dialog: MatDialog,
    private http: HttpClient, private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<UploadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public database: string) { }

  /**
   * upload is a 2-step process, this var keeps the results of the "inspection"
   * step and allows the user to make changes
   */
  tables: any = { '': [{ sample: ['', '', '', '', '', '', '', '', '', ''] }] };

  /**
   * are we creating new tables or uploading into existing tables
   */
  createMode: boolean;

  /**
   * upload form
   */
  formData: FormData;

  /**
   * are we on the config DB (allows model folder upload)?
   */
  isConfig(): boolean {
    return window.location.href.endsWith('/dj%2Fconfig');
  }

  /**
   * called when upload happens
   */
  handleFileInput(files: FileList) {

    if (files.length === 0) {
      return;
    }

    let total = 0;
    for (let i = 0; i < files.length; i++) {
      total = total + files.item(i).size;
    }

    // limit to 10 MB
    if (total > 10 * 1024 * 1024) {
      this.snackBar.open('Uploads are limited to 10MB', 'Ok');
      return;
    }

    this.formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      const path = (files.item(i) as any).webkitRelativePath ?
        (files.item(i) as any).webkitRelativePath : files.item(i).name;
      this.formData.append('file', files.item(i), encodeURIComponent(path));
    }
    this.http.post<any>('/rest/manage/detect?database=' + this.database, this.formData).subscribe(res => {
      this.tables = res.schema;
      this.createMode = res.createMode;
    }, error => {
      this.snackBar.open(error.error, 'Ok');
    });
  }

  /**
   * create new tables
   */
  create() {
    this.formData.append('__dj_schema', JSON.stringify(this.tables));
    this.dialogRef.close(this.http.post<any>('/rest/manage/create?database=' + this.database, this.formData));
  }

  /**
   * append existing tables
   */
  append() {
    this.dialogRef.close(this.http.post<any>('/rest/manage/append?database=' + this.database, this.formData));
  }

  /**
   * replace data in existing tables
   */
  replace() {
    this.dialog.open(DeleteConfirmDialogComponent,
      {
        data: {
          confirm: 'delete contents',
          display: 'You are replacing the contents of the tables: ' + Object.keys(this.tables) +
            '. The current contents of these tables will be deleted. This operation cannot be reverted!'
        }
      }).afterClosed().subscribe(res => {
        if (res) {
          this.dialogRef.close(this.http.post<any>('/rest/manage/replace?database=' + this.database, this.formData));
        }
      });
  }

  /**
   * catch radio button change event (select pk col)
   */
  radio(event: string, table: any) {
    for (const t of table) {
      t.pk = event === t.name;
    }
  }

  /**
   * compute the pk column name
   */
  radioValue(table): string {
    for (const col of table) {
      if (col.pk) {
        return col.name;
      }
    }
    return undefined;
  }
}
