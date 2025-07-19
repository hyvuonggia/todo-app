import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { Todo, Category } from '../models/todo';
import { CategoryService } from '../services/category.service';

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
    MatSelectModule,
    FormsModule,
    QuillModule
  ],
  templateUrl: './edit-todo-dialog.component.html',
  styleUrls: ['./edit-todo-dialog.component.scss']
})
export class EditTodoDialogComponent implements OnInit {
  title: string;
  description: string;
  selectedCategoryId: number | null = null;
  originalTitle: string;
  originalDescription: string;
  originalCategoryId: number | null = null;
  categories: Category[] = [];

  quillModules = {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'color': [] }, { 'background': [] }],
      ['clean']
    ]
  };

  constructor(
    public dialogRef: MatDialogRef<EditTodoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public todo: Todo,
    private categoryService: CategoryService
  ) {
    this.title = todo.title;
    this.description = todo.description || '';
    this.selectedCategoryId = todo.category?.id || null;
    this.originalTitle = todo.title;
    this.originalDescription = todo.description || '';
    this.originalCategoryId = todo.category?.id || null;
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
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

      // Handle category assignment
      if (this.selectedCategoryId) {
        const selectedCategory = this.categories.find(c => c.id === this.selectedCategoryId);
        if (selectedCategory) {
          updatedTodo.category = selectedCategory;
        }
      } else {
        updatedTodo.category = undefined;
      }

      this.dialogRef.close(updatedTodo);
    }
  }

  hasChanges(): boolean {
    return this.title.trim() !== this.originalTitle ||
           (this.description.trim() || '') !== this.originalDescription ||
           this.selectedCategoryId !== this.originalCategoryId;
  }
}
