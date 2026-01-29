import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { Allocation } from '../../models';

@Component({
  selector: 'app-allocations',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, FormsModule],
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
        
        <!-- Search and Filters -->
        <div class="filters-bar">
          <div class="search-box">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input type="text" placeholder="Search by employee or project..." [(ngModel)]="searchTerm" (input)="onSearch()">
          </div>
          <select class="filter-select" [(ngModel)]="statusFilter" (change)="onFilter()">
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="PENDING">Pending</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
        
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
            
            <!-- Pagination Controls -->
            <div class="pagination-controls">
              <button class="btn btn-secondary" (click)="previousPage()" [disabled]="currentPage() === 0">
                ← Previous
              </button>
              <span class="page-info">
                Page {{ currentPage() + 1 }} of {{ totalPages() }} ({{ totalElements() }} total)
              </span>
              <button class="btn btn-secondary" (click)="nextPage()" [disabled]="currentPage() >= totalPages() - 1">
                Next →
              </button>
            </div>
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
    
    .pagination-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
      padding: 16px;
      border-top: 1px solid var(--border);
    }
    
    .page-info {
      color: var(--text-muted);
      font-size: 14px;
    }
    
    .btn-secondary {
      background: var(--surface);
      border: 1px solid var(--border);
      color: var(--text);
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }
    
    .btn-secondary:hover:not(:disabled) {
      background: var(--surface-hover);
    }
    
    .btn-secondary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .filters-bar {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }
    
    .search-box {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 8px 12px;
      flex: 1;
      min-width: 200px;
    }
    
    .search-box svg {
      color: var(--text-muted);
    }
    
    .search-box input {
      border: none;
      background: transparent;
      color: var(--text);
      outline: none;
      width: 100%;
      font-size: 14px;
    }
    
    .search-box input::placeholder {
      color: var(--text-muted);
    }
    
    .filter-select {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 8px 12px;
      color: var(--text);
      font-size: 14px;
      min-width: 140px;
      cursor: pointer;
    }
    
    .filter-select:focus {
      outline: none;
      border-color: var(--primary);
    }
  `]
})
export class AllocationsComponent implements OnInit {
  allocations = signal<Allocation[]>([]);
  loading = signal(true);

  // Search and filter state
  searchTerm = '';
  statusFilter = '';

  // Pagination state
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadAllocations();
  }

  loadAllocations(): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const status = this.statusFilter || undefined;

    this.apiService.getAllocations(this.currentPage(), this.pageSize(), search, status).subscribe({
      next: (page) => {
        this.allocations.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadAllocations();
  }

  onFilter(): void {
    this.currentPage.set(0);
    this.loadAllocations();
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.loadAllocations();
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadAllocations();
    }
  }
}
