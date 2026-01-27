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
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7"></rect>
            <rect x="14" y="3" width="7" height="7"></rect>
            <rect x="14" y="14" width="7" height="7"></rect>
            <rect x="3" y="14" width="7" height="7"></rect>
          </svg>
          <span>Dashboard</span>
        </a>
        
        <a routerLink="/employees" routerLinkActive="active" class="nav-item">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          <span>Employees</span>
        </a>
        
        <a routerLink="/projects" routerLinkActive="active" class="nav-item">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
          </svg>
          <span>Projects</span>
        </a>
        
        <a routerLink="/allocations" routerLinkActive="active" class="nav-item">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <span>Allocations</span>
        </a>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">{{ getInitials() }}</div>
          <div class="user-details">
            <span class="user-name">{{ authService.currentUser()?.username }}</span>
            <span class="user-role">{{ authService.currentUser()?.role }}</span>
          </div>
        </div>
        <button class="logout-btn" (click)="logout()">
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
    
    .nav-icon {
      width: 20px;
      height: 20px;
    }
    
    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid rgba(255,255,255,0.1);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
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
    }
    
    .user-details {
      display: flex;
      flex-direction: column;
    }
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: white;
    }
    
    .user-role {
      font-size: 11px;
      color: rgba(255,255,255,0.5);
      text-transform: uppercase;
    }
    
    .logout-btn {
      background: none;
      border: none;
      color: rgba(255,255,255,0.5);
      cursor: pointer;
      padding: 8px;
      border-radius: var(--border-radius);
      transition: all 0.2s;
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
