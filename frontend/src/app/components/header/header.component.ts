import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../services/theme.service';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-header',
    standalone: true,
    imports: [CommonModule, RouterModule],
    template: `
    <header class="header">
      <div class="header-left">
        <h2 class="page-title">{{ getPageTitle() }}</h2>
      </div>
      
      <div class="header-right">
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
export class HeaderComponent {
    constructor(
        public themeService: ThemeService,
        private authService: AuthService
    ) { }

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
        return name.split('.').map(n => n[0]?.toUpperCase() || '').join('');
    }

    toggleTheme(): void {
        this.themeService.toggleTheme();
    }
}
