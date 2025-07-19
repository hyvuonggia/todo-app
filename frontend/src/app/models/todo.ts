export interface Category {
  id?: number;
  name: string;
  color?: string;
  description?: string;
  createdAt?: string;
  lastModified?: string;
}

export interface Todo {
  id?: number;
  title: string;
  description?: string;
  completed: boolean;
  category?: Category;
  createdAt?: string;
  lastModified?: string;
}
