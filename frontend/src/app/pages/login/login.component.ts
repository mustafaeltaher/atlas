import { Component, signal, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <img src="assets/atlas-logo.png" alt="Atlas Logo" class="login-logo">
          <h1 class="logo">Atlas</h1>
          <p>GDC Management System</p>
        </div>
        
        <form (ngSubmit)="login()" class="login-form">
          <div class="form-group">
            <label class="form-label">Username</label>
            <input 
              type="text" 
              class="form-input" 
              [(ngModel)]="username" 
              name="username"
              placeholder="Enter username"
              required>
          </div>
          
          <div class="form-group">
            <label class="form-label">Password</label>
            <input 
              type="password" 
              class="form-input" 
              [(ngModel)]="password" 
              name="password"
              placeholder="Enter password"
              required>
          </div>
          
          @if (error()) {
            <div class="error-message">{{ error() }}</div>
          }
          
          <button type="submit" class="btn btn-primary btn-full" [disabled]="loading()">
            {{ loading() ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>
        
        <div class="login-footer">
          <p>Demo Credentials:</p>
          <p><strong>Admin:</strong> admin / admin123</p>
          <p><strong>Manager:</strong> ahmed.el-sayed / password123</p>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, var(--bg-dark) 0%, var(--primary-dark) 100%);
    }
    
    .login-card {
      width: 100%;
      max-width: 400px;
      padding: 40px;
      background: var(--bg-card);
      border-radius: 12px;
      border: 1px solid var(--border-color);
      box-shadow: var(--shadow-lg);
    }
    
    .login-header {
      text-align: center;
      margin-bottom: 32px;
    }

    .login-logo {
      width: 120px;
      height: 120px;
      object-fit: contain;
      margin-bottom: 16px;
      border-radius: 12px;
    }

    .logo {
      font-size: 2.5rem;
      font-weight: 700;
      color: var(--secondary);
      margin-bottom: 8px;
    }
    
    .login-header p {
      color: var(--text-muted);
    }
    
    .login-form {
      margin-bottom: 24px;
    }
    
    .btn-full {
      width: 100%;
      padding: 14px;
      font-size: 16px;
    }
    
    .error-message {
      padding: 12px;
      margin-bottom: 16px;
      background: rgba(231, 76, 60, 0.15);
      border: 1px solid var(--danger);
      border-radius: var(--border-radius);
      color: var(--danger);
      font-size: 14px;
    }
    
    .login-footer {
      text-align: center;
      padding-top: 20px;
      border-top: 1px solid var(--border-color);
      font-size: 12px;
      color: var(--text-muted);
    }
    
    .login-footer p {
      margin: 4px 0;
      color: var(--text-muted);
    }
  `]
})
export class LoginComponent {
    private destroyRef = inject(DestroyRef);

    username = '';
    password = '';
    loading = signal(false);
    error = signal('');

    constructor(private authService: AuthService, private router: Router) {
        if (this.authService.isAuthenticated()) {
            this.router.navigate(['/dashboard']);
        }
    }

    login(): void {
        if (!this.username || !this.password) return;

        this.loading.set(true);
        this.error.set('');

        this.authService.login(this.username, this.password).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                this.router.navigate(['/dashboard']);
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set('Invalid username or password');
            }
        });
    }
}
