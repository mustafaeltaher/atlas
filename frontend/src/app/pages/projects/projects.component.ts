import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';
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
              <button class="toggle-btn" [class.active]="viewMode() === 'grid'" (click)="viewMode.set('grid')" title="Grid View">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7"></rect>
                  <rect x="14" y="3" width="7" height="7"></rect>
                  <rect x="14" y="14" width="7" height="7"></rect>
                  <rect x="3" y="14" width="7" height="7"></rect>
                </svg>
              </button>
              <button class="toggle-btn" [class.active]="viewMode() === 'list'" (click)="viewMode.set('list')" title="List View">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="8" y1="6" x2="21" y2="6"></line>
                  <line x1="8" y1="12" x2="21" y2="12"></line>
                  <line x1="8" y1="18" x2="21" y2="18"></line>
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
        
        @if (loading()) {
          <div class="loading">Loading projects...</div>
        } @else {
          @if (viewMode() === 'grid') {
            <div class="grid grid-3 fade-in">
              @for (project of projects(); track project.id) {
                <div class="card project-card">
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
            <div class="list-view fade-in">
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
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="7">
                        <div class="empty-state">No projects found</div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        }
        
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
    .status-pill.pending { background: rgba(243, 156, 18, 0.15); color: var(--warning); }
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
      margin-bottom: 20px;
    }
    
    .modal-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      margin-top: 20px;
    }
  `]
})
export class ProjectsComponent implements OnInit {
  projects = signal<Project[]>([]);
  loading = signal(true);
  viewMode = signal<'grid' | 'list'>('grid');
  showModal = false;
  newProject: Partial<Project> = {};

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.apiService.getProjects().subscribe({
      next: (data) => {
        this.projects.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  createProject(): void {
    this.apiService.createProject(this.newProject).subscribe({
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
}
