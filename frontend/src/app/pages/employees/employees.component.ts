import { Component, OnInit, signal, ElementRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Employee, Manager } from '../../models';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="layout">
      <app-sidebar></app-sidebar>

      <div class="main-area">
        <app-header></app-header>
        <main class="main-content">
        <header class="page-header">
          <div class="header-left">
            <h1>Employees</h1>
            <p>Manage and track employee allocation</p>
          </div>
          <div class="header-actions">
            <input type="file" #fileInput (change)="onFileSelected($event)" accept=".xlsx,.xls" style="display: none">
            <button class="btn btn-primary" (click)="fileInput.click()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              Import Excel
            </button>
          </div>
        </header>

        <div class="filters-bar">
          <input
            type="text"
            class="form-input search-input"
            [(ngModel)]="searchTerm"
            (ngModelChange)="filterEmployees()"
            placeholder="Search by name or email...">

          <select class="form-input filter-select" [(ngModel)]="statusFilter" (ngModelChange)="filterEmployees()">
            <option value="">All Status</option>
            @for (status of statuses(); track status) {
              <option [value]="status">{{ status | titlecase }}</option>
            }
          </select>

          <div class="searchable-select filter-select">
            <input type="text"
                   class="form-input"
                   placeholder="All Managers"
                   [(ngModel)]="managerSearchText"
                   (input)="onManagerSearchInput()"
                   (focus)="showManagerDropdown = true"
                   (blur)="closeManagerDropdownDelayed()"
                   autocomplete="off">
            @if (managerFilter) {
              <button type="button" class="clear-btn" (mousedown)="clearManagerSelection($event)">&times;</button>
            }
            @if (showManagerDropdown) {
              <div class="dropdown-list">
                @for (mgr of managers(); track mgr.id) {
                  <div class="dropdown-item" (mousedown)="selectManager(mgr)">
                    {{ mgr.name }}
                  </div>
                } @empty {
                  <div class="dropdown-item disabled">No managers found</div>
                }
              </div>
            }
          </div>

          @if (isN1Manager()) {
            <select class="form-input filter-select" [(ngModel)]="towerFilter" (ngModelChange)="filterEmployees()">
              <option value="">All Towers</option>
              @for (tower of towers(); track tower) {
                <option [value]="tower">{{ tower }}</option>
              }
            </select>
          }
        </div>

        @if (loading() && employees().length === 0) {
          <div class="loading">Loading employees...</div>
        } @else {
          <div class="card fade-in" [style.opacity]="loading() ? '0.5' : '1'">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Initials</th>
                  <th>Employee</th>
                  <th>Oracle ID</th>
                  <th>Grade</th>
                  <th>Title</th>
                  <th>Manager</th>
                  <th>Tower</th>
                  <th>Total Allocation</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (emp of filteredEmployees(); track emp.id) {
                  <tr>
                    <td>
                      <div class="avatar">{{ getInitials(emp.name) }}</div>
                    </td>
                    <td>
                      <div class="name">{{ emp.name }}</div>
                      <div class="email">{{ emp.email }}</div>
                    </td>
                    <td class="text-muted">{{ emp.oracleId || '-' }}</td>
                    <td>{{ emp.grade }}</td>
                    <td>{{ emp.title }}</td>
                    <td>{{ emp.managerName || '-' }}</td>
                    <td>{{ emp.towerName || '-' }}</td>
                    <td>
                      <div class="allocation-cell">
                        <div class="progress-bar">
                          <div class="progress-bar-fill"
                               [class.high]="emp.totalAllocation >= 75"
                               [class.medium]="emp.totalAllocation >= 50 && emp.totalAllocation < 75"
                               [class.low]="emp.totalAllocation < 50"
                               [style.width.%]="emp.totalAllocation">
                          </div>
                        </div>
                        <span class="alloc-value">{{ emp.totalAllocation }}%</span>
                      </div>
                    </td>
                    <td>
                      <div class="status-container">
                        @if (emp.status && emp.status !== 'ACTIVE') {
                          <span class="status-pill employee-status"
                                [class.maternity]="emp.status === 'MATERNITY'"
                                [class.vacation]="emp.status === 'VACATION'"
                                [class.resigned]="emp.status === 'RESIGNED'">
                            {{ formatEmployeeStatus(emp.status) }}
                          </span>
                        } @else if (emp.allocationStatus) {
                          <span class="status-pill"
                                [class.active]="emp.allocationStatus === 'ACTIVE'"
                                [class.bench]="emp.allocationStatus === 'BENCH'"
                                [class.prospect]="emp.allocationStatus === 'PROSPECT'">
                            {{ emp.allocationStatus }}
                          </span>
                        }
                      </div>
                    </td>
                    <td>
                      <div class="action-group">
                        <button class="btn-icon" (click)="viewDetails(emp)" title="View Details">
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="10" class="empty-state">No employees found</td>
                  </tr>
                }
              </tbody>
            </table>

            <!-- Pagination Controls -->
            <div class="pagination-controls">
              <span class="pagination-info">
                Showing {{ currentPage() * pageSize() + 1 }}-{{ Math.min((currentPage() + 1) * pageSize(), totalElements()) }} of {{ totalElements() }}
              </span>

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

        <!-- Employee Details Modal -->
        @if (selectedEmployee()) {
          <div class="modal-overlay" (click)="closeDetails()">
            <div class="modal-content" (click)="$event.stopPropagation()">
              <div class="modal-header">
                <h2>Employee Details</h2>
              </div>
              <div class="modal-body">
                <div class="detail-section">
                  <h3>Personal Information</h3>
                  <div class="detail-grid">
                    <div class="detail-item">
                      <label>Full Name</label>
                      <span>{{ selectedEmployee()?.name }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Email</label>
                      <span>{{ selectedEmployee()?.email }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Oracle ID</label>
                      <span>{{ selectedEmployee()?.oracleId || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Gender</label>
                      <span>{{ selectedEmployee()?.gender || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Nationality</label>
                      <span>{{ selectedEmployee()?.nationality || 'N/A' }}</span>
                    </div>
                  </div>
                </div>

                <div class="detail-section">
                  <h3>Job Details</h3>
                  <div class="detail-grid">
                    <div class="detail-item">
                      <label>Title</label>
                      <span>{{ selectedEmployee()?.title }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Grade</label>
                      <span>{{ selectedEmployee()?.grade }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Job Level</label>
                      <span>{{ selectedEmployee()?.jobLevel || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Hiring Type</label>
                      <span>{{ selectedEmployee()?.hiringType || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Hire Date</label>
                      <span>{{ selectedEmployee()?.hireDate | date:'mediumDate' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Status</label>
                      <span>{{ selectedEmployee()?.status ? formatEmployeeStatus(selectedEmployee()!.status) : 'Active' }}</span>
                    </div>
                  </div>
                </div>

                <div class="detail-section">
                  <h3>Organization</h3>
                  <div class="detail-grid">
                    <div class="detail-item">
                      <label>Tower</label>
                      <span>{{ selectedEmployee()?.towerName || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Parent Tower</label>
                      <span>{{ selectedEmployee()?.parentTowerName || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Manager</label>
                      <span>{{ selectedEmployee()?.managerName || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Legal Entity</label>
                      <span>{{ selectedEmployee()?.legalEntity || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Location</label>
                      <span>{{ selectedEmployee()?.location || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Cost Center</label>
                      <span>{{ selectedEmployee()?.costCenter || 'N/A' }}</span>
                    </div>
                  </div>
                </div>

                <!-- Skills Section -->
                @if (selectedEmployee()?.skills?.length) {
                  <div class="detail-section">
                    <h4>Skills</h4>
                    <div class="skills-list">
                      @for (skill of selectedEmployee()!.skills; track skill.skillName) {
                        <div class="skill-tag" [class.primary]="skill.skillLevel === 'PRIMARY'" [class.secondary]="skill.skillLevel === 'SECONDARY'">
                          <span class="skill-name">{{ skill.skillName }}</span>
                          <span class="skill-grade">{{ skill.skillGrade }}</span>
                        </div>
                      }
                    </div>
                  </div>
                }

              </div>
              <div class="modal-actions">
                <button class="btn btn-primary" (click)="closeDetails()">Close</button>
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
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
    }

    .header-left h1 { margin-bottom: 4px; }
    .header-left p { color: var(--text-muted); }

    .filters-bar {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }

    .search-input {
      flex: 1;
      max-width: 400px;
    }

    .filter-select {
      width: 180px;
    }

    .searchable-select {
      position: relative;
    }

    .searchable-select .form-input {
      padding-right: 28px;
      width: 100%;
    }

    .clear-btn {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      color: var(--text-muted);
      font-size: 18px;
      cursor: pointer;
      line-height: 1;
      padding: 0 4px;
    }

    .clear-btn:hover {
      color: var(--text-primary);
    }

    .dropdown-list {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      max-height: 200px;
      overflow-y: auto;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-top: none;
      border-radius: 0 0 8px 8px;
      z-index: 1010;
      box-shadow: var(--shadow-md);
    }

    .dropdown-item {
      padding: 8px 12px;
      font-size: 14px;
      color: var(--text-primary);
      cursor: pointer;
      transition: background 0.15s;
    }

    .dropdown-item:hover {
      background: var(--bg-hover);
    }

    .dropdown-item.disabled {
      color: var(--text-muted);
      cursor: default;
      font-style: italic;
    }

    .dropdown-item.disabled:hover {
      background: transparent;
    }

    .loading, .empty-state {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
    }

    .employee-cell {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .avatar {
      width: 36px;
      min-width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 12px;
      color: white;
      flex-shrink: 0;
    }

    .name {
      font-weight: 500;
      color: var(--text-primary);
    }

    .email {
      font-size: 12px;
      color: var(--text-muted);
    }

    .text-muted {
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

    .pagination-info {
      color: var(--text-muted);
      font-size: 14px;
      white-space: nowrap;
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

    .btn-icon {
      background: none;
      border: none;
      cursor: pointer;
      color: var(--text-muted);
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-icon:hover {
      color: var(--primary);
      background: rgba(37, 99, 235, 0.1);
    }

    .action-group {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    /* Modal Styles */
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: fadeIn 0.2s ease-out;
    }

    .modal-content {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      width: 90%;
      max-width: 800px;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      box-shadow: 0 4px 20px rgba(0,0,0,0.15);
      animation: slideUp 0.3s ease-out;
    }

    .modal-header {
      padding: 24px;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-shrink: 0;
    }

    .modal-header h2 {
      margin: 0;
      font-size: 20px;
    }

    .modal-actions {
      padding: 16px 24px;
      /* Removed border-top */
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      background: var(--bg-card);
      border-bottom-left-radius: 12px;
      border-bottom-right-radius: 12px;
      flex-shrink: 0;
    }

    .modal-body {
      padding: 24px;
      overflow-y: auto;
      flex: 1;
    }

    .detail-section {
      margin-bottom: 32px;
    }

    .detail-section:last-child {
      margin-bottom: 0;
    }

    .detail-section h3 {
      font-size: 16px;
      color: var(--text-muted);
      margin-bottom: 16px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 600;
    }

    .detail-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
    }

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .detail-item label {
      font-size: 12px;
      color: var(--text-muted);
    }

    .detail-item span {
      font-weight: 500;
      color: var(--text);
    }

    .skills-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .skill-tag {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 13px;
      background: rgba(100, 116, 139, 0.1);
      color: var(--text);
    }

    .skill-tag.primary {
      background: rgba(59, 130, 246, 0.12);
      border: 1px solid rgba(59, 130, 246, 0.3);
    }

    .skill-tag.secondary {
      background: rgba(100, 116, 139, 0.08);
      border: 1px solid rgba(100, 116, 139, 0.2);
    }

    .skill-name { font-weight: 500; }

    .skill-grade {
      font-size: 11px;
      color: var(--text-muted);
      text-transform: lowercase;
    }

    @keyframes slideUp {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .status-container {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .status-pill.employee-status.maternity {
      background: rgba(236, 72, 153, 0.15);
      color: #db2777;
    }

    .status-pill.employee-status.vacation {
      background: rgba(251, 191, 36, 0.15);
      color: #d97706;
    }

    .status-pill.employee-status.resigned {
      background: rgba(107, 114, 128, 0.15);
      color: #6b7280;
    }
  `]
})
export class EmployeesComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  employees = signal<Employee[]>([]);
  filteredEmployees = signal<Employee[]>([]);
  towers = signal<string[]>([]);
  statuses = signal<string[]>([]);
  managers = signal<Manager[]>([]);

  // Search and Filter signals

  loading = signal(true);
  Math = Math; // Expose Math for template use

  searchTerm = '';
  statusFilter = '';
  towerFilter = '';
  managerFilter = '';
  managerSearchText = '';
  showManagerDropdown = false;
  private managerSearchTimeout: any;
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);

  selectedEmployee = signal<Employee | null>(null);

  viewDetails(employee: Employee): void {
    this.selectedEmployee.set(employee);
  }

  closeDetails(): void {
    this.selectedEmployee.set(null);
  }


  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private elementRef: ElementRef
  ) { }

  ngOnInit(): void {
    this.loadManagers();
    this.loadTowers();
    this.loadStatuses();
    // Small delay to ensure auth context is fully initialized before loading employees
    setTimeout(() => this.loadEmployees(), 100);
  }

  isN1Manager(): boolean {
    const user = this.authService.currentUser();
    return user?.isTopLevel === true;
  }

  loadManagers(managerSearch?: string): void {
    const tower = this.towerFilter || undefined;
    const status = this.statusFilter || undefined;
    // explicit manager search from dropdown input
    const managerSearchParam = managerSearch || undefined;
    // general employee search term
    const employeeSearchParam = this.searchTerm || undefined;

    this.apiService.getManagers(tower, status, employeeSearchParam, managerSearchParam)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (managers) => this.managers.set(managers),
        error: () => { }
      });
  }

  onManagerSearchInput(): void {
    clearTimeout(this.managerSearchTimeout);
    this.managerSearchTimeout = setTimeout(() => {
      // Explicitly pass the dropdown search text
      this.loadManagers(this.managerSearchText);
    }, 300);
    this.showManagerDropdown = true;
  }

  selectManager(mgr: Manager): void {
    this.managerFilter = String(mgr.id);
    this.managerSearchText = mgr.name;
    this.showManagerDropdown = false;
    this.filterEmployees();
  }

  clearManagerSelection(event: Event): void {
    event.preventDefault();
    this.managerFilter = '';
    this.managerSearchText = '';
    this.showManagerDropdown = false;
    this.filterEmployees();
  }

  closeManagerDropdownDelayed(): void {
    setTimeout(() => {
      this.showManagerDropdown = false;
    }, 200);
  }

  loadTowers(): void {
    if (this.isN1Manager()) {
      const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
      const status = this.statusFilter || undefined;
      const search = this.searchTerm || undefined;
      this.apiService.getEmployeeTowers(managerId, status, search).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (data) => this.towers.set(data.towers),
        error: () => { }
      });
    }
  }

  loadEmployees(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const tower = this.towerFilter || undefined;
    const status = this.statusFilter || undefined;
    this.apiService.getEmployees(this.currentPage(), this.pageSize(), search, managerId, tower, status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => {

        this.employees.set(page.content);
        this.filteredEmployees.set(page.content);
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

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.loadEmployees(true);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadEmployees(true);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.currentPage()) {
      this.currentPage.set(page);
      this.loadEmployees(true);
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

  loadStatuses(): void {
    const managerId = this.managerFilter ? parseInt(this.managerFilter) : undefined;
    const tower = this.towerFilter || undefined;
    const search = this.searchTerm || undefined;

    this.apiService.getEmployeeStatuses(managerId, tower, search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (statuses) => this.statuses.set(statuses),
        error: (err) => console.error('Failed to load statuses', err)
      });
  }

  filterEmployees(): void {
    this.currentPage.set(0);
    this.loadEmployees();

    // cascading filters
    // If manager changes, reload towers and statuses
    // But wait, if I change Status, I should reload Managers and Towers?
    // Using a simple approach: Reload all "other" dropdowns based on current selection

    // We need to be careful not to reset the selection if it's still valid, 
    // but the options might change.

    this.loadManagers();
    this.loadTowers();
    this.loadStatuses();
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  formatEmployeeStatus(status: string): string {
    switch (status) {
      case 'VACATION': return 'Vacation';
      case 'MATERNITY': return 'Maternity';
      case 'RESIGNED': return 'Resigned';
      default: return status;
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      const file = input.files[0];
      this.apiService.importEmployees(file).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (result) => {
          alert(`Successfully imported ${result.imported} employees`);
          this.loadEmployees();
        },
        error: (err) => {
          alert('Import failed: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }
}
