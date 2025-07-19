import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Category } from '../models/todo';

@Component({
  selector: 'app-category-dialog',
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
  templateUrl: './category-dialog.component.html',
  styleUrls: ['./category-dialog.component.scss']
})
export class CategoryDialogComponent {
  name: string = '';
  color: string = '#2196F3';
  description: string = '';
  isEdit: boolean = false;

  predefinedColors = [
    '#F44336', '#E91E63', '#9C27B0', '#673AB7',
    '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
    '#009688', '#4CAF50', '#8BC34A', '#CDDC39',
    '#FFEB3B', '#FFC107', '#FF9800', '#FF5722'
  ];

  constructor(
    public dialogRef: MatDialogRef<CategoryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { category?: Category }
  ) {
    if (data?.category) {
      this.isEdit = true;
      this.name = data.category.name;
      this.color = data.category.color || '#2196F3';
      this.description = data.category.description || '';
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.name.trim()) {
      const categoryData: Partial<Category> = {
        name: this.name.trim(),
        color: this.color,
        description: this.description.trim() || undefined
      };
      
      if (this.isEdit && this.data.category) {
        categoryData.id = this.data.category.id;
      }
      
      this.dialogRef.close(categoryData);
    }
  }

  selectColor(color: string): void {
    this.color = color;
  }
}
