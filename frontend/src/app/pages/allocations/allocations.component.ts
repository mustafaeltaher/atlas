import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { Allocation } from '../../models';

@Component({
  selector: 'app-allocations',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="layout">
      <app-sidebar></app-sidebar>
      
      <div class="main-area">
        <app-header></app-header>
        <main class="main-content">
        <header class="page-header">
          <div class="header-left">
            <h1>Allocations</h1>
            <p>View and manage resource allocations</p>
          </div>
        </header>
        
        @if (loading()) {
          <div class="loading">Loading allocations...</div>
        } @else {
          <div class="card fade-in">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Project</th>
                  <th>Assignment</th>
                  <th>Current Allocation</th>
                  <th>Status</th>
                  <th>End Date</th>
                </tr>
              </thead>
              <tbody>
                @for (alloc of allocations(); track alloc.id) {
                  <tr>
                    <td>{{ alloc.employeeName }}</td>
                    <td>{{ alloc.projectName }}</td>
                    <td>{{ alloc.confirmedAssignment || '-' }}</td>
                    <td>
                      <div class="allocation-cell">
                        <div class="progress-bar">
                          <div class="progress-bar-fill" 
                               [class.high]="alloc.allocationPercentage >= 75"
                               [class.medium]="alloc.allocationPercentage >= 50 && alloc.allocationPercentage < 75"
                               [class.low]="alloc.allocationPercentage < 50"
                               [style.width.%]="alloc.allocationPercentage">
                          </div>
                        </div>
                        <span class="alloc-value">{{ alloc.currentMonthAllocation || 'N/A' }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="status-pill" 
                            [class.active]="alloc.status === 'ACTIVE'"
                            [class.pending]="alloc.status === 'PENDING'"
                            [class.completed]="alloc.status === 'COMPLETED'">
                        {{ alloc.status }}
                      </span>
                    </td>
                    <td>{{ alloc.endDate | date:'mediumDate' }}</td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="6" class="empty-state">No allocations found</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </main>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
    }
    
    .header-left h1 { margin-bottom: 4px; }
    .header-left p { color: var(--text-muted); }
    
    .loading, .empty-state {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
    }
    
    .allocation-cell {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .progress-bar {
      width: 100px;
    }
    
    .alloc-value {
      font-size: 12px;
      font-weight: 500;
      min-width: 40px;
    }
    
    .status-pill.completed {
      background: rgba(62, 146, 204, 0.15);
      color: var(--secondary);
    }
  `]
})
export class AllocationsComponent implements OnInit {
  allocations = signal<Allocation[]>([]);
  loading = signal(true);

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadAllocations();
  }

  loadAllocations(): void {
    this.apiService.getAllocations().subscribe({
      next: (data) => {
        this.allocations.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
