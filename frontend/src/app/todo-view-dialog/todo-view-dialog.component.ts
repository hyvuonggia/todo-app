import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Todo } from '../models/todo';
import { EditTodoDialogComponent } from '../edit-todo-dialog/edit-todo-dialog.component';

@Component({
  selector: 'app-todo-view-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule
  ],
  templateUrl: './todo-view-dialog.component.html',
  styleUrls: ['./todo-view-dialog.component.scss']
})
export class TodoViewDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<TodoViewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public todo: Todo,
    private dialog: MatDialog
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }

  onToggleComplete(): void {
    this.todo.completed = !this.todo.completed;
    this.dialogRef.close(this.todo);
  }

  onEdit(): void {
    const editDialogRef = this.dialog.open(EditTodoDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: { ...this.todo }
    });

    editDialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Close the view dialog and return the updated todo
        this.dialogRef.close(result);
      }
    });
  }

  onDelete(): void {
    this.dialogRef.close({ action: 'delete', todo: this.todo });
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';

    const date = new Date(dateString);
    const now = new Date();
    const diffInMilliseconds = now.getTime() - date.getTime();
    const diffInHours = diffInMilliseconds / (1000 * 60 * 60);
    const diffInDays = diffInHours / 24;

    if (diffInHours < 1) {
      const diffInMinutes = Math.floor(diffInMilliseconds / (1000 * 60));
      return diffInMinutes < 1 ? 'Just now' : `${diffInMinutes} minutes ago`;
    } else if (diffInHours < 24) {
      return `${Math.floor(diffInHours)} hours ago`;
    } else if (diffInDays < 7) {
      return `${Math.floor(diffInDays)} days ago`;
    } else {
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}
