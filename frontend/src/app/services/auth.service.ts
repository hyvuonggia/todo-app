import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface UserInfo {
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // Adjust if your backend runs on a different port/path
  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());
  private currentUser = new BehaviorSubject<UserInfo | null>(this.getUserFromStorage());

  constructor(private http: HttpClient) { }

  private hasToken(): boolean {
    return !!localStorage.getItem('jwt_token');
  }

  private getUserFromStorage(): UserInfo | null {
    const userStr = localStorage.getItem('user_info');
    return userStr ? JSON.parse(userStr) : null;
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        console.log('Login response:', response); // Log the full response
        if (response.token) {
          localStorage.setItem('jwt_token', response.token);

          // Store user information
          const userInfo: UserInfo = {
            username: response.username,
            email: response.email
          };
          localStorage.setItem('user_info', JSON.stringify(userInfo));

          this.loggedIn.next(true);
          this.currentUser.next(userInfo);
        }
      })
    );
  }

  register(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, credentials);
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_info');
    this.loggedIn.next(false);
    this.currentUser.next(null);
  }

  isLoggedIn(): Observable<boolean> {
    return this.loggedIn.asObservable();
  }

  getCurrentUser(): Observable<UserInfo | null> {
    return this.currentUser.asObservable();
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }
}
