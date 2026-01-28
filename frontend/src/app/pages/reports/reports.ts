import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="layout">
      <app-sidebar></app-sidebar>
      
      <div class="main-area">
        <app-header></app-header>
        <main class="main-content">
          <div class="page-header">
            <div>
              <h1>Reports & Analytics</h1>
              <p>Comprehensive reporting and analytics for resource management</p>
            </div>
          </div>
          
          <div class="grid grid-3 fade-in">
            <!-- Utilization Report -->
            <div class="card report-card" (click)="openReport('utilization')">
              <div class="card-icon-lg">📊</div>
              <h3 class="card-title">Utilization Report</h3>
              <p class="card-desc">Resource utilization by employee and project</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Employee Utilization</div>
                <div class="stat-item"><span class="dot secondary"></span>Project Coverage</div>
                <div class="stat-item"><span class="dot secondary"></span>Time Period Analysis</div>
              </div>
            </div>

            <!-- Bench Report -->
            <div class="card report-card" (click)="openReport('bench')">
              <div class="card-icon-lg">⏸️</div>
              <h3 class="card-title">Bench Report</h3>
              <p class="card-desc">Identify bench resources and availability</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Bench Employees</div>
                <div class="stat-item"><span class="dot secondary"></span>Cost Impact</div>
                <div class="stat-item"><span class="dot secondary"></span>Availability Timeline</div>
              </div>
            </div>

            <!-- Allocation Report -->
            <div class="card report-card" (click)="openReport('allocation')">
              <div class="card-icon-lg">🎯</div>
              <h3 class="card-title">Allocation Report</h3>
              <p class="card-desc">Allocation breakdown by tech tower and project</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Tech Tower Distribution</div>
                <div class="stat-item"><span class="dot secondary"></span>Project Allocation</div>
                <div class="stat-item"><span class="dot secondary"></span>Team Capacity</div>
              </div>
            </div>

            <!-- Project Status -->
            <div class="card report-card" (click)="openReport('project')">
              <div class="card-icon-lg">📋</div>
              <h3 class="card-title">Project Status</h3>
              <p class="card-desc">Project health, budget, and resource status</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Budget Utilization</div>
                <div class="stat-item"><span class="dot secondary"></span>Timeline Progress</div>
                <div class="stat-item"><span class="dot secondary"></span>Team Efficiency</div>
              </div>
            </div>

            <!-- Skills Analysis -->
            <div class="card report-card" (click)="openReport('skills')">
              <div class="card-icon-lg">💡</div>
              <h3 class="card-title">Skills Analysis</h3>
              <p class="card-desc">Employee skills inventory and gaps</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Skills Distribution</div>
                <div class="stat-item"><span class="dot secondary"></span>Proficiency Levels</div>
                <div class="stat-item"><span class="dot secondary"></span>Skill Gaps</div>
              </div>
            </div>

            <!-- Forecast Report -->
            <div class="card report-card" (click)="openReport('forecast')">
              <div class="card-icon-lg">🔮</div>
              <h3 class="card-title">Forecast Report</h3>
              <p class="card-desc">Resource demand and planning forecasts</p>
              <div class="card-stats">
                <div class="stat-item"><span class="dot secondary"></span>Demand Forecast</div>
                <div class="stat-item"><span class="dot secondary"></span>Capacity Planning</div>
                <div class="stat-item"><span class="dot secondary"></span>Risk Assessment</div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }
    
    .page-header h1 {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 4px;
      color: var(--text-primary);
    }
    
    .page-header p {
      color: var(--text-muted);
    }
    
    .report-card {
      transition: transform 0.2s, box-shadow 0.2s;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      height: 100%;
    }
    
    .report-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
      border-color: var(--secondary);
    }
    
    .card-icon-lg {
      font-size: 2.5rem;
      margin-bottom: 16px;
      line-height: 1;
    }
    
    .card-title {
      font-size: 1.125rem;
      font-weight: 600;
      margin-bottom: 8px;
      color: var(--text-primary);
    }
    
    .card-desc {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 16px;
    }
    
    .card-stats {
      margin-bottom: 16px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .stat-item {
      font-size: 0.75rem;
      color: var(--text-muted);
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: var(--secondary);
    }

    .mt-4 { margin-top: 24px; }
    .mt-2 { margin-top: 12px; }
  `]
})
export class ReportsComponent {
  openReport(reportType: string): void {
    console.log('Opening report:', reportType);
    // Future implementation: Navigate to detail view or open modal
  }
}
