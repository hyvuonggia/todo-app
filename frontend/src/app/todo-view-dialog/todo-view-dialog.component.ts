import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Todo } from '../models/todo';

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
    @Inject(MAT_DIALOG_DATA) public todo: Todo
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }

  onToggleComplete(): void {
    this.todo.completed = !this.todo.completed;
    this.dialogRef.close(this.todo);
  }

  onEdit(): void {
    this.dialogRef.close({ action: 'edit', todo: this.todo });
  }

  onDelete(): void {
    this.dialogRef.close({ action: 'delete', todo: this.todo });
  }
}
