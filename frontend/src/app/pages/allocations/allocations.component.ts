import { Component, OnInit, signal, ElementRef, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { ApiService } from '../../services/api.service';

import { Allocation, EmployeeAllocationSummary, Employee, Project, Manager } from '../../models';

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
            <input type="text" placeholder="Search by name or email..." [(ngModel)]="searchTerm" (input)="onSearch()">
          </div>
          <select class="filter-select" [(ngModel)]="allocationTypeFilter" (change)="onFilter()">
            <option value="">All Types</option>
            @for (type of allocationTypes(); track type) {
              <option [value]="type">{{ type | titlecase }}</option>
            }
          </select>
          <div class="searchable-select filter-select-wrap">
            <input type="text"
                   class="filter-select"
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
        </div>

        @if (loading() && employeeSummaries().length === 0) {
          <div class="loading">Loading allocations...</div>
        } @else {
          <!-- Master Table: grouped by employee -->
          <div class="card fade-in" [style.opacity]="loading() ? '0.5' : '1'">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Oracle ID</th>
                  <th>Projects</th>
                  <th>Total Allocation</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (summary of employeeSummaries(); track summary.employeeId) {
                  <tr>
                    <td>
                      <div class="employee-cell">
                        <div class="avatar-sm">{{ getInitials(summary.employeeName) }}</div>
                        <div class="employee-info">
                          <div class="name">{{ summary.employeeName }}</div>
                          <div class="email">{{ summary.employeeEmail }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="text-muted">{{ summary.employeeOracleId || '-' }}</td>
                    <td>
                      <span class="project-count">{{ summary.projectCount }}</span>
                    </td>
                    <td>
                      <div class="allocation-cell">
                        <div class="progress-bar">
                          <div class="progress-bar-fill"
                               [class.high]="summary.totalAllocationPercentage >= 75"
                               [class.medium]="summary.totalAllocationPercentage >= 50 && summary.totalAllocationPercentage < 75"
                               [class.low]="summary.totalAllocationPercentage < 50"
                               [style.width.%]="Math.min(summary.totalAllocationPercentage, 100)">
                          </div>
                        </div>
                        @if (summary.totalAllocationPercentage === 0) {
                          <span class="badge-bench">Bench</span>
                        } @else {
                          <span class="alloc-value">{{ summary.totalAllocationPercentage }}%</span>
                        }
                      </div>
                    </td>
                    <td>
                      <div class="action-group">
                        <button class="btn-icon" (click)="viewEmployeeDetails(summary); $event.stopPropagation()" title="View Profile">
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                          </svg>
                        </button>
                        <button class="btn-icon" (click)="openDetailModal(summary); $event.stopPropagation()" title="Edit allocations">
                          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                             <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                             <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="5" class="empty-state">No allocations found</td>
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

        <!-- Detail Modal: individual allocations for selected employee -->
        @if (showDetailModal) {
          <div class="modal-overlay" (click)="showDetailModal = false">
            <div class="modal modal-wide" (click)="$event.stopPropagation()">
              <div class="detail-header">
                <div>
                  <h2>{{ selectedSummary.employeeName }}</h2>
                  <p class="edit-info">Oracle ID: {{ selectedSummary.employeeOracleId || 'N/A' }} &middot; Total: {{ selectedSummary.totalAllocationPercentage }}%</p>
                </div>
                <button class="btn btn-primary btn-sm" (click)="openAddAssignmentForm()">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                  </svg>
                  Create Allocation
                </button>
              </div>

              <!-- Add assignment inline form -->
              @if (showAddAssignment) {
                <div class="add-assignment-form">
                  <div class="form-row">
                    <div class="form-group flex-1">
                      <label class="form-label">Project</label>
                      <div class="searchable-select">
                        <input type="text"
                               class="form-input"
                               placeholder="Search projects..."
                               [(ngModel)]="detailProjectSearchText"
                               (focus)="showDetailProjectDropdown = true"
                               (input)="showDetailProjectDropdown = true"
                               (blur)="closeDropdownDelayed('detailProject')"
                               name="detailProjectSearch"
                               autocomplete="off">
                        @if (newDetailAllocation.projectId) {
                          <button type="button" class="clear-btn" (mousedown)="clearDetailProjectSelection($event)">&times;</button>
                        }
                        @if (showDetailProjectDropdown) {
                          <div class="dropdown-list">
                            @for (proj of getFilteredDetailProjects(); track proj.id) {
                              <div class="dropdown-item" (mousedown)="selectDetailProject(proj)">
                                {{ proj.description }} ({{ proj.projectId }})
                              </div>
                            } @empty {
                              <div class="dropdown-item disabled">No matches found</div>
                            }
                          </div>
                        }
                      </div>
                    </div>
                    <div class="form-group">
                      <label class="form-label">Type</label>
                      <select class="form-input" [(ngModel)]="newDetailAllocation.allocationType" name="detailAllocationType" (change)="onDetailStatusChange()">
                        <option value="PROJECT">Project</option>
                        <option value="PROSPECT">Prospect</option>
                        <option value="VACATION">Vacation</option>
                        <option value="MATERNITY">Maternity</option>
                      </select>
                    </div>
                    @if (newDetailAllocation.allocationType === 'PROJECT') {
                      <div class="form-group">
                        <label class="form-label">Allocation</label>
                        <select class="form-input" [(ngModel)]="newDetailAllocation.currentMonthAllocation" name="detailAllocation">
                          <option value="100">100%</option>
                          <option value="75">75%</option>
                          <option value="50">50%</option>
                          <option value="25">25%</option>
                        </select>
                      </div>
                    }
                  </div>
                  <div class="form-row">
                    <div class="form-group">
                      <label class="form-label">Start Date</label>
                      <input type="date" class="form-input" [(ngModel)]="newDetailAllocation.startDate" name="detailStartDate">
                    </div>
                    <div class="form-group">
                      <label class="form-label">End Date</label>
                      <input type="date" class="form-input" [(ngModel)]="newDetailAllocation.endDate" name="detailEndDate">
                    </div>
                    <div class="form-group form-actions-inline">
                      <button class="btn btn-primary btn-sm" (click)="createDetailAllocation()" [disabled]="!newDetailAllocation.projectId">Save</button>
                      <button class="btn btn-secondary btn-sm" (click)="showAddAssignment = false">Cancel</button>
                    </div>
                  </div>
                </div>
              }

              <!-- Detail allocation list -->
              <table class="data-table detail-table">
                <thead>
                  <tr>
                    <th>Project</th>
                    <th>Type</th>
                    <th>Allocation</th>
                    <th>Status</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (alloc of detailAllocations(); track alloc.id) {
                    <tr>
                      <td>{{ alloc.projectName }}</td>
                      <td>{{ alloc.allocationType }}</td>
                      <td>
                        @if (editingAllocationId === alloc.id && alloc.allocationType === 'PROJECT') {
                          <select class="form-input form-input-sm" [(ngModel)]="editAllocationValue" name="inlineEdit">
                            <option value="100">100%</option>
                            <option value="75">75%</option>
                            <option value="50">50%</option>
                            <option value="25">25%</option>
                          </select>
                        } @else if (alloc.allocationType !== 'PROJECT') {
                          <span class="prospect-badge">{{ alloc.allocationType }}</span>
                        } @else {
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
                        }
                      </td>
                      <td>
                        <span class="status-pill"
                              [class.active]="alloc.allocationType === 'PROJECT'"
                              [class.prospect]="alloc.allocationType === 'PROSPECT'"
                              [class.vacation]="alloc.allocationType === 'VACATION'"
                              [class.maternity]="alloc.allocationType === 'MATERNITY'">
                          {{ alloc.allocationType }}
                        </span>
                      </td>
                      <td>{{ alloc.startDate | date:'mediumDate' }}</td>
                      <td>{{ alloc.endDate | date:'mediumDate' }}</td>
                      <td>
                        <div class="action-buttons">
                          @if (editingAllocationId === alloc.id) {
                            <button class="btn-icon save" (click)="saveInlineEdit()" title="Save">
                              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="20 6 9 17 4 12"></polyline>
                              </svg>
                            </button>
                            <button class="btn-icon" (click)="cancelInlineEdit()" title="Cancel">
                              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                              </svg>
                            </button>
                          } @else {
                            <button class="btn-icon" (click)="startInlineEdit(alloc)" title="Edit allocation">
                              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                              </svg>
                            </button>
                            <button class="btn-icon delete" (click)="deleteAllocation(alloc.id)" title="Delete allocation">
                              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                              </svg>
                            </button>
                          }
                        </div>
                      </td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="6" class="empty-state">No assignments for this employee</td>
                    </tr>
                  }
                </tbody>
              </table>

              <div class="modal-actions">
                <button class="btn btn-secondary" (click)="showDetailModal = false">Close</button>
              </div>
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
                  <div class="searchable-select">
                    <input type="text"
                           class="form-input"
                           placeholder="Search employees..."
                           [(ngModel)]="employeeSearchText"
                           (focus)="showEmployeeDropdown = true"
                           (input)="showEmployeeDropdown = true"
                           (blur)="closeDropdownDelayed('employee')"
                           name="employeeSearch"
                           autocomplete="off">
                    @if (newAllocation.employeeId) {
                      <button type="button" class="clear-btn" (mousedown)="clearEmployeeSelection($event)">&times;</button>
                    }
                    @if (showEmployeeDropdown) {
                      <div class="dropdown-list">
                        @for (emp of getFilteredEmployees(); track emp.id) {
                          <div class="dropdown-item" (mousedown)="selectEmployee(emp)">
                            {{ emp.name }} ({{ emp.oracleId || 'N/A' }})
                          </div>
                        } @empty {
                          <div class="dropdown-item disabled">No matches found</div>
                        }
                      </div>
                    }
                  </div>
                </div>
                @if (newAllocation.allocationType === 'PROJECT' || newAllocation.allocationType === 'PROSPECT') {
                  <div class="form-group">
                    <label class="form-label">Project</label>
                    <div class="searchable-select">
                      <input type="text"
                             class="form-input"
                             placeholder="Search projects..."
                             [(ngModel)]="createProjectSearchText"
                             (focus)="showCreateProjectDropdown = true"
                             (input)="showCreateProjectDropdown = true"
                             (blur)="closeDropdownDelayed('createProject')"
                             name="projectSearch"
                             autocomplete="off">
                      @if (newAllocation.projectId) {
                        <button type="button" class="clear-btn" (mousedown)="clearCreateProjectSelection($event)">&times;</button>
                      }
                      @if (showCreateProjectDropdown) {
                        <div class="dropdown-list">
                          @for (proj of getFilteredCreateProjects(); track proj.id) {
                            <div class="dropdown-item" (mousedown)="selectCreateProject(proj)">
                              {{ proj.description }} ({{ proj.projectId }})
                            </div>
                          } @empty {
                            <div class="dropdown-item disabled">No matches found</div>
                          }
                        </div>
                      }
                    </div>
                  </div>
                }
                <div class="form-group">
                  <label class="form-label">Allocation Type</label>
                  <select class="form-input" [(ngModel)]="newAllocation.allocationType" name="allocationType" (change)="onCreateStatusChange()">
                    <option value="PROJECT">Project</option>
                    <option value="PROSPECT">Prospect</option>
                    <option value="VACATION">Vacation</option>
                    <option value="MATERNITY">Maternity</option>
                  </select>
                </div>
                @if (newAllocation.allocationType === 'PROJECT') {
                  <div class="form-group">
                    <label class="form-label">Allocation (current month)</label>
                    <select class="form-input" [(ngModel)]="newAllocation.currentMonthAllocation" name="allocation">
                      <option value="100">100%</option>
                      <option value="75">75%</option>
                      <option value="50">50%</option>
                      <option value="25">25%</option>
                    </select>
                  </div>
                }
                <div class="form-group">
                  <label class="form-label">Start Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.startDate" name="startDate">
                </div>
                <div class="form-group">
                  <label class="form-label">End Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.endDate" name="endDate">
                </div>
                <div class="modal-actions">
                  <button type="button" class="btn btn-secondary" (click)="showCreateModal = false">Cancel</button>
                  <button type="submit" class="btn btn-primary">Create</button>
                </div>
              </form>
            </div>
          </div>
        }

        <!-- Employee Details Modal -->
        @if (selectedEmployee()) {
          <div class="modal-overlay" (click)="closeEmployeeDetails()">
            <div class="modal-content" (click)="$event.stopPropagation()">
              <div class="modal-header">

                <h2>Allocations Details</h2>
              </div>
              <div class="modal-body">
                <div class="detail-section">
                  <h3>Employee Summary</h3>
                  <div class="detail-grid">
                    <div class="detail-item">
                      <label>Full Name</label>
                      <span>{{ selectedEmployee()?.name }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Oracle ID</label>
                      <span>{{ selectedEmployee()?.oracleId || 'N/A' }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Title</label>
                      <span>{{ selectedEmployee()?.title }}</span>
                    </div>
                    <div class="detail-item">
                      <label>Manager</label>
                      <span>{{ selectedEmployee()?.managerName || 'N/A' }}</span>
                    </div>
                  </div>
                </div>

                <div class="detail-section">
                  <h3>Allocations</h3>
                  @if (selectedEmployeeAllocations().length > 0) {
                    <table class="data-table detail-table">
                      <thead>
                        <tr>
                          <th>Project</th>
                          <th>Type</th>
                          <th>Allocation</th>
                          <th>End Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        @for (alloc of selectedEmployeeAllocations(); track alloc.id) {
                          <tr>
                            <td>{{ alloc.projectName }}</td>
                            <td>
                              <span class="status-pill"
                                  [class.active]="alloc.allocationType === 'PROJECT'"
                                  [class.prospect]="alloc.allocationType === 'PROSPECT'"
                                  [class.vacation]="alloc.allocationType === 'VACATION'"
                                  [class.maternity]="alloc.allocationType === 'MATERNITY'">
                                {{ alloc.allocationType }}
                              </span>
                            </td>
                            <td>
                              @if (alloc.allocationType === 'PROJECT') {
                                <div class="allocation-cell">
                                  <div class="progress-bar">
                                    <div class="progress-bar-fill"
                                         [class.high]="(alloc.currentMonthAllocation || 0) >= 75"
                                         [class.medium]="(alloc.currentMonthAllocation || 0) >= 50 && (alloc.currentMonthAllocation || 0) < 75"
                                         [class.low]="(alloc.currentMonthAllocation || 0) < 50"
                                         [style.width.%]="alloc.currentMonthAllocation || 0">
                                    </div>
                                  </div>
                                  <span class="alloc-value">{{ alloc.currentMonthAllocation || 0 }}%</span>
                                </div>
                              } @else {
                                <span class="text-muted">-</span>
                              }
                            </td>
                            <td>{{ alloc.endDate | date:'mediumDate' }}</td>
                          </tr>
                        }
                      </tbody>
                    </table>
                  } @else {
                    <p class="text-muted">No active allocations.</p>
                  }
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
                <button class="btn btn-secondary" (click)="closeEmployeeDetails()">Close</button>
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

    .loading, .empty-state {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
    }

    .text-muted { color: var(--text-muted); }

    .employee-cell {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .avatar-sm {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: var(--primary);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
      flex-shrink: 0;
    }

    .employee-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .name {
      font-weight: 500;
      color: var(--text-primary);
    }

    .email {
      font-size: 12px;
      color: var(--text-muted);
    }

    .project-count {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: rgba(62, 146, 204, 0.12);
      color: var(--secondary);
      font-weight: 600;
      font-size: 13px;
      min-width: 28px;
      height: 28px;
      border-radius: 14px;
      padding: 0 8px;
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

    .badge-bench {
      background: #e2e8f0;
      color: #64748b;
      padding: 4px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .status-pill.prospect {
      background: rgba(251, 191, 36, 0.15);
      color: #d97706;
    }

    .prospect-badge {
      display: inline-block;
      padding: 4px 8px;
      background: rgba(251, 191, 36, 0.15);
      color: #d97706;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
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

    .btn-icon.save:hover {
      color: #22c55e;
      border-color: #22c55e;
    }

    .btn-icon.delete:hover {
      color: #ef4444;
      border-color: #ef4444;
    }

    .action-buttons {
      display: flex;
      gap: 6px;
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
      padding: 0;
      width: 100%;
      max-width: 480px;
      overflow: hidden;
    }

    .modal-wide {
      max-width: 800px;
      max-height: 80vh;
      overflow-y: auto;
      padding: 24px;
    }

    .modal h2 {
      margin: 0 0 8px 0;
      padding: 24px 24px 0 24px;
    }

    .modal form {
      padding: 0 24px;
    }

    .edit-info {
      color: var(--text-muted);
      font-size: 14px;
      margin-bottom: 20px;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
    }

    .detail-header h2 {
      margin-bottom: 4px;
      padding: 0;
    }

    .modal-wide .edit-info {
      padding: 0;
      margin-bottom: 20px;
    }

    .detail-table {
      font-size: 14px;
    }

    .add-assignment-form {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
    }

    .form-row {
      display: flex;
      gap: 12px;
      align-items: flex-end;
      flex-wrap: wrap;
    }

    .form-row + .form-row {
      margin-top: 12px;
    }

    .flex-1 {
      flex: 1;
      min-width: 160px;
    }

    .action-group {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .form-actions-inline {
      display: flex;
      gap: 8px;
      align-items: flex-end;
      padding-bottom: 2px;
    }


    .btn-sm {
      padding: 6px 12px;
      font-size: 13px;
    }

    .form-input-sm {
      padding: 4px 8px;
      font-size: 13px;
      min-width: 100px;
    }

    .modal form .modal-actions {
      margin-left: -24px;
      margin-right: -24px;
    }

    .modal-wide .modal-actions {
      margin-left: -24px;
      margin-right: -24px;
      margin-bottom: -24px;
    }

    .modal-actions {
      padding: 16px 24px;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      background: var(--bg-card);
      border-bottom-left-radius: 12px;
      border-bottom-right-radius: 12px;
      flex-shrink: 0;
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

    .pagination-info {
        color: var(--text-muted);
        font-size: 14px;
        white-space: nowrap;
    }

    .btn-secondary {
      background: #475569;
      border: none;
      color: white;
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-secondary:hover:not(:disabled) {
      background: #334155;
      transform: translateY(-1px);
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

    .filter-select-wrap {
      position: relative;
      min-width: 180px;
    }

    .searchable-select {
      position: relative;
    }

    .searchable-select .form-input {
      padding-right: 28px;
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

    /* Modal Styles */
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
  `]
})
export class AllocationsComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  Math = Math;

  employeeSummaries = signal<EmployeeAllocationSummary[]>([]);
  managers = signal<Manager[]>([]);
  employeesList = signal<Employee[]>([]);
  projectsList = signal<Project[]>([]);
  allocationTypes = signal<string[]>([]);
  loading = signal(true);

  // Search and filter state
  searchTerm = '';
  allocationTypeFilter = '';
  managerFilter = '';
  managerSearchText = '';
  showManagerDropdown = false;
  private managerSearchTimeout: any;

  // Create modal (from master page)
  showCreateModal = false;
  newAllocation: any = { allocationType: 'PROJECT', currentMonthAllocation: '100' };

  // Searchable dropdown state
  employeeSearchText = '';
  showEmployeeDropdown = false;
  createProjectSearchText = '';
  showCreateProjectDropdown = false;
  detailProjectSearchText = '';
  showDetailProjectDropdown = false;

  // Detail modal
  showDetailModal = false;
  selectedSummary: any = {};
  detailAllocations = signal<Allocation[]>([]);

  // Inline edit within detail modal
  editingAllocationId: number | null = null;
  editAllocationValue = '';

  // Add assignment within detail modal
  showAddAssignment = false;
  newDetailAllocation: any = { allocationType: 'PROJECT', currentMonthAllocation: '100' };

  // Pagination state
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);

  constructor(
    private apiService: ApiService,
    private elementRef: ElementRef
  ) { }

  ngOnInit(): void {
    this.loadManagers();
    this.loadAllocationTypes();
    this.loadAllocations();
  }

  loadManagers(managerNameSearch?: string): void {
    const allocationType = this.allocationTypeFilter || undefined;
    const globalSearch = this.searchTerm || undefined;
    const managerSearch = managerNameSearch || undefined;
    this.apiService.getAllocationManagers(allocationType, globalSearch, managerSearch).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (managers) => this.managers.set(managers),
      error: () => { }
    });
  }

  onManagerSearchInput(): void {
    clearTimeout(this.managerSearchTimeout);
    this.managerSearchTimeout = setTimeout(() => {
      this.loadManagers(this.managerSearchText);
    }, 300);
    this.showManagerDropdown = true;
  }

  selectManager(mgr: Manager): void {
    this.managerFilter = String(mgr.id);
    this.managerSearchText = mgr.name;
    this.showManagerDropdown = false;
    this.onFilter();
  }

  clearManagerSelection(event: Event): void {
    event.preventDefault();
    this.managerFilter = '';
    this.managerSearchText = '';
    this.showManagerDropdown = false;
    this.onFilter();
  }

  closeManagerDropdownDelayed(): void {
    setTimeout(() => {
      this.showManagerDropdown = false;
    }, 200);
  }

  loadAllocationTypes(): void {
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const search = this.searchTerm || undefined;
    this.apiService.getAllocationTypes(managerId, search).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (types) => this.allocationTypes.set(types),
      error: () => { }
    });
  }

  loadAllocations(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const allocationType = this.allocationTypeFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;

    this.apiService.getGroupedAllocations(this.currentPage(), this.pageSize(), search, allocationType, managerId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (page) => {
          this.employeeSummaries.set(page.content);
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

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.split(' ');
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name.substring(0, 2).toUpperCase();
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
  }

  onFilter(): void {
    this.currentPage.set(0);
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
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

  // --- Searchable dropdown helpers ---

  closeDropdownDelayed(which: string): void {
    setTimeout(() => {
      if (which === 'employee') this.showEmployeeDropdown = false;
      else if (which === 'createProject') this.showCreateProjectDropdown = false;
      else if (which === 'detailProject') this.showDetailProjectDropdown = false;
    }, 200);
  }

  getFilteredEmployees(): Employee[] {
    const term = this.employeeSearchText.toLowerCase();
    if (!term) return this.employeesList();
    return this.employeesList().filter(e =>
      e.name.toLowerCase().includes(term) ||
      (e.oracleId && String(e.oracleId).toLowerCase().includes(term))
    );
  }

  selectEmployee(emp: Employee): void {
    this.newAllocation.employeeId = emp.id;
    this.employeeSearchText = `${emp.name} (${emp.oracleId ?? 'N/A'})`;
    this.showEmployeeDropdown = false;
  }

  clearEmployeeSelection(event: Event): void {
    event.preventDefault();
    this.newAllocation.employeeId = undefined;
    this.employeeSearchText = '';
  }

  getFilteredCreateProjects(): Project[] {
    const term = this.createProjectSearchText.toLowerCase();
    if (!term) return this.projectsList();
    return this.projectsList().filter(p =>
      p.description.toLowerCase().includes(term) ||
      (p.projectId && p.projectId.toLowerCase().includes(term))
    );
  }

  selectCreateProject(proj: Project): void {
    this.newAllocation.projectId = proj.id;
    this.createProjectSearchText = `${proj.description} (${proj.projectId})`;
    this.showCreateProjectDropdown = false;
  }

  clearCreateProjectSelection(event: Event): void {
    event.preventDefault();
    this.newAllocation.projectId = undefined;
    this.createProjectSearchText = '';
  }

  getFilteredDetailProjects(): Project[] {
    const term = this.detailProjectSearchText.toLowerCase();
    if (!term) return this.projectsList();
    return this.projectsList().filter(p =>
      p.description.toLowerCase().includes(term) ||
      (p.projectId && p.projectId.toLowerCase().includes(term))
    );
  }

  selectDetailProject(proj: Project): void {
    this.newDetailAllocation.projectId = proj.id;
    this.detailProjectSearchText = `${proj.description} (${proj.projectId})`;
    this.showDetailProjectDropdown = false;
  }

  clearDetailProjectSelection(event: Event): void {
    event.preventDefault();
    this.newDetailAllocation.projectId = undefined;
    this.detailProjectSearchText = '';
  }

  // --- Detail modal ---

  openDetailModal(summary: EmployeeAllocationSummary): void {
    this.selectedSummary = summary;
    this.detailAllocations.set(summary.allocations || []);
    this.editingAllocationId = null;
    this.showAddAssignment = false;
    this.showDetailModal = true;
  }

  refreshDetailAllocations(): void {
    this.apiService.getAllocationsByEmployee(this.selectedSummary.employeeId)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (allocations) => {
          this.detailAllocations.set(allocations);
          // Recalculate summary totals
          let total = 0;
          for (const a of allocations) {
            total += a.allocationPercentage || 0;
          }
          this.selectedSummary.totalAllocationPercentage = total;
          this.selectedSummary.projectCount = allocations.length;
        },
        error: () => { }
      });
  }

  startInlineEdit(alloc: Allocation): void {
    this.editingAllocationId = alloc.id;
    this.editAllocationValue = alloc.currentMonthAllocation?.toString() || '100';
  }

  cancelInlineEdit(): void {
    this.editingAllocationId = null;
    this.editAllocationValue = '';
  }

  saveInlineEdit(): void {
    if (this.editingAllocationId == null) return;

    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1; // 1-indexed for backend

    const payload: any = {
      year: currentYear,
      currentMonthAllocation: parseInt(this.editAllocationValue) || 100
    };

    this.apiService.updateAllocation(this.editingAllocationId, payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.editingAllocationId = null;
        this.refreshDetailAllocations();
        this.loadAllocations();
      },
      error: (err) => {
        alert('Failed to update allocation: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  deleteAllocation(id: number): void {
    if (!confirm('Are you sure you want to delete this assignment?')) return;
    this.apiService.deleteAllocation(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.refreshDetailAllocations();
        this.loadAllocations();
      },
      error: (err) => {
        alert('Failed to delete allocation: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  // --- Add assignment from detail modal ---

  openAddAssignmentForm(): void {
    this.newDetailAllocation = { allocationType: 'PROJECT', currentMonthAllocation: '100' };
    this.detailProjectSearchText = '';
    this.showDetailProjectDropdown = false;
    this.apiService.getProjects(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.projectsList.set(page.content),
      error: () => { }
    });
    this.showAddAssignment = true;
  }

  createDetailAllocation(): void {
    const allocType = this.newDetailAllocation.allocationType || 'PROJECT';
    const requiresProject = allocType === 'PROJECT' || allocType === 'PROSPECT';
    if (requiresProject && !this.newDetailAllocation.projectId) return;

    const currentYear = new Date().getFullYear();

    const payload: any = {
      employeeId: this.selectedSummary.employeeId,
      projectId: requiresProject ? this.newDetailAllocation.projectId : null,
      startDate: this.newDetailAllocation.startDate,
      endDate: this.newDetailAllocation.endDate,
      allocationType: allocType,
      year: currentYear
    };

    // Only set allocation percentage for PROJECT type
    if (allocType === 'PROJECT') {
      payload.currentMonthAllocation = parseInt(this.newDetailAllocation.currentMonthAllocation) || 100;
    }

    this.apiService.createAllocation(payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.showAddAssignment = false;
        this.refreshDetailAllocations();
        this.loadAllocations();
      },
      error: (err) => {
        alert('Failed to create allocation: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }

  // --- Create allocation from master page ---

  openCreateModal(): void {
    this.newAllocation = { allocationType: 'PROJECT', currentMonthAllocation: '100' };
    this.employeeSearchText = '';
    this.createProjectSearchText = '';
    this.showEmployeeDropdown = false;
    this.showCreateProjectDropdown = false;
    this.apiService.getEmployees(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.employeesList.set(page.content),
      error: () => { }
    });
    this.apiService.getProjects(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.projectsList.set(page.content),
      error: () => { }
    });
    this.showCreateModal = true;
  }

  createAllocation(): void {
    const allocType = this.newAllocation.allocationType || 'PROJECT';
    const requiresProject = allocType === 'PROJECT' || allocType === 'PROSPECT';
    if (!this.newAllocation.employeeId || (requiresProject && !this.newAllocation.projectId)) return;

    const currentYear = new Date().getFullYear();

    const payload: any = {
      employeeId: this.newAllocation.employeeId,
      projectId: requiresProject ? this.newAllocation.projectId : null,
      startDate: this.newAllocation.startDate,
      endDate: this.newAllocation.endDate,
      allocationType: allocType,
      year: currentYear
    };

    // Only set allocation percentage for PROJECT type
    if (allocType === 'PROJECT') {
      payload.currentMonthAllocation = parseInt(this.newAllocation.currentMonthAllocation) || 100;
    }

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

  // Allocation type change handlers
  onCreateStatusChange(): void {
    if (this.newAllocation.allocationType === 'PROJECT') {
      this.newAllocation.currentMonthAllocation = '100';
    }
  }

  onDetailStatusChange(): void {
    if (this.newDetailAllocation.allocationType === 'PROJECT') {
      this.newDetailAllocation.currentMonthAllocation = '100';
    }
  }
  // Employee Details Modal
  selectedEmployee = signal<Employee | null>(null);
  selectedEmployeeAllocations = signal<Allocation[]>([]);

  viewEmployeeDetails(summary: EmployeeAllocationSummary): void {
    this.selectedEmployeeAllocations.set(summary.allocations || []);
    // Fetch full employee details to get skills and other info not in summary
    this.apiService.getEmployee(summary.employeeId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (employee) => {
        this.selectedEmployee.set(employee);
      },
      error: (err) => {
        console.error('Failed to load employee details', err);
        // Fallback or alert? For now just log.
      }
    });
  }

  closeEmployeeDetails(): void {
    this.selectedEmployee.set(null);
    this.selectedEmployeeAllocations.set([]);
  }

  formatEmployeeStatus(status: string): string {
    return status ? status.charAt(0).toUpperCase() + status.slice(1).toLowerCase().replace('_', ' ') : '';
  }
}

