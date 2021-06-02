import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

/**
 * dialog to confirm data deletion
 */
@Component({
  selector: 'app-delete-confirm-dialog',
  templateUrl: './delete-confirm-dialog.component.html'
})
export class DeleteConfirmDialogComponent {

  /**
   * user confirmation input
   */
  input = '';

  /**
   * dialog constrcutor
   * @param dialogRef   dialog ref
   * @param data        required confirmation message and display
   */
  constructor(
    public dialogRef: MatDialogRef<DeleteConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { display: string, confirm: string }) { }
}
