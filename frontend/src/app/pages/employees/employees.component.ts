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
            placeholder="Search by name, skill, or tower...">

          <select class="form-input filter-select" [(ngModel)]="statusFilter" (ngModelChange)="filterEmployees()">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="BENCH">Bench</option>
            <option value="PROSPECT">Prospect</option>
          </select>

          <select class="form-input filter-select" [(ngModel)]="managerFilter" (ngModelChange)="filterEmployees()">
            <option value="">All Managers</option>
            @for (mgr of managers(); track mgr.id) {
              <option [value]="mgr.id">{{ mgr.name }}</option>
            }
          </select>

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
                  <th>Primary Skill</th>
                  <th>Allocation</th>
                  <th>Status</th>
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
                    <td>{{ emp.tower }}</td>
                    <td>{{ emp.primarySkill }}</td>
                    <td>
                      <div class="allocation-cell">
                        <div class="progress-bar">
                          <div class="progress-bar-fill"
                               [class.high]="emp.currentAllocation >= 75"
                               [class.medium]="emp.currentAllocation >= 50 && emp.currentAllocation < 75"
                               [class.low]="emp.currentAllocation < 50"
                               [style.width.%]="emp.currentAllocation">
                          </div>
                        </div>
                        <span class="alloc-value">{{ emp.currentAllocation }}%</span>
                      </div>
                    </td>
                    <td>
                      <span class="status-pill"
                            [class.active]="emp.allocationStatus === 'ACTIVE'"
                            [class.bench]="emp.allocationStatus === 'BENCH'"
                            [class.prospect]="emp.allocationStatus === 'PROSPECT'">
                        {{ emp.allocationStatus }}
                      </span>
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
  `]
})
export class EmployeesComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  employees = signal<Employee[]>([]);
  filteredEmployees = signal<Employee[]>([]);
  towers = signal<string[]>([]);
  managers = signal<Manager[]>([]);
  loading = signal(true);

  searchTerm = '';
  statusFilter = '';
  towerFilter = '';
  managerFilter = '';
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
    this.loadEmployees();
  }

  isN1Manager(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'EXECUTIVE';
  }

  loadManagers(): void {
    this.apiService.getManagers().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (managers) => this.managers.set(managers),
      error: () => {}
    });
  }

  loadEmployees(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    this.apiService.getEmployees(this.currentPage(), this.pageSize(), search, managerId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => {
        this.employees.set(page.content);
        this.filteredEmployees.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        const uniqueTowers = [...new Set(page.content.map(e => e.tower).filter(Boolean))];
        this.towers.set(uniqueTowers);
        this.loading.set(false);
        this.applyClientFilters();

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

  filterEmployees(): void {
    this.currentPage.set(0);
    this.loadEmployees();
  }

  applyClientFilters(): void {
    let filtered = this.employees();

    if (this.statusFilter) {
      filtered = filtered.filter(e => e.allocationStatus === this.statusFilter);
    }

    if (this.towerFilter) {
      filtered = filtered.filter(e => e.tower === this.towerFilter);
    }

    this.filteredEmployees.set(filtered);
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
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
