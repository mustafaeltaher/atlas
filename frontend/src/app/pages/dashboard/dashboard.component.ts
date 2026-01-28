import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { DashboardStats } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="layout">
      <app-sidebar></app-sidebar>
      
      <div class="main-area">
        <app-header></app-header>
        <main class="main-content">
        
        @if (loading()) {
          <div class="loading">Loading...</div>
        } @else {
          <div class="grid grid-4 fade-in">
            <div class="card metric-card">
              <div class="card-header">
                <span class="card-title">Total Employees</span>
                <div class="card-icon employees">
                  <span>👤</span>
                </div>
              </div>
              <div class="card-value">{{ stats()?.activeEmployees || 0 }}</div>
              <div class="card-trend positive">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline>
                  <polyline points="17 6 23 6 23 12"></polyline>
                </svg>
                +{{ stats()?.employeeTrend || 0 }}% this month
              </div>
            </div>
            
            <div class="card metric-card">
              <div class="card-header">
                <span class="card-title">Utilization Rate</span>
                <div class="card-icon utilization">
                  <span>📊</span>
                </div>
              </div>
              <div class="card-value">{{ stats()?.averageUtilization || 0 }}%</div>
              <div class="card-trend" [class.positive]="(stats()?.utilizationTrend || 0) > 0" 
                   [class.negative]="(stats()?.utilizationTrend || 0) < 0">
                {{ (stats()?.utilizationTrend || 0) > 0 ? '+' : '' }}{{ stats()?.utilizationTrend || 0 }}% this month
              </div>
            </div>
            
            <div class="card metric-card">
              <div class="card-header">
                <span class="card-title">Bench Count</span>
                <div class="card-icon bench">
                  <span>⏸️</span>
                </div>
              </div>
              <div class="card-value">{{ stats()?.benchCount || 0 }}</div>
              <div class="card-trend" [class.positive]="(stats()?.benchTrend || 0) < 0" 
                   [class.negative]="(stats()?.benchTrend || 0) > 0">
                {{ (stats()?.benchTrend || 0) > 0 ? '+' : '' }}{{ stats()?.benchTrend || 0 }}% this month
              </div>
            </div>
            
            <div class="card metric-card">
              <div class="card-header">
                <span class="card-title">Active Projects</span>
                <div class="card-icon projects">
                  <span>📋</span>
                </div>
              </div>
              <div class="card-value">{{ stats()?.activeProjects || 0 }}</div>
              <div class="card-trend positive">
                +{{ stats()?.projectTrend || 0 }}% this month
              </div>
            </div>
          </div>
          
          <div class="grid grid-2" style="margin-top: 24px;">
            <div class="card">
              <h3 style="margin-bottom: 16px;">Utilization Overview</h3>
              <div class="stat-row">
                <span>Active Employees</span>
                <span class="stat-value">{{ stats()?.activeEmployees }}</span>
              </div>
              <div class="stat-row">
                <span>On Bench</span>
                <span class="stat-value bench-color">{{ stats()?.benchCount }}</span>
              </div>
              <div class="stat-row">
                <span>Prospects</span>
                <span class="stat-value prospect-color">{{ stats()?.prospectCount }}</span>
              </div>
            </div>
            
            <div class="card">
              <h3 style="margin-bottom: 16px;">Quick Stats</h3>
              <div class="stat-row">
                <span>Total in System</span>
                <span class="stat-value">{{ stats()?.totalEmployees }}</span>
              </div>
              <div class="stat-row">
                <span>Average Utilization</span>
                <span class="stat-value">{{ stats()?.averageUtilization }}%</span>
              </div>
              <div class="stat-row">
                <span>Active Projects</span>
                <span class="stat-value">{{ stats()?.activeProjects }}</span>
              </div>
            </div>
          </div>
        }
      </main>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 24px;
    }
    
    .page-header h1 {
      margin-bottom: 4px;
    }
    
    .page-header p {
      color: var(--text-muted);
    }
    
    .loading {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
    }
    
    .metric-card {
      position: relative;
    }
    
    .card-icon {
      font-size: 1.5rem;
      line-height: 1;
    }
    
    .card-icon.employees { color: var(--accent); }
    .card-icon.utilization { color: var(--secondary); }
    .card-icon.bench { color: var(--warning); }
    .card-icon.projects { color: #9B59B6; }
    
    .stat-row {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid var(--border-color);
    }
    
    .stat-row:last-child {
      border-bottom: none;
    }
    
    .stat-value {
      font-weight: 600;
      color: var(--text-primary);
    }
    
    .bench-color { color: var(--warning); }
    .prospect-color { color: var(--secondary); }
  `]
})
export class DashboardComponent implements OnInit {
  stats = signal<DashboardStats | null>(null);
  loading = signal(true);

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.apiService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
