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
  allTodos: Todo[] = []; // Store all todos for filtering
  categories: Category[] = [];
  selectedCategoryId: number | null = null;
  searchQuery: string = '';

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
    // Always load all todos first
    this.todoService.getTodos().subscribe(todos => {
      this.allTodos = todos;
      this.applyFilters();
    });
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe(categories => {
      this.categories = categories;
    });
  }

  onCategoryFilterChange(): void {
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    let filteredTodos = [...this.allTodos];

    // Apply category filter
    if (this.selectedCategoryId === 0) {
      // Filter for uncategorized todos (category is null)
      filteredTodos = filteredTodos.filter(todo => !todo.category);
    } else if (this.selectedCategoryId !== null) {
      filteredTodos = filteredTodos.filter(todo => todo.category?.id === this.selectedCategoryId);
    }

    // Apply search filter
    if (this.searchQuery && this.searchQuery.trim()) {
      const query = this.searchQuery.trim().toLowerCase();
      filteredTodos = filteredTodos.filter(todo => {
        // Search in title
        const titleMatch = todo.title.toLowerCase().includes(query);
        
        // Search in description (remove HTML tags for search)
        const descriptionMatch = todo.description ? 
          this.stripHtmlTags(todo.description).toLowerCase().includes(query) : false;
        
        // Search in category name
        const categoryMatch = todo.category ? 
          todo.category.name.toLowerCase().includes(query) : false;

        return titleMatch || descriptionMatch || categoryMatch;
      });
    }

    this.todos = filteredTodos;
  }

  private stripHtmlTags(html: string): string {
    const tmp = document.createElement('div');
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || '';
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
      this.allTodos.push(todo);
      this.applyFilters();
    });
  }

  updateTodo(todo: Todo): void {
    this.todoService.updateTodo(todo.id!, todo).subscribe(() => {
      this.loadTodos(); // Reload to ensure consistency
    });
  }

  deleteTodo(id: number): void {
    this.todoService.deleteTodo(id).subscribe(() => {
      this.allTodos = this.allTodos.filter(todo => todo.id !== id);
      this.applyFilters();
    });
  }

  getTodoCountForCategory(categoryId: number): number {
    return this.allTodos.filter(todo => todo.category?.id === categoryId).length;
  }

  getCategoryName(categoryId: number | null): string {
    if (categoryId === null) return 'All';
    if (categoryId === 0) return 'Uncategorized';
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }

  getSectionTitle(): string {
    const categoryTitle = this.selectedCategoryId === null ? 'All Todos' : 
                         this.selectedCategoryId === 0 ? 'Uncategorized Todos' : 
                         this.getCategoryName(this.selectedCategoryId) + ' Todos';
    
    const totalCount = this.todos.length;
    const totalAllTodos = this.allTodos.length;
    
    if (this.searchQuery && this.searchQuery.trim() && totalCount !== totalAllTodos) {
      return `${categoryTitle} (${totalCount} of ${totalAllTodos})`;
    }
    
    return categoryTitle;
  }

  getEmptyStateIcon(): string {
    if (this.searchQuery && this.searchQuery.trim()) {
      return 'search_off';
    }
    return 'assignment';
  }

  getEmptyStateMessage(): string {
    if (this.searchQuery && this.searchQuery.trim()) {
      return `No todos found matching "${this.searchQuery}". Try a different search term.`;
    }
    if (this.selectedCategoryId !== null) {
      const categoryName = this.getCategoryName(this.selectedCategoryId);
      return `No todos in ${categoryName} category. Click "Add Todo" to create one!`;
    }
    return 'No todos yet. Click "Add Todo" to create your first todo!';
  }
}
