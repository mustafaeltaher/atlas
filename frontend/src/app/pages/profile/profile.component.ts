import { Component, OnInit, signal, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ThemeService } from '../../services/theme.service';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { Employee } from '../../models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="layout">
      <app-sidebar></app-sidebar>
      <div class="main-area">
        <app-header></app-header>
        <main class="main-content">
    <div class="profile-container">
      <!-- Profile Header -->
      <div class="profile-header card fade-in">
        <div class="profile-avatar-large">{{ getInitials() }}</div>
        <div class="profile-info">
          <h1 class="profile-name">{{ profile()?.name || authService.currentUser()?.username }}</h1>
          <p class="profile-title">{{ profile()?.title || 'N/A' }}</p>
          <span class="profile-badge">{{ authService.currentUser()?.isTopLevel ? 'Top Level' : 'Manager' }}</span>
        </div>
      </div>

      <div class="profile-grid">
        <!-- Personal Information -->
        <div class="card fade-in">
          <h3 class="section-title">Personal Information</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Email</span>
              <span class="info-value">{{ profile()?.email || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Grade</span>
              <span class="info-value">{{ profile()?.grade || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Location</span>
              <span class="info-value">{{ profile()?.location || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Hire Date</span>
              <span class="info-value">{{ profile()?.hireDate || 'N/A' }}</span>
            </div>
          </div>
        </div>

        <!-- Organization -->
        <div class="card fade-in">
          <h3 class="section-title">Organization</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Parent Tower</span>
              <span class="info-value">{{ profile()?.parentTowerName || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Tower</span>
              <span class="info-value">{{ profile()?.towerName || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Manager</span>
              <span class="info-value">{{ profile()?.managerName || 'N/A' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Allocation</span>
              <span class="info-value allocation">{{ profile()?.totalAllocation || 0 }}%</span>
            </div>
          </div>
        </div>

        <!-- Theme Preference -->
        <div class="card fade-in">
          <h3 class="section-title">Preferences</h3>
          <div class="preference-item">
            <div class="preference-info">
              <span class="preference-label">Theme</span>
              <span class="preference-desc">Choose your preferred color scheme</span>
            </div>
            <div class="theme-toggle">
              <button 
                class="theme-btn" 
                [class.active]="!themeService.isDarkMode()" 
                (click)="themeService.setDarkMode(false)">
                ☀️ Light
              </button>
              <button 
                class="theme-btn" 
                [class.active]="themeService.isDarkMode()" 
                (click)="themeService.setDarkMode(true)">
                🌙 Dark
              </button>
            </div>
          </div>
        </div>

        <!-- Change Password -->
        <div class="card fade-in">
          <h3 class="section-title">Security</h3>
          <form (ngSubmit)="changePassword()" class="password-form">
            <div class="form-group">
              <label>Current Password</label>
              <input 
                type="password" 
                [(ngModel)]="currentPassword" 
                name="currentPassword"
                class="form-input"
                placeholder="Enter current password">
            </div>
            <div class="form-group">
              <label>New Password</label>
              <input 
                type="password" 
                [(ngModel)]="newPassword" 
                name="newPassword"
                class="form-input"
                placeholder="Enter new password">
            </div>
            <div class="form-group">
              <label>Confirm New Password</label>
              <input 
                type="password" 
                [(ngModel)]="confirmPassword" 
                name="confirmPassword"
                class="form-input"
                placeholder="Confirm new password">
            </div>
            @if (passwordError()) {
              <div class="error-message">{{ passwordError() }}</div>
            }
            @if (passwordSuccess()) {
              <div class="success-message">{{ passwordSuccess() }}</div>
            }
            <button type="submit" class="btn-primary" [disabled]="isChangingPassword()">
              {{ isChangingPassword() ? 'Changing...' : 'Change Password' }}
            </button>
          </form>
        </div>
      </div>
    </div>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .profile-container {
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .profile-header {
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 32px;
      margin-bottom: 24px;
    }

    .profile-avatar-large {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary), var(--secondary));
      color: white;
      font-size: 36px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .profile-info {
      flex: 1;
    }

    .profile-name {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 4px 0;
    }

    .profile-title {
      font-size: 1rem;
      color: var(--text-secondary);
      margin: 0 0 12px 0;
    }

    .profile-badge {
      display: inline-block;
      padding: 4px 12px;
      background: var(--primary);
      color: white;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      text-transform: uppercase;
    }

    .profile-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 24px;
    }

    .section-title {
      font-size: 1rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 16px 0;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--border-color);
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-label {
      font-size: 12px;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .info-value {
      font-size: 14px;
      color: var(--text-primary);
      font-weight: 500;
    }

    .info-value.allocation {
      color: var(--success);
      font-weight: 600;
    }

    .preference-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .preference-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .preference-label {
      font-weight: 500;
      color: var(--text-primary);
    }

    .preference-desc {
      font-size: 12px;
      color: var(--text-secondary);
    }

    .theme-toggle {
      display: flex;
      gap: 8px;
    }

    .theme-btn {
      padding: 8px 16px;
      border: 1px solid var(--border-color);
      background: var(--bg-secondary);
      color: var(--text-secondary);
      border-radius: var(--border-radius);
      cursor: pointer;
      transition: all 0.2s;
    }

    .theme-btn.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
    }

    .password-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .form-group label {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
    }

    .form-input {
      padding: 10px 14px;
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      background: var(--bg-secondary);
      color: var(--text-primary);
      font-size: 14px;
    }

    .form-input:focus {
      outline: none;
      border-color: var(--primary);
    }

    .btn-primary {
      padding: 12px 24px;
      background: var(--primary);
      color: white;
      border: none;
      border-radius: var(--border-radius);
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-primary:hover:not(:disabled) {
      opacity: 0.9;
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .error-message {
      color: var(--danger);
      font-size: 13px;
      padding: 8px 12px;
      background: rgba(239, 68, 68, 0.1);
      border-radius: var(--border-radius);
    }

    .success-message {
      color: var(--success);
      font-size: 13px;
      padding: 8px 12px;
      background: rgba(16, 185, 129, 0.1);
      border-radius: var(--border-radius);
    }
  `]
})
export class ProfileComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  profile = signal<Employee | null>(null);
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordError = signal<string>('');
  passwordSuccess = signal<string>('');
  isChangingPassword = signal<boolean>(false);

  constructor(
    public themeService: ThemeService,
    public authService: AuthService,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    const employeeId = this.authService.currentUser()?.employeeId;
    if (employeeId) {
      this.apiService.get<Employee>(`/employees/${employeeId}`).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (data) => this.profile.set(data),
        error: (err) => console.error('Failed to load profile', err)
      });
    }
  }

  getInitials(): string {
    const name = this.profile()?.name || this.authService.currentUser()?.username || '';
    if (name.includes(' ')) {
      return name.split(' ').map(n => n[0]?.toUpperCase() || '').join('');
    }
    return name.split('.').map(n => n[0]?.toUpperCase() || '').join('');
  }

  changePassword(): void {
    this.passwordError.set('');
    this.passwordSuccess.set('');

    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.passwordError.set('All fields are required');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.passwordError.set('New passwords do not match');
      return;
    }

    if (this.newPassword.length < 6) {
      this.passwordError.set('New password must be at least 6 characters');
      return;
    }

    this.isChangingPassword.set(true);

    this.apiService.post('/auth/change-password', {
      currentPassword: this.currentPassword,
      newPassword: this.newPassword
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.passwordSuccess.set('Password changed successfully');
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.isChangingPassword.set(false);
      },
      error: (err) => {
        this.passwordError.set(err.error?.message || 'Failed to change password');
        this.isChangingPassword.set(false);
      }
    });
  }
}
