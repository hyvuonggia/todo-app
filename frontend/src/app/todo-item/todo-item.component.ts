import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { Todo } from '../models/todo';
import { TodoViewDialogComponent } from '../todo-view-dialog/todo-view-dialog.component';
import { EditTodoDialogComponent } from '../edit-todo-dialog/edit-todo-dialog.component';

@Component({
  selector: 'app-todo-item',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatCheckboxModule, MatButtonModule, MatIconModule],
  templateUrl: './todo-item.component.html',
  styleUrls: ['./todo-item.component.scss']
})
export class TodoItemComponent {
  @Input() todo!: Todo;
  @Output() update = new EventEmitter<Todo>();
  @Output() delete = new EventEmitter<number>();

  constructor(private dialog: MatDialog) {}

  onCheckboxChange(): void {
    this.todo.completed = !this.todo.completed;
    this.update.emit(this.todo);
  }

  onCardClick(): void {
    const dialogRef = this.dialog.open(TodoViewDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      data: { ...this.todo }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (result.action === 'delete') {
          this.onDeleteClick();
        } else if (result.id) {
          // Handle updated todo from edit dialog (passed through view dialog)
          this.update.emit(result);
        } else if (result.completed !== undefined) {
          // Handle toggle completion from dialog
          this.todo.completed = result.completed;
          this.update.emit(this.todo);
        }
      }
    });
  }

  onDeleteClick(): void {
    this.delete.emit(this.todo.id);
  }

  openEditDialog(): void {
    const dialogRef = this.dialog.open(EditTodoDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: { ...this.todo }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.update.emit(result);
      }
    });
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
      return diffInMinutes < 1 ? 'Just now' : `${diffInMinutes}m ago`;
    } else if (diffInHours < 24) {
      return `${Math.floor(diffInHours)}h ago`;
    } else if (diffInDays < 7) {
      return `${Math.floor(diffInDays)}d ago`;
    } else {
      return date.toLocaleDateString();
    }
  }
}
