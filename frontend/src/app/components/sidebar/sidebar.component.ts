import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar" [class.collapsed]="isCollapsed()">
      <!-- Toggle Button -->
      <button class="sidebar-toggle" (click)="toggleSidebar()" [title]="isCollapsed() ? 'Expand sidebar' : 'Collapse sidebar'">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
          @if (isCollapsed()) {
            <path d="M9 18l6-6-6-6"></path>
          } @else {
            <path d="M15 18l-6-6 6-6"></path>
          }
        </svg>
      </button>

      <div class="sidebar-header">
        <img src="assets/atlas-logo.png" alt="Atlas Logo" class="sidebar-logo">
        @if (!isCollapsed()) {
          <h1 class="logo">Atlas</h1>
        }
      </div>

      <nav class="sidebar-nav">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item" [title]="isCollapsed() ? 'Dashboard' : ''">
          <span class="nav-icon-emoji">📊</span>
          @if (!isCollapsed()) {
            <span class="nav-label">Dashboard</span>
          }
        </a>

        <a routerLink="/employees" routerLinkActive="active" class="nav-item" [title]="isCollapsed() ? 'Employees' : ''">
          <span class="nav-icon-emoji">👤</span>
          @if (!isCollapsed()) {
            <span class="nav-label">Employees</span>
          }
        </a>

        <a routerLink="/projects" routerLinkActive="active" class="nav-item" [title]="isCollapsed() ? 'Projects' : ''">
          <span class="nav-icon-emoji">📋</span>
          @if (!isCollapsed()) {
            <span class="nav-label">Projects</span>
          }
        </a>

        <a routerLink="/allocations" routerLinkActive="active" class="nav-item" [title]="isCollapsed() ? 'Allocations' : ''">
          <span class="nav-icon-emoji">🎯</span>
          @if (!isCollapsed()) {
            <span class="nav-label">Allocations</span>
          }
        </a>

        <a routerLink="/reports" routerLinkActive="active" class="nav-item" [title]="isCollapsed() ? 'Reports' : ''">
          <span class="nav-icon-emoji">📈</span>
          @if (!isCollapsed()) {
            <span class="nav-label">Reports</span>
          }
        </a>
      </nav>

      <div class="sidebar-footer">
        <a routerLink="/profile" routerLinkActive="active" class="user-profile-widget" [title]="isCollapsed() ? 'My Profile' : ''">
          <div class="user-avatar">{{ getInitials() }}</div>
          @if (!isCollapsed()) {
            <div class="user-details">
              <span class="user-name">{{ authService.currentUser()?.employeeName || authService.currentUser()?.username }}</span>
              <span class="user-role">{{ authService.currentUser()?.isTopLevel ? 'Top Level' : 'Manager' }}</span>
            </div>
          }
        </a>
        <button class="logout-btn" (click)="logout()" [title]="isCollapsed() ? 'Logout' : 'Logout'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </button>
      </div>
    </aside>
  `,
  styles: [`
    :host {
      display: block;
      flex-shrink: 0;
      height: 100vh;
      position: relative;
      z-index: 100;
    }

    .sidebar {
      width: 260px;
      min-width: 260px;
      height: 100%;
      background: var(--bg-sidebar);
      display: flex;
      flex-direction: column;
      border-right: 1px solid rgba(255,255,255,0.1);
      position: relative;
      transition: width 0.3s ease, min-width 0.3s ease;
    }

    .sidebar.collapsed {
      width: 80px;
      min-width: 80px;
    }

    /* Toggle Button */
    .sidebar-toggle {
      position: absolute;
      top: 12px;
      right: -12px;
      width: 24px;
      height: 24px;
      background: var(--bg-sidebar);
      border: 1px solid rgba(255,255,255,0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: white;
      transition: all 0.2s;
      z-index: 10;
    }

    .sidebar-toggle:hover {
      background: var(--primary);
      border-color: var(--primary);
      transform: scale(1.1);
    }

    .sidebar-header {
      padding: 20px 24px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      transition: padding 0.3s ease;
    }

    .sidebar.collapsed .sidebar-header {
      padding: 20px 12px;
    }

    .sidebar-logo {
      width: 120px;
      height: 120px;
      object-fit: contain;
      border-radius: 8px;
      transition: all 0.3s ease;
    }

    .sidebar.collapsed .sidebar-logo {
      width: 48px;
      height: 48px;
    }

    .logo {
      font-size: 1.75rem;
      font-weight: 700;
      color: white;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.2s ease;
    }

    .sidebar.collapsed .logo {
      opacity: 0;
      height: 0;
      overflow: hidden;
    }

    .sidebar-nav {
      flex: 1;
      padding: 16px 12px;
      overflow-y: auto;
      overflow-x: hidden;
    }

    /* Custom scrollbar for sidebar nav */
    .sidebar-nav::-webkit-scrollbar {
      width: 6px;
    }

    .sidebar-nav::-webkit-scrollbar-track {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 3px;
    }

    .sidebar-nav::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }

    .sidebar-nav::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 255, 255, 0.3);
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      color: rgba(255,255,255,0.7);
      border-radius: var(--border-radius);
      margin-bottom: 4px;
      transition: all 0.2s;
      white-space: nowrap;
      position: relative;
    }

    .sidebar.collapsed .nav-item {
      justify-content: center;
      padding: 12px;
    }

    .nav-item:hover {
      background: rgba(255,255,255,0.1);
      color: white;
    }

    .nav-item.active {
      background: white;
      color: var(--primary);
    }

    .nav-icon-emoji {
      font-size: 1.25rem;
      flex-shrink: 0;
      width: 24px;
      text-align: center;
      display: inline-block;
    }

    .sidebar.collapsed .nav-icon-emoji {
      margin-right: 0;
    }

    .nav-label {
      font-weight: 500;
      opacity: 1;
      transition: opacity 0.2s ease;
    }

    .sidebar.collapsed .nav-label {
      opacity: 0;
      width: 0;
      overflow: hidden;
    }

    .sidebar-footer {
      padding: 16px;
      margin-top: auto;
      display: flex;
      align-items: center;
      gap: 8px;
      border-top: 1px solid rgba(255,255,255,0.1);
    }

    .sidebar.collapsed .sidebar-footer {
      flex-direction: column;
      gap: 12px;
    }

    .user-profile-widget {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px;
      border-radius: var(--border-radius);
      cursor: pointer;
      transition: all 0.2s;
      text-decoration: none;
      color: inherit;
      flex: 1;
    }

    .sidebar.collapsed .user-profile-widget {
      flex: unset;
      width: 100%;
      justify-content: center;
    }

    .user-profile-widget:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .user-profile-widget.active {
      background: white;
      color: var(--primary);
    }

    .user-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 14px;
      color: white;
      transition: transform 0.2s;
      flex-shrink: 0;
    }

    .user-profile-widget:hover .user-avatar {
      transform: scale(1.05);
    }

    .user-details {
      display: flex;
      flex-direction: column;
      flex: 1;
      opacity: 1;
      transition: opacity 0.2s ease;
    }

    .sidebar.collapsed .user-details {
      opacity: 0;
      width: 0;
      overflow: hidden;
    }

    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: white;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .user-profile-widget.active .user-name {
      color: var(--primary);
    }

    .user-role {
      font-size: 11px;
      color: rgba(255,255,255,0.5);
      text-transform: uppercase;
    }

    .user-profile-widget.active .user-role {
      color: rgba(59, 130, 246, 0.7);
    }

    .logout-btn {
      background: none;
      border: none;
      color: rgba(255,255,255,0.5);
      cursor: pointer;
      padding: 8px;
      border-radius: var(--border-radius);
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .logout-btn:hover {
      background: rgba(255,255,255,0.1);
      color: white;
    }

    /* Responsive: Auto-collapse on smaller screens */
    @media (max-width: 768px) {
      .sidebar {
        width: 80px;
      }

      .sidebar.collapsed {
        width: 80px;
      }
    }
  `]
})
export class SidebarComponent {
  isCollapsed = signal(this.getInitialCollapsedState());

  constructor(public authService: AuthService) { }

  private getInitialCollapsedState(): boolean {
    const saved = localStorage.getItem('sidebarCollapsed');
    return saved === 'true';
  }

  toggleSidebar(): void {
    this.isCollapsed.update(value => {
      const newValue = !value;
      localStorage.setItem('sidebarCollapsed', String(newValue));
      return newValue;
    });
  }

  getInitials(): string {
    const name = this.authService.currentUser()?.username || '';
    return name.split('.').map(n => n[0]?.toUpperCase() || '').join('');
  }

  logout(): void {
    this.authService.logout();
  }
}
