import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { User, LoginResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly API_URL = '/api/auth';
    private readonly TOKEN_KEY = 'atlas_token';
    private readonly USER_KEY = 'atlas_user';

    currentUser = signal<User | null>(this.getStoredUser());

    constructor(private http: HttpClient, private router: Router) { }

    login(username: string, password: string): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.API_URL}/login`, { username, password })
            .pipe(tap(response => {
                localStorage.setItem(this.TOKEN_KEY, response.token);
                const user: User = {
                    username: response.username,
                    email: response.email,
                    role: response.role as User['role'],
                    managerLevel: response.managerLevel,
                    employeeId: response.employeeId
                };
                localStorage.setItem(this.USER_KEY, JSON.stringify(user));
                this.currentUser.set(user);
            }));
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    private getStoredUser(): User | null {
        const stored = localStorage.getItem(this.USER_KEY);
        return stored ? JSON.parse(stored) : null;
    }
}
