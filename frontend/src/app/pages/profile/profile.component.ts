import { Component, OnInit, signal, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { ThemeService } from '../../services/theme.service';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { DelegateService } from '../../services/delegate.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { Employee, DelegateResponse } from '../../models';

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

        <!-- Managers I a Support (Impersonation available) -->
        @if (managersISupport().length > 0) {
          <div class="card fade-in">
            <h3 class="section-title">Managers I Support</h3>
            <p class="section-desc">You have been granted access to manage these accounts.</p>
            <div class="delegates-list">
              @for (manager of managersISupport(); track manager.id) {
                <div class="delegate-item">
                  <div class="delegate-info">
                    <span class="delegate-name">{{ manager.delegatorName || manager.delegatorUsername }}</span>
                    <span class="delegate-date">Granted on {{ manager.createdAt | date }}</span>
                  </div>
                  <span class="status-badge active">Active</span>
                </div>
              }
            </div>
          </div>
        }

        <!-- My Delegates (Granting Access) -->
        <div class="card fade-in">
          <h3 class="section-title">My Delegates</h3>
          <p class="section-desc">Grant other users permission to access your account.</p>
          
          <div class="delegate-form-container">
            <div class="delegate-input-group">
              <input 
                type="text" 
                [ngModel]="newDelegateUsername" 
                (input)="onSearch($event)"
                (focus)="onFocus()"
                (blur)="closeDropdown()"
                placeholder="Search by name or username..."
                class="form-input"
                autocomplete="off">
              
              @if (showDropdown() && potentialDelegates().length > 0) {
                <div class="dropdown-list">
                  @for (delegate of potentialDelegates(); track delegate.id) {
                    <button class="dropdown-option" (mousedown)="selectDelegate(delegate)">
                      <div class="option-name">{{ delegate.name }}</div>
                      <div class="option-details">{{ delegate.title }} • {{ delegate.jobLevel }}</div>
                    </button>
                  }
                </div>
              }
            </div>
            
            <button class="btn-primary btn-sm" (click)="grantAccess()" [disabled]="!newDelegateUsername">
              Grant Access
            </button>
          </div>

          <div class="delegates-list">
            @if (myDelegates().length === 0) {
              <div class="empty-state">No delegates assigned.</div>
            } @else {
              @for (delegate of myDelegates(); track delegate.id) {
                <div class="delegate-item">
                  <div class="delegate-info">
                    <span class="delegate-name">{{ delegate.delegateName || delegate.delegateUsername }}</span>
                    <span class="delegate-date">Granted on {{ delegate.createdAt | date }}</span>
                  </div>
                  <button class="btn-icon danger" (click)="revokeAccess(delegate.id)" title="Revoke Access">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                      <path d="M3 6h18"></path>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              }
            }
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
    
    .section-desc {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 16px;
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

    .btn-primary.btn-sm {
        padding: 8px 16px;
        font-size: 13px;
    }

    .btn-primary:hover:not(:disabled) {
      opacity: 0.9;
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    
    .btn-icon.danger {
        color: var(--danger);
        background: transparent;
        border: none;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
    }
    
    .btn-icon.danger:hover {
        background: rgba(239, 68, 68, 0.1);
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

    .delegate-form {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
    }
    
    .delegate-form .form-input {
        flex: 1;
    }

    .delegates-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .delegate-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: var(--bg-secondary);
      border-radius: var(--border-radius);
      border: 1px solid var(--border-color);
    }

    .delegate-info {
        display: flex;
        flex-direction: column;
        gap: 2px;
    }

    .delegate-name {
        font-weight: 500;
        color: var(--text-primary);
    }

    .delegate-date {
        font-size: 11px;
        color: var(--text-secondary);
    }

    .empty-state {
        color: var(--text-secondary);
        font-size: 13px;
        padding: 12px;
        text-align: center;
        background: var(--bg-secondary);
        border-radius: var(--border-radius);
        border: 1px dashed var(--border-color);
    }

    .delegate-form-container {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
      position: relative;
    }

    .delegate-input-group {
      flex: 1;
      position: relative;
    }

    .delegate-input-group .form-input {
      width: 100%;
    }

    .dropdown-list {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      z-index: 1000;
      max-height: 200px;
      overflow-y: auto;
      margin-top: 4px;
    }

    .dropdown-option {
      width: 100%;
      text-align: left;
      padding: 8px 12px;
      background: none;
      border: none;
      cursor: pointer;
      border-bottom: 1px solid var(--border-color);
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .dropdown-option:last-child {
      border-bottom: none;
    }

    .dropdown-option:hover {
      background: var(--bg-secondary);
    }

    .option-name {
      font-weight: 500;
      color: var(--text-primary);
      font-size: 14px;
    }

    .option-details {
      font-size: 11px;
      color: var(--text-secondary);
    }
    
    .status-badge {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 12px;
      font-weight: 600;
    }
    
    .status-badge.active {
      background: rgba(16, 185, 129, 0.1);
      color: var(--success);
    }
  `]
})
export class ProfileComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  profile = signal<Employee | null>(null);
  myDelegates = signal<DelegateResponse[]>([]);
  managersISupport = signal<DelegateResponse[]>([]);

  // Search & Dropdown
  newDelegateUsername = '';
  potentialDelegates = signal<any[]>([]);
  showDropdown = signal<boolean>(false);
  private searchSubject = new Subject<string>();

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordError = signal<string>('');
  passwordSuccess = signal<string>('');
  isChangingPassword = signal<boolean>(false);

  constructor(
    public themeService: ThemeService,
    public authService: AuthService,
    private apiService: ApiService,
    private delegateService: DelegateService
  ) { }

  ngOnInit(): void {
    this.loadProfile();
    this.loadDelegates();
    this.loadManagersISupport();
    this.setupSearch();
  }

  setupSearch(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      // distinctUntilChanged(), // Removed to allow re-triggering on focus with same term
      switchMap(term => {
        return this.delegateService.getPotentialDelegates(term).pipe(
          catchError(() => of([]))
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(results => {
      this.potentialDelegates.set(results);
      this.showDropdown.set(results.length > 0);
    });
  }

  loadManagersISupport(): void {
    this.delegateService.getAvailableAccounts().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.managersISupport.set(data),
      error: (err: any) => console.error('Failed to load managers I support', err)
    });
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.newDelegateUsername = input.value;
    this.searchSubject.next(this.newDelegateUsername);
  }

  onFocus(): void {
    this.searchSubject.next(this.newDelegateUsername);
  }

  selectDelegate(delegate: any): void {
    this.newDelegateUsername = delegate.username;
    this.showDropdown.set(false);
  }

  closeDropdown(): void {
    // Delay to allow click event to register
    setTimeout(() => this.showDropdown.set(false), 200);
  }

  loadProfile(): void {
    const employeeId = this.authService.currentUser()?.employeeId;
    if (employeeId) {
      this.apiService.get<Employee>(`/employees/${employeeId}`).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (data) => this.profile.set(data),
        error: (err: any) => console.error('Failed to load profile', err)
      });
    }
  }

  loadDelegates(): void {
    this.delegateService.getMyDelegates().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.myDelegates.set(data),
      error: (err: any) => console.error('Failed to load delegates', err)
    });
  }

  grantAccess(): void {
    if (!this.newDelegateUsername) return;

    this.delegateService.grantAccess({ delegateUsername: this.newDelegateUsername }).subscribe({
      next: (delegate) => {
        this.myDelegates.update(list => [...list, delegate]);
        this.newDelegateUsername = '';
        alert('Access granted successfully');
      },
      error: (err: any) => alert(err.error?.message || 'Failed to grant access')
    });
  }

  revokeAccess(id: number): void {
    if (!confirm('Are you sure you want to revoke access?')) return;

    this.delegateService.revokeAccess(id).subscribe({
      next: () => {
        this.myDelegates.update(list => list.filter(d => d.id !== id));
      },
      error: (err: any) => alert('Failed to revoke access')
    });
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
      error: (err: any) => {
        this.passwordError.set(err.error?.message || 'Failed to change password');
        this.isChangingPassword.set(false);
      }
    });
  }
}
