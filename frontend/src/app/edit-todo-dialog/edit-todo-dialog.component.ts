import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Todo } from '../models/todo';

@Component({
  selector: 'app-edit-todo-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    FormsModule
  ],
  templateUrl: './edit-todo-dialog.component.html',
  styleUrls: ['./edit-todo-dialog.component.scss']
})
export class EditTodoDialogComponent {
  title: string;
  description: string;
  originalTitle: string;
  originalDescription: string;

  constructor(
    public dialogRef: MatDialogRef<EditTodoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public todo: Todo
  ) {
    this.title = todo.title;
    this.description = todo.description || '';
    this.originalTitle = todo.title;
    this.originalDescription = todo.description || '';
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.title.trim()) {
      const updatedTodo: Todo = {
        ...this.todo,
        title: this.title.trim(),
        description: this.description.trim() || undefined
      };
      this.dialogRef.close(updatedTodo);
    }
  }

  hasChanges(): boolean {
    return this.title.trim() !== this.originalTitle ||
           (this.description.trim() || '') !== this.originalDescription;
  }
}
