import { Component, OnInit, signal, ElementRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Project } from '../../models';

@Component({
  selector: 'app-projects',
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
            <h1>Projects</h1>
            <p>Manage projects and track allocation</p>
          </div>
          <div class="header-actions">
            <div class="view-toggle">
              <button class="toggle-btn" [class.active]="viewMode() === 'list'" (click)="viewMode.set('list')" title="List View">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="8" y1="6" x2="21" y2="6"></line>
                  <line x1="8" y1="12" x2="21" y2="12"></line>
                  <line x1="8" y1="18" x2="21" y2="18"></line>
                </svg>
              </button>
              <button class="toggle-btn" [class.active]="viewMode() === 'grid'" (click)="viewMode.set('grid')" title="Grid View">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7"></rect>
                  <rect x="14" y="3" width="7" height="7"></rect>
                  <rect x="14" y="14" width="7" height="7"></rect>
                  <rect x="3" y="14" width="7" height="7"></rect>
                </svg>
              </button>
            </div>
            <button class="btn btn-primary" (click)="showModal = true">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Add Project
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
            <input type="text" placeholder="Search projects..." [(ngModel)]="searchTerm" (input)="onSearch()">
          </div>
          @if (isN1Manager()) {
            <select class="filter-select" [(ngModel)]="towerFilter" (change)="onFilter()">
              <option value="">All Towers</option>
              @for (tower of towers(); track tower) {
                <option [value]="tower">{{ tower }}</option>
              }
            </select>
          }
          <select class="filter-select" [(ngModel)]="statusFilter" (change)="onFilter()">
            <option value="">All Statuses</option>
            @for (status of statuses(); track status) {
              <option [value]="status">{{ status | titlecase }}</option>
            }
          </select>
        </div>

        @if (loading() && projects().length === 0) {
          <div class="loading">Loading projects...</div>
        } @else {
          @if (viewMode() === 'grid') {
            <div class="grid grid-3 fade-in" [style.opacity]="loading() ? '0.5' : '1'">
              @for (project of projects(); track project.id) {
                <div class="card project-card" (click)="openEditModal(project)">
                  <div class="project-header">
                    <h3>{{ project.name }}</h3>
                    <span class="status-pill" [class]="project.status.toLowerCase()">{{ project.status }}</span>
                  </div>
                  <p class="project-id">{{ project.projectId }}</p>
                  <p class="project-tower">{{ project.tower }}</p>

                  <div class="project-stats">
                    <div class="stat">
                      <span class="stat-value">{{ project.allocatedEmployees }}</span>
                      <span class="stat-label">Employees</span>
                    </div>
                    <div class="stat">
                      <span class="stat-value">{{ project.averageAllocation | number:'1.0-0' }}%</span>
                      <span class="stat-label">Avg Alloc</span>
                    </div>
                  </div>

                  @if (project.startDate || project.endDate) {
                    <div class="project-dates">
                      <span>{{ project.startDate | date:'mediumDate' }}</span>
                      <span>→</span>
                      <span>{{ project.endDate | date:'mediumDate' }}</span>
                    </div>
                  }
                </div>
              } @empty {
                <div class="empty-state">No projects found</div>
              }
            </div>
          } @else {
            <div class="list-view fade-in" [style.opacity]="loading() ? '0.5' : '1'">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Project Name</th>
                    <th>ID</th>
                    <th>Tower</th>
                    <th>Status</th>
                    <th>Employees</th>
                    <th>Avg Alloc</th>
                    <th>Timeline</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (project of projects(); track project.id) {
                    <tr>
                      <td><span class="fw-500">{{ project.name }}</span></td>
                      <td class="text-muted">{{ project.projectId }}</td>
                      <td>{{ project.tower }}</td>
                      <td><span class="status-pill sm" [class]="project.status.toLowerCase()">{{ project.status }}</span></td>
                      <td>{{ project.allocatedEmployees }}</td>
                      <td>{{ project.averageAllocation | number:'1.0-0' }}%</td>
                      <td class="text-sm">
                        @if (project.startDate || project.endDate) {
                          {{ project.startDate | date:'MMM d, y' }} — {{ project.endDate | date:'MMM d, y' }}
                        }
                      </td>
                      <td>
                        <button class="btn-icon" (click)="openEditModal(project)" title="Edit project">
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                          </svg>
                        </button>
                      </td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="8">
                        <div class="empty-state">No projects found</div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
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
        }

        <!-- Add Project Modal -->
        @if (showModal) {
          <div class="modal-overlay" (click)="showModal = false">
            <div class="modal" (click)="$event.stopPropagation()">
              <h2>Add New Project</h2>
              <form (ngSubmit)="createProject()">
                <div class="form-group">
                  <label class="form-label">Project ID</label>
                  <input type="text" class="form-input" [(ngModel)]="newProject.projectId" name="projectId" required>
                </div>
                <div class="form-group">
                  <label class="form-label">Name</label>
                  <input type="text" class="form-input" [(ngModel)]="newProject.name" name="name" required>
                </div>
                <div class="form-group">
                  <label class="form-label">Tower</label>
                  <input type="text" class="form-input" [(ngModel)]="newProject.tower" name="tower">
                </div>
                <div class="form-group">
                  <label class="form-label">Description</label>
                  <textarea class="form-input" [(ngModel)]="newProject.description" name="description" rows="3"></textarea>
                </div>
                <div class="modal-actions">
                  <button type="button" class="btn btn-secondary" (click)="showModal = false">Cancel</button>
                  <button type="submit" class="btn btn-primary">Create</button>
                </div>
              </form>
            </div>
          </div>
        }

        <!-- Edit Project Modal -->
        @if (showEditModal) {
          <div class="modal-overlay" (click)="showEditModal = false">
            <div class="modal" (click)="$event.stopPropagation()">
              <h2>Edit Project</h2>
              <p class="edit-project-name">{{ editProject.name }} ({{ editProject.projectId }})</p>
              <form (ngSubmit)="updateProject()">
                <div class="form-group">
                  <label class="form-label">Status</label>
                  <select class="form-input" [(ngModel)]="editProject.status" name="editStatus">
                    <option value="ACTIVE">Active</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="ON_HOLD">On Hold</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Start Date</label>
                  <input type="date" class="form-input" [(ngModel)]="editProject.startDate" name="editStartDate">
                </div>
                <div class="form-group">
                  <label class="form-label">End Date</label>
                  <input type="date" class="form-input" [(ngModel)]="editProject.endDate" name="editEndDate">
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

    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .view-toggle {
      display: flex;
      background: var(--bg-secondary);
      border-radius: 8px;
      padding: 4px;
      gap: 4px;
    }

    .toggle-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      background: transparent;
      color: var(--text-secondary);
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .toggle-btn:hover {
      color: var(--text-primary);
    }

    .toggle-btn.active {
      background: var(--bg-card);
      color: var(--text-primary);
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }

    .header-left h1 { margin-bottom: 4px; }
    .header-left p { color: var(--text-muted); }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      background: var(--bg-card);
      border-radius: var(--border-radius);
      overflow: hidden;
    }

    .data-table th, .data-table td {
      padding: 12px 16px;
      text-align: left;
      border-bottom: 1px solid var(--border-color);
    }

    .data-table th {
      background: var(--bg-secondary);
      font-weight: 600;
      color: var(--text-secondary);
      font-size: 13px;
    }

    .data-table td {
      font-size: 14px;
      color: var(--text-primary);
    }

    .data-table tr:last-child td {
      border-bottom: none;
    }

    .text-muted { color: var(--text-muted); }
    .text-sm { font-size: 13px; }
    .fw-500 { font-weight: 500; }

    .status-pill.sm {
      padding: 2px 8px;
      font-size: 11px;
    }

    .loading, .empty-state {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
    }

    .project-card {
      transition: transform 0.2s, box-shadow 0.2s;
      cursor: pointer;
    }

    .project-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }

    .project-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 8px;
    }

    .project-header h3 {
      font-size: 1rem;
    }

    .project-id {
      font-size: 12px;
      color: var(--text-muted);
      margin-bottom: 4px;
    }

    .project-tower {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 16px;
    }

    .project-stats {
      display: flex;
      gap: 24px;
      margin-bottom: 12px;
    }

    .stat {
      display: flex;
      flex-direction: column;
    }

    .stat-value {
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary);
    }

    .stat-label {
      font-size: 11px;
      color: var(--text-muted);
      text-transform: uppercase;
    }

    .project-dates {
      font-size: 12px;
      color: var(--text-muted);
      display: flex;
      gap: 8px;
      padding-top: 12px;
      border-top: 1px solid var(--border-color);
    }

    .status-pill.active { background: rgba(46, 204, 113, 0.15); color: var(--accent); }
    .status-pill.completed { background: rgba(62, 146, 204, 0.15); color: var(--secondary); }
    .status-pill.on_hold { background: rgba(231, 76, 60, 0.15); color: var(--danger); }

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

    .edit-project-name {
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

    .pagination-info {
      color: var(--text-muted);
      font-size: 14px;
      white-space: nowrap;
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
export class ProjectsComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  projects = signal<Project[]>([]);
  towers = signal<string[]>([]);
  statuses = signal<string[]>([]);
  loading = signal(true);
  viewMode = signal<'grid' | 'list'>('list');
  showModal = false;
  showEditModal = false;
  newProject: Partial<Project> = {};
  editProject: Partial<Project> & { id?: number } = {};
  editProjectId: number | null = null;

  // Search and filter state
  searchTerm = '';
  towerFilter = '';
  statusFilter = '';

  // Pagination state
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);
  Math = Math;

  constructor(private apiService: ApiService, private authService: AuthService, private elementRef: ElementRef) { }

  ngOnInit(): void {
    this.loadTowers();
    this.loadStatuses();
    this.loadProjects();
  }

  isN1Manager(): boolean {
    const user = this.authService.currentUser();
    return user?.role === 'EXECUTIVE';
  }

  loadTowers(): void {
    const status = this.statusFilter || undefined;
    this.apiService.getProjectTowers(status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (towers) => this.towers.set(towers),
      error: (err) => console.error('Failed to load towers', err)
    });
  }

  loadStatuses(): void {
    const tower = this.towerFilter || undefined;
    this.apiService.getProjectStatuses(tower).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (statuses) => this.statuses.set(statuses),
      error: (err) => console.error('Failed to load statuses', err)
    });
  }

  loadProjects(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const tower = this.towerFilter || undefined;
    const status = this.statusFilter || undefined;

    this.apiService.getProjects(this.currentPage(), this.pageSize(), search, tower, status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => {
        this.projects.set(page.content);
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
      this.loadProjects(true);
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadProjects(true);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.currentPage()) {
      this.currentPage.set(page);
      this.loadProjects(true);
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

  onSearch(): void {
    this.currentPage.set(0);
    this.loadProjects();
  }

  onFilter(): void {
    this.currentPage.set(0);
    this.loadProjects();
    this.loadTowers();
    this.loadStatuses();
  }

  createProject(): void {
    this.apiService.createProject(this.newProject).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.showModal = false;
        this.newProject = {};
        this.loadProjects();
      },
      error: (err) => {
        alert('Failed to create project: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  openEditModal(project: Project): void {
    this.editProjectId = project.id;
    this.editProject = {
      name: project.name,
      projectId: project.projectId,
      status: project.status,
      startDate: project.startDate ? project.startDate.substring(0, 10) : '',
      endDate: project.endDate ? project.endDate.substring(0, 10) : ''
    };
    this.showEditModal = true;
  }

  updateProject(): void {
    if (this.editProjectId == null) return;
    this.apiService.updateProject(this.editProjectId, {
      status: this.editProject.status,
      startDate: this.editProject.startDate,
      endDate: this.editProject.endDate
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.showEditModal = false;
        this.editProject = {};
        this.editProjectId = null;
        this.loadProjects();
      },
      error: (err) => {
        alert('Failed to update project: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }
}
