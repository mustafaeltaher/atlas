import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">Atlas</h1>
      </div>
      
      <nav class="sidebar-nav">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
          <span class="nav-icon-emoji">📊</span>
          <span>Dashboard</span>
        </a>
        
        <a routerLink="/employees" routerLinkActive="active" class="nav-item">
          <span class="nav-icon-emoji">👤</span>
          <span>Employees</span>
        </a>
        
        <a routerLink="/projects" routerLinkActive="active" class="nav-item">
          <span class="nav-icon-emoji">📋</span>
          <span>Projects</span>
        </a>
        
        <a routerLink="/allocations" routerLinkActive="active" class="nav-item">
          <span class="nav-icon-emoji">🎯</span>
          <span>Allocations</span>
        </a>
        
        <a routerLink="/reports" routerLinkActive="active" class="nav-item">
          <span class="nav-icon-emoji">📈</span>
          <span>Reports</span>
        </a>
      </nav>
      
      <div class="sidebar-footer">
        <a routerLink="/profile" routerLinkActive="active" class="user-profile-widget" title="My Profile">
          <div class="user-avatar">{{ getInitials() }}</div>
          <div class="user-details">
            <span class="user-name">{{ authService.currentUser()?.username }}</span>
            <span class="user-role">{{ authService.currentUser()?.role }}</span>
          </div>
        </a>
        <button class="logout-btn" (click)="logout()" title="Logout">
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
    .sidebar {
      width: 260px;
      height: 100vh;
      background: var(--bg-sidebar);
      display: flex;
      flex-direction: column;
      border-right: 1px solid rgba(255,255,255,0.1);
    }
    
    .sidebar-header {
      padding: 24px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    
    .logo {
      font-size: 1.75rem;
      font-weight: 700;
      color: white;
    }
    
    .sidebar-nav {
      flex: 1;
      padding: 16px 12px;
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
      margin-right: 12px;
    }
    
    .nav-item span:last-child {
      font-weight: 500;
    }
    
    .sidebar-footer {
      padding: 16px;
      margin-top: auto;
      display: flex;
      align-items: center;
      gap: 8px;
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
    }

    .user-profile-widget:hover .user-avatar {
      transform: scale(1.05);
    }
    
    .user-details {
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: white;
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
    }
    
    .logout-btn:hover {
      background: rgba(255,255,255,0.1);
      color: white;
    }
  `]
})
export class SidebarComponent {
  constructor(public authService: AuthService) { }

  getInitials(): string {
    const name = this.authService.currentUser()?.username || '';
    return name.split('.').map(n => n[0]?.toUpperCase() || '').join('');
  }

  logout(): void {
    this.authService.logout();
  }
}
