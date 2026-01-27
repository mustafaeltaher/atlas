import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { ApiService } from '../../services/api.service';
import { Project } from '../../models';

@Component({
    selector: 'app-projects',
    standalone: true,
    imports: [CommonModule, FormsModule, SidebarComponent],
    template: `
    <div class="layout">
      <app-sidebar></app-sidebar>
      
      <main class="main-content">
        <header class="page-header">
          <div class="header-left">
            <h1>Projects</h1>
            <p>Manage projects and track allocation</p>
          </div>
          <button class="btn btn-primary" (click)="showModal = true">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            Add Project
          </button>
        </header>
        
        @if (loading()) {
          <div class="loading">Loading projects...</div>
        } @else {
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
                    <span class="stat-value">{{ project.averageUtilization | number:'1.0-0' }}%</span>
                    <span class="stat-label">Avg Util</span>
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
