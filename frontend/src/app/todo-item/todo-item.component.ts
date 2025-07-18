import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { Todo } from '../models/todo';
import { TodoViewDialogComponent } from '../todo-view-dialog/todo-view-dialog.component';

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
        } else if (result.action === 'edit') {
          // Future: could open edit dialog
          console.log('Edit functionality to be implemented');
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
}
