import { Component, OnInit, signal, HostListener, ElementRef, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../services/theme.service';
import { AuthService } from '../../services/auth.service';
import { DelegateService } from '../../services/delegate.service';
import { DelegateResponse } from '../../models';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <!-- Impersonation Banner -->
    @if (authService.currentUser()?.isImpersonating) {
        <div class="impersonation-banner">
            <span>Acting as <strong>{{ authService.currentUser()?.username }}</strong> ({{ authService.currentUser()?.employeeName }})</span>
            <button class="btn-xs" (click)="exitImpersonation()">Switch Back</button>
        </div>
    }

    <header class="header">
      <div class="header-left">
        <h2 class="page-title">{{ getPageTitle() }}</h2>
      </div>
      
      <div class="header-right">
        <!-- Switch Account Dropdown -->
        @if (availableAccounts().length > 0) {
          <div class="dropdown-container">
            <button class="header-btn" (click)="toggleAccountDropdown()" title="Switch Account">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="8.5" cy="7" r="4"></circle>
                <polyline points="17 11 19 13 23 9"></polyline>
              </svg>
            </button>
            
            @if (showAccountDropdown) {
              <div class="dropdown-menu fade-in">
                <div class="dropdown-header">Switch Account</div>
                @for (account of availableAccounts(); track account.id) {
                  <button class="dropdown-item" (click)="switchAccount(account.delegatorUsername)">
                    <div class="account-name">{{ account.delegatorName }}</div>
                    <div class="account-username">{{ account.delegatorUsername }}</div>
                  </button>
                }
              </div>
            }
          </div>
        }

        <!-- Theme Toggle -->
        <button class="header-btn" (click)="toggleTheme()" [title]="themeService.isDarkMode() ? 'Switch to Light Mode' : 'Switch to Dark Mode'">
          @if (themeService.isDarkMode()) {
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <circle cx="12" cy="12" r="5"></circle>
              <line x1="12" y1="1" x2="12" y2="3"></line>
              <line x1="12" y1="21" x2="12" y2="23"></line>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
              <line x1="1" y1="12" x2="3" y2="12"></line>
              <line x1="21" y1="12" x2="23" y2="12"></line>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
          } @else {
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
          }
        </button>

        <!-- Notifications Bell (Placeholder) -->
        <button class="header-btn notification-btn" title="Notifications (Coming Soon)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
          </svg>
          <span class="notification-badge">3</span>
        </button>

        <!-- Profile Avatar -->
        <a routerLink="/profile" class="profile-avatar" title="View Profile">
          {{ getInitials() }}
        </a>
      </div>
    </header>
  `,
  styles: [`
    .impersonation-banner {
      background: #f59e0b; /* Warning/Amber color */
      color: #1a1a1a;
      padding: 8px 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      font-size: 14px;
      font-weight: 500;
    }

    .btn-xs {
      background: rgba(0,0,0,0.1);
      border: none;
      padding: 4px 12px;
      border-radius: 12px;
      cursor: pointer;
      font-size: 12px;
      font-weight: 600;
      transition: background 0.2s;
    }

    .btn-xs:hover {
      background: rgba(0,0,0,0.2);
    }

    .header {
      height: 64px;
      background: var(--bg-card);
      border-bottom: 1px solid var(--border-color);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
    }

    .header-left {
      display: flex;
      align-items: center;
    }

    .page-title {
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .header-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: none;
      background: var(--bg-secondary);
      color: var(--text-secondary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
    }

    .header-btn:hover {
      background: var(--primary);
      color: white;
    }

    .dropdown-container {
      position: relative;
    }

    .dropdown-menu {
      position: absolute;
      top: 100%;
      right: 0;
      margin-top: 8px;
      width: 240px;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      z-index: 1000;
      padding: 4px 0;
    }

    .dropdown-header {
      padding: 8px 16px;
      font-size: 12px;
      color: var(--text-secondary);
      text-transform: uppercase;
      border-bottom: 1px solid var(--border-color);
      margin-bottom: 4px;
    }

    .dropdown-item {
      display: block;
      width: 100%;
      text-align: left;
      padding: 8px 16px;
      background: none;
      border: none;
      cursor: pointer;
      transition: background 0.2s;
    }

    .dropdown-item:hover {
      background: var(--bg-secondary);
    }
    
    .dropdown-item.empty {
        color: var(--text-secondary);
        font-size: 13px;
        cursor: default;
    }

    .account-name {
      font-weight: 500;
      color: var(--text-primary);
      font-size: 14px;
    }

    .account-username {
      font-size: 12px;
      color: var(--text-secondary);
    }

    .notification-btn {
      position: relative;
    }

    .notification-badge {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background: var(--danger);
      color: white;
      font-size: 10px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .profile-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary), var(--secondary));
      color: white;
      font-weight: 600;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: transform 0.2s;
    }

    .profile-avatar:hover {
      transform: scale(1.1);
    }
  `]
})
export class HeaderComponent implements OnInit {
  availableAccounts = signal<DelegateResponse[]>([]);
  showAccountDropdown = false;

  constructor(
    public themeService: ThemeService,
    public authService: AuthService,
    private delegateService: DelegateService,
    private eRef: ElementRef
  ) {
    effect(() => {
      const user = this.authService.currentUser();
      if (user && !user.isImpersonating) {
        this.loadAvailableAccounts();
      } else {
        this.availableAccounts.set([]); // Clear if impersonating to avoid confusion or if logged out
      }
    });
  }

  ngOnInit() {
    // Initial load handled by effect
  }

  loadAvailableAccounts() {
    this.delegateService.getAvailableAccounts().subscribe({
      next: (data) => this.availableAccounts.set(data),
      error: (err) => console.error('Failed to load available accounts', err)
    });
  }

  toggleAccountDropdown() {
    this.showAccountDropdown = !this.showAccountDropdown;
    if (this.showAccountDropdown) {
      this.loadAvailableAccounts();
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.showAccountDropdown = false;
    }
  }

  switchAccount(targetUsername: string) {
    this.delegateService.impersonate({ targetUsername }).subscribe({
      next: (response) => {
        this.authService.startImpersonation(response);
        window.location.reload();
      },
      error: (err) => alert('Failed to switch account: ' + (err.error?.message || 'Unknown error'))
    });
  }

  exitImpersonation() {
    this.authService.stopImpersonation();
  }

  getPageTitle(): string {
    const path = window.location.pathname;
    const titles: { [key: string]: string } = {
      '/dashboard': 'Dashboard',
      '/employees': 'Employees',
      '/projects': 'Projects',
      '/allocations': 'Allocations',
      '/profile': 'My Profile'
    };
    return titles[path] || 'Atlas';
  }

  getInitials(): string {
    const name = this.authService.currentUser()?.username || '';
    if (!name) return '';
    const parts = name.split(/[._ ]/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
