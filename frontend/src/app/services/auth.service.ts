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
    private readonly ORIGINAL_TOKEN_KEY = 'atlas_original_token';

    currentUser = signal<User | null>(this.getStoredUser());

    constructor(private http: HttpClient, private router: Router) { }

    login(username: string, password: string): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.API_URL}/login`, { username, password })
            .pipe(tap(response => this.setSession(response)));
    }

    setSession(response: LoginResponse): void {
        if (response.token) {
            localStorage.setItem(this.TOKEN_KEY, response.token);
        }
        const user: User = {
            username: response.username,
            email: response.email,
            isTopLevel: response.isTopLevel,
            employeeName: response.employeeName,
            employeeId: response.employeeId,
            isImpersonating: response.isImpersonating,
            impersonatorUsername: response.impersonatorUsername
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
    }

    startImpersonation(response: LoginResponse): void {
        const currentToken = this.getToken();
        if (currentToken) {
            localStorage.setItem(this.ORIGINAL_TOKEN_KEY, currentToken);
        }
        this.setSession(response);
    }

    stopImpersonation(): void {
        const originalToken = localStorage.getItem(this.ORIGINAL_TOKEN_KEY);
        if (originalToken) {
            localStorage.setItem(this.TOKEN_KEY, originalToken);
            localStorage.removeItem(this.ORIGINAL_TOKEN_KEY);

            // Refresh user details with the original token
            this.http.get<LoginResponse>(`${this.API_URL}/me`).subscribe({
                next: (userResponse) => {
                    this.setSession(userResponse);
                    window.location.reload();
                },
                error: () => this.logout()
            });
        } else {
            this.logout();
        }
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        localStorage.removeItem(this.ORIGINAL_TOKEN_KEY);
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
        if (!stored) return null;
        try {
            return JSON.parse(stored);
        } catch {
            localStorage.removeItem(this.USER_KEY);
            return null;
        }
    }
}
