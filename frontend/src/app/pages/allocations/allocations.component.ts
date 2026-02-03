import { Component, OnInit, signal, ElementRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Allocation, Employee, Project, Manager } from '../../models';

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
          <div class="header-actions">
            <button class="btn btn-primary" (click)="openCreateModal()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Create Allocation
            </button>
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
          <select class="filter-select" [(ngModel)]="managerFilter" (change)="onFilter()">
            <option value="">All Managers</option>
            @for (mgr of managers(); track mgr.id) {
              <option [value]="mgr.id">{{ mgr.name }}</option>
            }
          </select>
        </div>

        @if (loading() && allocations().length === 0) {
          <div class="loading">Loading allocations...</div>
        } @else {
          <div class="card fade-in" [style.opacity]="loading() ? '0.5' : '1'">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Oracle ID</th>
                  <th>Project</th>
                  <th>Assignment</th>
                  <th>Current Allocation</th>
                  <th>Status</th>
                  <th>End Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (alloc of allocations(); track alloc.id) {
                  <tr>
                    <td>{{ alloc.employeeName }}</td>
                    <td class="text-muted">{{ alloc.employeeOracleId || '-' }}</td>
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
                    <td>
                      <button class="btn-icon" (click)="openEditModal(alloc)" title="Edit allocation">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                      </button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="8" class="empty-state">No allocations found</td>
                  </tr>
                }
              </tbody>
            </table>

            <!-- Pagination Controls -->
            <div class="pagination-controls">
              <button class="btn btn-secondary" (click)="previousPage()" [disabled]="currentPage() === 0 || loading()">
                ← Previous
              </button>

              <div class="page-numbers">
                @for (page of getPageNumbers(); track page) {
                  <button class="page-btn"
                          [class.active]="page === currentPage()"
                          [disabled]="loading()"
                          (click)="goToPage(page)">
                    {{ page + 1 }}
                  </button>
                }
              </div>

              <button class="btn btn-secondary" (click)="nextPage()" [disabled]="currentPage() >= totalPages() - 1 || loading()">
                Next →
              </button>
            </div>
          </div>
        }

        <!-- Create Allocation Modal -->
        @if (showCreateModal) {
          <div class="modal-overlay" (click)="showCreateModal = false">
            <div class="modal" (click)="$event.stopPropagation()">
              <h2>Create Allocation</h2>
              <form (ngSubmit)="createAllocation()">
                <div class="form-group">
                  <label class="form-label">Employee</label>
                  <select class="form-input" [(ngModel)]="newAllocation.employeeId" name="employeeId" required>
                    <option [ngValue]="undefined">Select employee...</option>
                    @for (emp of employeesList(); track emp.id) {
                      <option [ngValue]="emp.id">{{ emp.name }} ({{ emp.oracleId || 'N/A' }})</option>
                    }
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Project</label>
                  <select class="form-input" [(ngModel)]="newAllocation.projectId" name="projectId" required>
                    <option [ngValue]="undefined">Select project...</option>
                    @for (proj of projectsList(); track proj.id) {
                      <option [ngValue]="proj.id">{{ proj.name }} ({{ proj.projectId }})</option>
                    }
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Allocation (current month)</label>
                  <select class="form-input" [(ngModel)]="newAllocation.currentMonthAllocation" name="allocation">
                    <option value="1">100%</option>
                    <option value="0.5">50%</option>
                    <option value="0.25">25%</option>
                    <option value="B">Bench</option>
                    <option value="P">Prospect</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Start Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.startDate" name="startDate">
                </div>
                <div class="form-group">
                  <label class="form-label">End Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.endDate" name="endDate">
                </div>
                <div class="form-group">
                  <label class="form-label">Status</label>
                  <select class="form-input" [(ngModel)]="newAllocation.status" name="status">
                    <option value="ACTIVE">Active</option>
                    <option value="PENDING">Pending</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </div>
                <div class="modal-actions">
                  <button type="button" class="btn btn-secondary" (click)="showCreateModal = false">Cancel</button>
                  <button type="submit" class="btn btn-primary">Create</button>
                </div>
              </form>
            </div>
          </div>
        }

        <!-- Edit Allocation Modal (only allocation percentage) -->
        @if (showEditModal) {
          <div class="modal-overlay" (click)="showEditModal = false">
            <div class="modal" (click)="$event.stopPropagation()">
              <h2>Edit Allocation</h2>
              <p class="edit-info">{{ editAllocationData.employeeName }} — {{ editAllocationData.projectName }}</p>
              <form (ngSubmit)="updateAllocation()">
                <div class="form-group">
                  <label class="form-label">Allocation (current month)</label>
                  <select class="form-input" [(ngModel)]="editAllocationValue" name="editAllocation">
                    <option value="1">100%</option>
                    <option value="0.5">50%</option>
                    <option value="0.25">25%</option>
                    <option value="B">Bench</option>
                    <option value="P">Prospect</option>
                  </select>
                </div>
                <div class="modal-actions">
                  <button type="button" class="btn btn-secondary" (click)="showEditModal = false">Cancel</button>
                  <button type="submit" class="btn btn-primary">Save</button>
                </div>
              </form>
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

    .text-muted { color: var(--text-muted); }

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

    .btn-icon {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      border: 1px solid var(--border);
      background: var(--surface);
      color: var(--text-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-icon:hover {
      background: var(--surface-hover);
      color: var(--primary);
    }

    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      padding: 24px;
      width: 100%;
      max-width: 480px;
    }

    .modal h2 {
      margin-bottom: 8px;
    }

    .edit-info {
      color: var(--text-muted);
      font-size: 14px;
      margin-bottom: 20px;
    }

    .modal-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      margin-top: 20px;
    }

    .pagination-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
      padding: 16px;
      border-top: 1px solid var(--border);
    }

    .page-numbers {
      display: flex;
      gap: 8px;
    }

    .page-btn {
      min-width: 32px;
      height: 32px;
      border-radius: 6px;
      border: 1px solid var(--border);
      background: var(--surface);
      color: var(--text);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s;
    }

    .page-btn:hover {
      background: var(--surface-hover);
    }

    .page-btn.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
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
  private destroyRef = inject(DestroyRef);

  allocations = signal<Allocation[]>([]);
  managers = signal<Manager[]>([]);
  employeesList = signal<Employee[]>([]);
  projectsList = signal<Project[]>([]);
  loading = signal(true);

  // Search and filter state
  searchTerm = '';
  statusFilter = '';
  managerFilter = '';

  // Create modal
  showCreateModal = false;
  newAllocation: any = { status: 'ACTIVE', currentMonthAllocation: '1' };

  // Edit modal
  showEditModal = false;
  editAllocationId: number | null = null;
  editAllocationData: any = {};
  editAllocationValue = '';

  // Pagination state
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private elementRef: ElementRef
  ) { }

  ngOnInit(): void {
    this.loadManagers();
    this.loadAllocations();
  }

  loadManagers(): void {
    this.apiService.getManagers().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (managers) => this.managers.set(managers),
      error: () => {}
    });
  }

  loadAllocations(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const status = this.statusFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;

    this.apiService.getAllocations(this.currentPage(), this.pageSize(), search, status, managerId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => {
        this.allocations.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);

        if (scrollToBottom) {
          setTimeout(() => {
            const element = this.elementRef.nativeElement.querySelector('.pagination-controls');
            if (element) {
              element.scrollIntoView({ behavior: 'smooth', block: 'end' });
            }
          }, 100);
        }
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
      this.loadAllocations(true);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadAllocations(true);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.currentPage()) {
      this.currentPage.set(page);
      this.loadAllocations(true);
    }
  }

  getPageNumbers(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const range = [];

    let start = Math.max(0, current - 2);
    let end = Math.min(total - 1, start + 4);

    if (end - start < 4) {
      start = Math.max(0, end - 4);
    }

    for (let i = start; i <= end; i++) {
      range.push(i);
    }

    return range;
  }

  openCreateModal(): void {
    this.newAllocation = { status: 'ACTIVE', currentMonthAllocation: '1' };
    // Load employees and projects for dropdowns
    this.apiService.getEmployees(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.employeesList.set(page.content),
      error: () => {}
    });
    this.apiService.getProjects(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.projectsList.set(page.content),
      error: () => {}
    });
    this.showCreateModal = true;
  }

  createAllocation(): void {
    if (!this.newAllocation.employeeId || !this.newAllocation.projectId) return;

    // Build the allocation payload with the current month value set
    const monthNames = ['janAllocation', 'febAllocation', 'marAllocation', 'aprAllocation',
      'mayAllocation', 'junAllocation', 'julAllocation', 'augAllocation',
      'sepAllocation', 'octAllocation', 'novAllocation', 'decAllocation'];
    const currentMonth = new Date().getMonth(); // 0-indexed
    const payload: any = {
      employeeId: this.newAllocation.employeeId,
      projectId: this.newAllocation.projectId,
      startDate: this.newAllocation.startDate,
      endDate: this.newAllocation.endDate,
      status: this.newAllocation.status
    };
    payload[monthNames[currentMonth]] = this.newAllocation.currentMonthAllocation;

    this.apiService.createAllocation(payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.loadAllocations();
      },
      error: (err) => {
        alert('Failed to create allocation: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  openEditModal(alloc: Allocation): void {
    this.editAllocationId = alloc.id;
    this.editAllocationData = {
      employeeName: alloc.employeeName,
      projectName: alloc.projectName
    };
    this.editAllocationValue = alloc.currentMonthAllocation || '1';
    this.showEditModal = true;
  }

  updateAllocation(): void {
    if (this.editAllocationId == null) return;
    this.apiService.updateAllocation(this.editAllocationId, {
      currentMonthAllocation: this.editAllocationValue
    } as any).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.showEditModal = false;
        this.editAllocationId = null;
        this.loadAllocations();
      },
      error: (err) => {
        alert('Failed to update allocation: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }
}
