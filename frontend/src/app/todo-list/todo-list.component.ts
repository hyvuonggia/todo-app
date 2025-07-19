import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { QuillModule } from 'ngx-quill';
import { TodoService } from '../services/todo.service';
import { CategoryService } from '../services/category.service';
import { Todo, Category } from '../models/todo';
import { TodoItemComponent } from '../todo-item/todo-item.component';
import { AddTodoDialogComponent } from '../add-todo-dialog/add-todo-dialog.component';
import { CategoryDialogComponent } from '../category-dialog/category-dialog.component';

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TodoItemComponent,
    QuillModule
  ],
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.scss']
})
export class TodoListComponent implements OnInit {
  todos: Todo[] = [];
  categories: Category[] = [];
  selectedCategoryId: number | null = null;

  constructor(
    private todoService: TodoService,
    private categoryService: CategoryService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadTodos();
    this.loadCategories();
  }

  loadTodos(): void {
    if (this.selectedCategoryId === 0) {
      // Filter for uncategorized todos (category is null)
      this.todoService.getTodos().subscribe(todos => {
        this.todos = todos.filter(todo => !todo.category);
      });
    } else if (this.selectedCategoryId !== null) {
      this.todoService.getTodosByCategory(this.selectedCategoryId).subscribe(todos => {
        this.todos = todos;
      });
    } else {
      this.todoService.getTodos().subscribe(todos => {
        this.todos = todos;
      });
    }
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe(categories => {
      this.categories = categories;
    });
  }

  onCategoryFilterChange(): void {
    this.loadTodos();
  }

  openAddTodoDialog(): void {
    const dialogRef = this.dialog.open(AddTodoDialogComponent, {
      width: '500px',
      disableClose: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.addTodo(result);
      }
    });
  }

  openCategoryDialog(category?: Category): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      width: '450px',
      data: { category }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (category) {
          this.updateCategory(category.id!, result);
        } else {
          this.createCategory(result);
        }
      }
    });
  }

  createCategory(categoryData: Partial<Category>): void {
    this.categoryService.createCategory(categoryData as Category).subscribe({
      next: (category) => {
        this.categories.push(category);
      },
      error: (error) => {
        console.error('Error creating category:', error);
      }
    });
  }

  updateCategory(id: number, categoryData: Partial<Category>): void {
    this.categoryService.updateCategory(id, categoryData as Category).subscribe({
      next: (updatedCategory) => {
        const index = this.categories.findIndex(c => c.id === id);
        if (index !== -1) {
          this.categories[index] = updatedCategory;
        }
        this.loadTodos(); // Reload todos to reflect category changes
      },
      error: (error) => {
        console.error('Error updating category:', error);
      }
    });
  }

  deleteCategory(id: number): void {
    if (confirm('Are you sure you want to delete this category? Todos in this category will become uncategorized.')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.categories = this.categories.filter(c => c.id !== id);
          if (this.selectedCategoryId === id) {
            this.selectedCategoryId = null;
          }
          this.loadTodos(); // Reload todos to reflect category deletion
        },
        error: (error) => {
          console.error('Error deleting category:', error);
        }
      });
    }
  }

  addTodo(todoData: Partial<Todo>): void {
    this.todoService.createTodo(todoData as Todo).subscribe(todo => {
      this.todos.push(todo);
    });
  }

  updateTodo(todo: Todo): void {
    this.todoService.updateTodo(todo.id!, todo).subscribe(() => {
      this.loadTodos(); // Reload to ensure consistency
    });
  }

  deleteTodo(id: number): void {
    this.todoService.deleteTodo(id).subscribe(() => {
      this.todos = this.todos.filter(todo => todo.id !== id);
    });
  }

  getTodoCountForCategory(categoryId: number): number {
    return this.todos.filter(todo => todo.category?.id === categoryId).length;
  }

  getCategoryName(categoryId: number | null): string {
    if (categoryId === null) return 'All';
    if (categoryId === 0) return 'Uncategorized';
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }
}
