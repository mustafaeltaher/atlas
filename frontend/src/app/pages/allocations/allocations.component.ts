import { Component, OnInit, signal, ElementRef, DestroyRef, inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { HeaderComponent } from '../../components/header/header.component';
import { MonthPickerComponent } from '../../components/month-picker/month-picker.component';
import { ApiService } from '../../services/api.service';

import { Allocation, EmployeeAllocationSummary, Employee, Project, Manager } from '../../models';

@Component({
  selector: 'app-allocations',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    FormsModule,
    MonthPickerComponent
  ],
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
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <line x1="12" y1="5" x2="12" y2="19"></line>
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

          <!-- Custom Month Picker -->
          <app-month-picker
            [selectedYear]="selectedYear()"
            [selectedMonth]="selectedMonth()"
            [availableMonths]="availableMonths()"
            (monthChange)="onMonthChange($event)">
          </app-month-picker>

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
                  <th>Manager</th>
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
                    <td>{{ summary.employeeOracleId || '-' }}</td>
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
                          <span class="alloc-value">{{ summary.totalAllocationPercentage | number:'1.0-0' }}%</span>
                        }
                      </div>
                    </td>
                    <td>{{ summary.managerName || '-' }}</td>
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
                    <td colspan="6" class="empty-state">No allocations found</td>
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
          <div class="modal-overlay" (click)="closeDetailModal()">
            <div class="modal modal-wide" (click)="$event.stopPropagation()">
              <div class="detail-header">
                <div>
                  <h2>{{ selectedSummary.employeeName }}</h2>
                  <p class="edit-info">Oracle ID: {{ selectedSummary.employeeOracleId || 'N/A' }} &middot; Total: {{ selectedSummary.totalAllocationPercentage }}%</p>
                </div>
              </div>

              <!-- Add assignment inline form (always visible) -->
              <h3>Add New Allocation</h3>
              <div class="add-assignment-form" style="margin-top:8px; padding-top:0; border-top:none;">
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
                    <div class="form-group" style="min-width: 140px;">
                      <label class="form-label">Type</label>
                      <select class="form-input" [(ngModel)]="newDetailAllocation.allocationType" name="detailAllocationType" (change)="onDetailStatusChange()">
                        <option value="PROJECT">Project</option>
                        <option value="PROSPECT">Prospect</option>
                        <option value="VACATION">Vacation</option>
                        <option value="MATERNITY">Maternity</option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label class="form-label">Start Date</label>
                      <input type="date" class="form-input" [(ngModel)]="newDetailAllocation.startDate" name="detailStartDate" (change)="onDetailDateChange()">
                    </div>
                    <div class="form-group">
                      <label class="form-label">End Date</label>
                      <input type="date" class="form-input" [(ngModel)]="newDetailAllocation.endDate" name="detailEndDate" (change)="onDetailDateChange()">
                    </div>
                  </div>
                  <div class="form-row">
                    @if (newDetailAllocation.allocationType === 'PROJECT' || newDetailAllocation.allocationType === 'PROSPECT') {
                      <div class="form-group">
                        <label class="form-label">Mode</label>
                        <label class="checkbox-label" style="display:flex;align-items:center;height:38px;">
                          <input type="checkbox" (change)="onDetailMonthByMonthToggle($event)" [checked]="detailMonthByMonthMode()">
                          <span style="margin-left:8px;">Month-by-Month</span>
                        </label>
                      </div>
                      <div class="form-group" style="flex:1;">
                        @if (!detailMonthByMonthMode()) {
                          <label class="form-label">Uniform Allocation</label>
                          <input type="number" class="form-input" min="1" max="100" [(ngModel)]="newDetailAllocation.currentMonthAllocation" name="detailAllocation">
                        } @else {
                          <div class="month-list-container" style="display:flex;flex-wrap:wrap;gap:8px;">
                            @for (m of detailMonthList(); track m.key) {
                              <div class="month-item" style="display:flex;flex-direction:column;align-items:center;">
                                <label style="font-size:11px;">{{ m.label }}</label>
                                <div style="display:flex;align-items:center;">
                                  <input type="number" class="form-input form-input-sm" 
                                         [min]="1" [max]="100"
                                         [value]="getDetailMonthlyPercentage(m.key)"
                                         (input)="updateDetailMonthlyPercentage(m.key, $any($event.target).value)"
                                         style="width:60px;text-align:center;">
                                  <span style="font-size:12px;margin-left:4px;">%</span>
                                </div>
                              </div>
                            } @empty {
                              <span class="text-muted" style="font-size:12px;margin-top:8px;">Enter Start/End Date</span>
                            }
                          </div>
                        }
                      </div>
                    }
                    <div class="form-group form-actions-inline" style="margin-left:auto; align-self:flex-end;">
                      <button class="btn btn-primary" style="width: 90px; flex-shrink: 0;" (click)="createDetailAllocation()" [disabled]="(newDetailAllocation.allocationType === 'PROJECT' || newDetailAllocation.allocationType === 'PROSPECT') && !newDetailAllocation.projectId">Add</button>
                    </div>
                  </div>
                </div>

              <!-- Detail allocation list -->
              <hr style="margin-top: 24px; margin-bottom: 16px; border: 0; border-top: 1px solid var(--border-color);">
              <h3 style="margin-top: 16px; margin-bottom: 8px;">Existing Allocations</h3>
              <table class="data-table detail-table">
                <thead>
                  <tr>
                    <th>Project</th>
                    <th>Type</th>
                    <th>Allocation</th>
                    <th>Type</th>
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
                        @if (editingAllocationId === alloc.id && (alloc.allocationType === 'PROJECT' || alloc.allocationType === 'PROSPECT')) {
                          <div style="margin-bottom:8px;">
                            <label class="checkbox-label" style="display:flex;align-items:center;">
                              <input type="checkbox" (change)="toggleInlineEditMode()" [checked]="editMonthByMonthMode()">
                              <span style="margin-left:8px;font-size:12px;">Month-by-Month Allocation</span>
                            </label>
                          </div>
                          @if (!editMonthByMonthMode()) {
                            <div style="display:flex;align-items:center;gap:8px;">
                              <input type="number" class="form-input form-input-sm" min="1" max="100" [(ngModel)]="editAllocationValue" name="inlineEdit" style="width:70px;">
                            </div>
                          } @else {
                            <div class="month-list-container" style="max-width: 320px; display:flex;flex-wrap:wrap;gap:8px;">
                              @for (m of editMonthList(); track m.key) {
                                <div class="month-item" style="display:flex;flex-direction:column;align-items:center;">
                                  <label style="font-size:11px;">{{ m.label }}</label>
                                  <div style="display:flex;align-items:center;">
                                    <input type="number" class="form-input form-input-sm" 
                                           [min]="1" [max]="100"
                                           [value]="getEditMonthlyPercentage(m.key)"
                                           (input)="updateEditMonthlyPercentage(m.key, $any($event.target).value)"
                                           [disabled]="m.isPast"
                                           [title]="m.isPast ? 'Past month' : ''"
                                           style="width:60px;text-align:center;">
                                    <span style="font-size:12px;margin-left:4px;" [class.text-muted]="m.isPast">%</span>
                                  </div>
                                </div>
                              }
                            </div>
                          }
                        } @else {
                          <div class="allocation-cell">
                            @if (isMonthByMonth(alloc)) {
                              <div class="month-list-readonly" style="max-width: 250px; display:grid;grid-template-columns:1fr 1fr;gap:4px;">
                                @for (m of getSortedMonthlyAllocations(alloc); track m.id) {
                                  <span class="status-pill" 
                                        [class.active]="!isCurrentMonth(m.year, m.month)"
                                        [class.current]="isCurrentMonth(m.year, m.month)"
                                        style="font-size: 10px; padding: 2px 6px; white-space: nowrap;" 
                                        [title]="getShortMonthName(m.month) + ' ' + m.year">
                                    {{ getShortMonthName(m.month) }} '{{ m.year.toString().slice(-2) }}: {{ m.percentage }}%
                                  </span>
                                }
                              </div>
                            } @else {
                              <div class="progress-bar">
                                <div class="progress-bar-fill"
                                     [class.high]="alloc.allocationPercentage >= 75"
                                     [class.medium]="alloc.allocationPercentage >= 50 && alloc.allocationPercentage < 75"
                                     [class.low]="alloc.allocationPercentage < 50"
                                     [style.width.%]="alloc.allocationPercentage">
                                </div>
                              </div>
                              <span class="alloc-value">{{ alloc.currentMonthAllocation || 'N/A' }}</span>
                            }
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
                      <td colspan="7" class="empty-state">No assignments for this employee</td>
                    </tr>
                  }
                </tbody>
              </table>

              <div class="modal-actions" style="display:flex; justify-content:flex-end; gap:8px;">
                <button class="btn btn-secondary" style="width: 90px;" (click)="closeDetailModal()">Close</button>
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
                <div class="form-group">
                  <label class="form-label">Start Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.startDate" name="startDate" (change)="onDateChange()">
                </div>
                <div class="form-group">
                  <label class="form-label">End Date</label>
                  <input type="date" class="form-input" [(ngModel)]="newAllocation.endDate" name="endDate" (change)="onDateChange()">
                </div>
                @if (newAllocation.allocationType === 'PROJECT' || newAllocation.allocationType === 'PROSPECT') {
                  <div class="form-group">
                     <label class="form-label">Mode</label>
                     <label class="checkbox-label" style="display:flex;align-items:center;height:38px;margin-bottom:8px;">
                       <input type="checkbox" (change)="onMonthByMonthToggle($event)" [checked]="monthByMonthMode()">
                       <span style="margin-left:8px;">Month-by-Month Allocation</span>
                     </label>
                  </div>
                  @if (!monthByMonthMode()) {
                    <div class="form-group">
                      <label class="form-label">Allocation (Uniform)</label>
                      <input type="number" class="form-input" min="1" max="100" [(ngModel)]="newAllocation.currentMonthAllocation" name="allocation">
                    </div>
                  } @else {
                    <div class="form-group">
                      <label class="form-label">Monthly Allocations</label>
                      <div class="month-list-container" style="display:flex;flex-wrap:wrap;gap:8px;padding:8px;border:1px solid var(--border);border-radius:8px;margin-bottom:8px;">
                        @for (m of monthList(); track m.key) {
                          <div class="month-item" style="display:flex;flex-direction:column;align-items:center;">
                            <label style="font-size:11px;">{{ m.label }}</label>
                            <div style="display:flex;align-items:center;">
                              <input type="number" class="form-input form-input-sm" 
                                     [min]="1" [max]="100"
                                     [value]="getMonthlyPercentage(m.key)"
                                     (input)="updateMonthlyPercentage(m.key, $any($event.target).value)"
                                     style="width:60px;text-align:center;">
                              <span style="font-size:12px;margin-left:4px;">%</span>
                            </div>
                          </div>
                        } @empty {
                           <span class="text-muted" style="font-size:12px;">Enter Start and End Date first.</span>
                        }
                      </div>
                    </div>
                  }
                }
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
                          <th>Start Date</th>
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
                              @if (alloc.allocationType === 'PROJECT' || alloc.allocationType === 'PROSPECT') {
                                <div class="allocation-cell">
                                  @if (isMonthByMonth(alloc)) {
                                    <div class="month-list-readonly" style="max-width: 250px; display:grid;grid-template-columns:1fr 1fr;gap:4px;">
                                      @for (m of getSortedMonthlyAllocations(alloc); track m.id) {
                                        <span class="status-pill" 
                                              [class.active]="!isCurrentMonth(m.year, m.month)"
                                              [class.current]="isCurrentMonth(m.year, m.month)"
                                              style="font-size: 10px; padding: 2px 6px; white-space: nowrap;" 
                                              [title]="getShortMonthName(m.month) + ' ' + m.year">
                                          {{ getShortMonthName(m.month) }} '{{ m.year.toString().slice(-2) }}: {{ m.percentage }}%
                                        </span>
                                      }
                                    </div>
                                  } @else {
                                    <div class="progress-bar">
                                      <div class="progress-bar-fill"
                                           [class.high]="(alloc.currentMonthAllocation || 0) >= 75"
                                           [class.medium]="(alloc.currentMonthAllocation || 0) >= 50 && (alloc.currentMonthAllocation || 0) < 75"
                                           [class.low]="(alloc.currentMonthAllocation || 0) < 50"
                                           [style.width.%]="alloc.currentMonthAllocation || 0">
                                      </div>
                                    </div>
                                    <span class="alloc-value">{{ alloc.currentMonthAllocation || 0 }}%</span>
                                  }
                                </div>
                              } @else {
                                <span class="text-muted">-</span>
                              }
                            </td>
                            <td>{{ alloc.startDate | date:'mediumDate' }}</td>
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
      min-width: 32px;
      min-height: 32px;
      border-radius: 6px;
      border: 1px solid var(--border);
      background: var(--surface);
      color: var(--text-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-sizing: border-box;
      transition: all 0.2s;
      flex-shrink: 0;
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
      align-items: center;
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
      overflow: auto;
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

    .modal-wide .detail-table {
      margin-bottom: 20px;
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
      min-width: 90px;
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
      position: relative;
    }

    .modal-wide .modal-actions::before {
      content: '';
      position: absolute;
      top: 0;
      left: -24px;
      right: -24px;
      height: 1px;
      background: var(--border);
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

    /* Native Month Picker Styling */
    .month-picker {
      min-width: 160px;
      cursor: pointer;
      user-select: none;
    }

    .month-picker::-webkit-calendar-picker-indicator {
      cursor: pointer;
      filter: var(--calendar-icon-filter, none);
    }

    .month-picker:hover {
      border-color: var(--primary);
    }

    .month-picker:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(62, 146, 204, 0.1);
    }

    .month-picker[readonly] {
      background-color: var(--bg-card);
      cursor: pointer;
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

  // Month/Year filter state
  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number>(new Date().getMonth() + 1); // 1-indexed for backend
  availableMonths = signal<string[]>([]); // Format: "YYYY-MM"

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
  editMonthByMonthMode = signal<boolean>(false);
  editMonthlyPercentages = signal<Map<string, number>>(new Map());
  editOriginalMonthlyPercentages = new Map<string, number>();
  editMonthList = signal<Array<{ year: number, month: number, label: string, key: string, isPast: boolean }>>([]);

  // Add assignment within detail modal
  newDetailAllocation: any = { allocationType: 'PROJECT', currentMonthAllocation: '100' };
  detailMonthByMonthMode = signal<boolean>(false);
  detailMonthlyPercentages = signal<Map<string, number>>(new Map());
  detailMonthList = signal<Array<{ year: number, month: number, label: string, key: string }>>([]);

  // Create modal (from master page)
  monthByMonthMode = signal<boolean>(false);
  monthlyPercentages = signal<Map<string, number>>(new Map());
  monthList = signal<Array<{ year: number, month: number, label: string, key: string }>>([]);

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
    this.loadAvailableMonths();
    this.loadManagers();
    this.loadAllocationTypes();
    this.loadAllocations();
  }

  // Month/Year picker methods
  onMonthChange(event: { year: number; month: number }): void {
    this.selectedYear.set(event.year);
    this.selectedMonth.set(event.month);
    this.currentPage.set(0); // Reset pagination
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
  }

  loadAvailableMonths(): void {
    const allocationType = this.allocationTypeFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const search = this.searchTerm || undefined;

    this.apiService.getAvailableMonths(allocationType, managerId, search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (months) => this.availableMonths.set(months),
        error: () => this.availableMonths.set([])
      });
  }

  loadManagers(managerNameSearch?: string): void {
    const allocationType = this.allocationTypeFilter || undefined;
    const globalSearch = this.searchTerm || undefined;
    const managerSearch = managerNameSearch || undefined;
    const year = this.selectedYear();
    const month = this.selectedMonth();
    this.apiService.getAllocationManagers(allocationType, globalSearch, managerSearch, year, month).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    const year = this.selectedYear();
    const month = this.selectedMonth();
    this.apiService.getAllocationTypes(managerId, search, year, month).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (types) => this.allocationTypes.set(types),
      error: () => { }
    });
  }

  loadAllocations(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const allocationType = this.allocationTypeFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const year = this.selectedYear();
    const month = this.selectedMonth();

    this.apiService.getGroupedAllocations(this.currentPage(), this.pageSize(), search, allocationType, managerId, year, month)
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
    this.loadAvailableMonths();
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
  }

  onFilter(): void {
    this.currentPage.set(0);
    this.loadAvailableMonths();
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

  isMonthByMonth(alloc: any): boolean {
    return Array.isArray(alloc.monthlyAllocations) && alloc.monthlyAllocations.length > 0;
  }

  getSortedMonthlyAllocations(alloc: any): any[] {
    if (!alloc.monthlyAllocations) return [];

    // Default to full array if dates are missing, though they shouldn't be
    let startD = new Date(1900, 0, 1);
    let endD = new Date(2100, 0, 1);

    if (alloc.startDate && alloc.endDate) {
      startD = new Date(alloc.startDate);
      startD.setDate(1); // Normalize to month start
      startD.setHours(0, 0, 0, 0);
      endD = new Date(alloc.endDate);
      endD.setDate(28); // Push to near end of month to safely boundary check year-month (don't worry about day)
      endD.setHours(23, 59, 59, 999);
    }

    return [...alloc.monthlyAllocations]
      .filter((m: any) => {
        const d = new Date(m.year, m.month - 1, 5); // arbitrarily day 5 to avoid timezone shifts throwing it into previous month
        return d >= startD && d <= endD;
      })
      .sort((a, b) => (a.year - b.year) || (a.month - b.month));
  }

  getShortMonthName(month: number): string {
    const d = new Date();
    d.setMonth(month - 1);
    return d.toLocaleString('default', { month: 'short' });
  }

  isCurrentMonth(year: number, month: number): boolean {
    const today = new Date();
    return today.getFullYear() === year && (today.getMonth() + 1) === month;
  }

  // --- Detail modal ---

  openDetailModal(summary: EmployeeAllocationSummary): void {
    this.selectedSummary = summary;
    this.detailAllocations.set(summary.allocations || []);
    this.editingAllocationId = null;
    this.showDetailModal = true;

    // Reset add form on open
    this.openAddAssignmentForm();

    // Immediately fetch all allocations to guarantee PROSPECT or other hidden types are consistently visible from load
    this.refreshDetailAllocations();
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.editingAllocationId = null;
  }

  handleDetailCancel(): void {
    if (this.editingAllocationId !== null) {
      this.editingAllocationId = null;
    } else {
      this.closeDetailModal();
    }
  }

  refreshDetailAllocations(): void {
    const filterYear = this.selectedYear();
    const filterMonth = this.selectedMonth();

    this.apiService.getAllocationsByEmployee(this.selectedSummary.employeeId, filterYear, filterMonth)
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

  startInlineEdit(alloc: any): void {
    this.editingAllocationId = alloc.id;
    this.editAllocationValue = alloc.currentMonthAllocation?.toString() || '100';

    // Check if this allocation was a month-by-month one. We determine this by whether it has multiple 
    // monthly allocations or relies on `monthlyAllocations`. Actually, the backend DTO now returns `monthlyAllocations`.
    const monthlyList = alloc.monthlyAllocations || [];
    if (monthlyList.length > 0) {
      this.editMonthByMonthMode.set(true);
    } else {
      this.editMonthByMonthMode.set(false);
    }

    // Generate month list for edit based on allocation bounds
    if (alloc.startDate && alloc.endDate) {
      const start = new Date(alloc.startDate);
      const end = new Date(alloc.endDate);
      const list = [];
      const initialMap = new Map<string, number>();
      this.editOriginalMonthlyPercentages.clear();

      let current = new Date(start.getFullYear(), start.getMonth(), 1);
      const endMonth = new Date(end.getFullYear(), end.getMonth(), 1);

      while (current <= endMonth) {
        const year = current.getFullYear();
        const month = current.getMonth() + 1;
        const key = `${year}-${month.toString().padStart(2, '0')}`;
        const label = current.toLocaleString('default', { month: 'short', year: 'numeric' });

        const isPast = this.isPastMonth(year, month);
        let defaultVal = 100;
        const existing = monthlyList.find((m: any) => m.year === year && m.month === month);
        if (existing) {
          defaultVal = existing.percentage;
        } else if (alloc.currentMonthAllocation != null) {
          defaultVal = alloc.currentMonthAllocation;
        }

        initialMap.set(key, defaultVal);
        this.editOriginalMonthlyPercentages.set(key, defaultVal);
        list.push({ year, month, label, key, isPast });

        current.setMonth(current.getMonth() + 1);
      }
      this.editMonthList.set(list);
      this.editMonthlyPercentages.set(initialMap);

      // If all months are past, user shouldn't switch to month-by-month to edit past. 
      // We keep it functionally handled in UI.
    } else {
      this.editMonthList.set([]);
      this.editMonthlyPercentages.set(new Map());
    }
  }

  isPastMonth(year: number, month: number): boolean {
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth() + 1;
    return year < currentYear || (year === currentYear && month < currentMonth);
  }

  toggleInlineEditMode(): void {
    this.editMonthByMonthMode.update(v => !v);
  }

  getEditMonthlyPercentage(key: string): number {
    return this.editMonthlyPercentages().get(key) || 100;
  }

  updateEditMonthlyPercentage(key: string, value: string): void {
    const numValue = parseInt(value, 10);
    if (!isNaN(numValue)) {
      const map = new Map(this.editMonthlyPercentages());
      const clampedValue = Math.max(1, Math.min(100, numValue));
      map.set(key, clampedValue);
      this.editMonthlyPercentages.set(map);
    }
  }

  cancelInlineEdit(): void {
    this.editingAllocationId = null;
    this.editAllocationValue = '';
  }

  saveInlineEdit(): void {
    if (this.editingAllocationId == null) return;

    const currentAlloc = this.detailAllocations().find(a => a.id === this.editingAllocationId);
    let payload: any = {
      allocationType: currentAlloc ? currentAlloc.allocationType : 'PROJECT'
    };

    if (this.editMonthByMonthMode()) {
      const changes: any[] = [];
      const currentMap = this.editMonthlyPercentages();
      for (const { year, month, key, isPast } of this.editMonthList()) {
        if (!isPast) { // Only send changed future/current months
          const currentVal = currentMap.get(key);
          const originalVal = this.editOriginalMonthlyPercentages.get(key);
          if (currentVal !== originalVal) {
            changes.push({ year, month, percentage: currentVal });
          }
        }
      }

      // Validate 
      for (const c of changes) {
        if (c.percentage < 1 || c.percentage > 100) {
          alert('Percentages must be between 1 and 100');
          return;
        }
      }

      if (changes.length === 0) {
        // No changes
        this.editingAllocationId = null;
        return;
      }
      payload.monthlyAllocations = changes;
    } else {
      const val = parseInt(this.editAllocationValue, 10);
      if (isNaN(val) || val < 1 || val > 100) {
        alert('Percentage must be between 1 and 100');
        return;
      }
      payload.currentMonthAllocation = val;
    }

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

  private previousDetailDates: { start?: string, end?: string } = {};

  openAddAssignmentForm(): void {
    this.newDetailAllocation = { allocationType: 'PROJECT', currentMonthAllocation: '100' };
    this.previousDetailDates = {};
    this.detailProjectSearchText = '';
    this.showDetailProjectDropdown = false;
    this.detailMonthByMonthMode.set(false);
    this.detailMonthlyPercentages.set(new Map());
    this.detailMonthList.set([]);

    this.apiService.getProjects(0, 100).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => this.projectsList.set(page.content),
      error: () => { }
    });
  }

  onDetailDateChange(): void {
    if (this.detailMonthByMonthMode()) {
      this.generateDetailMonthList();
    } else {
      this.previousDetailDates = { start: this.newDetailAllocation.startDate, end: this.newDetailAllocation.endDate };
    }
  }

  onDetailMonthByMonthToggle(event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (!isChecked && this.detailMonthlyPercentages().size > 0) {
      if (!confirm('Switching to single percentage mode will replace all individual month allocations. Continue?')) {
        (event.target as HTMLInputElement).checked = true;
        return;
      }
    }

    this.detailMonthByMonthMode.set(isChecked);
    if (isChecked) {
      this.generateDetailMonthList();
    } else {
      this.detailMonthlyPercentages.set(new Map());
      this.detailMonthList.set([]);
    }
  }

  generateDetailMonthList(): void {
    const startStr = this.newDetailAllocation.startDate;
    const endStr = this.newDetailAllocation.endDate;
    const list = [];
    if (startStr && endStr) {
      const start = new Date(startStr);
      const end = new Date(endStr);
      let current = new Date(start.getFullYear(), start.getMonth(), 1);
      const endMonth = new Date(end.getFullYear(), end.getMonth(), 1);

      const newKeys: string[] = [];
      const currentMap = this.detailMonthlyPercentages();

      while (current <= endMonth) {
        const year = current.getFullYear();
        const month = current.getMonth() + 1;
        const key = `${year}-${month.toString().padStart(2, '0')}`;
        newKeys.push(key);
        current.setMonth(current.getMonth() + 1);
      }

      const existingKeys = Array.from(currentMap.keys());
      const removedKeys = existingKeys.filter(k => !newKeys.includes(k) && currentMap.get(k) !== undefined && currentMap.get(k) !== null);

      if (removedKeys.length > 0) {
        const monthLabels = removedKeys.map(k => {
          const [y, m] = k.split('-');
          return new Date(parseInt(y), parseInt(m) - 1, 1).toLocaleString('default', { month: 'short', year: 'numeric' });
        });
        if (!confirm(`Changing the date range will remove data for ${monthLabels.join(', ')}. Continue?`)) {
          this.newDetailAllocation.startDate = this.previousDetailDates.start;
          this.newDetailAllocation.endDate = this.previousDetailDates.end;
          return;
        }
      }

      current = new Date(start.getFullYear(), start.getMonth(), 1);
      while (current <= endMonth) {
        const year = current.getFullYear();
        const month = current.getMonth() + 1;
        const key = `${year}-${month.toString().padStart(2, '0')}`;
        const label = current.toLocaleString('default', { month: 'short', year: 'numeric' });
        list.push({ year, month, label, key });

        if (!currentMap.has(key)) {
          const map = new Map(this.detailMonthlyPercentages());
          map.set(key, undefined as any);
          this.detailMonthlyPercentages.set(map);
        }
        current.setMonth(current.getMonth() + 1);
      }

      if (removedKeys.length > 0) {
        const map = new Map(this.detailMonthlyPercentages());
        for (const k of removedKeys) {
          map.delete(k);
        }
        this.detailMonthlyPercentages.set(map);
      }
      this.previousDetailDates = { start: startStr, end: endStr };
    }
    this.detailMonthList.set(list);
  }

  getDetailMonthlyPercentage(key: string): number | '' {
    const val = this.detailMonthlyPercentages().get(key);
    return val === undefined || val === null ? '' : val;
  }

  updateDetailMonthlyPercentage(key: string, value: string): void {
    const map = new Map(this.detailMonthlyPercentages());
    if (value === '') {
      map.set(key, undefined as any);
      this.detailMonthlyPercentages.set(map);
      return;
    }
    const numValue = parseInt(value, 10);
    if (!isNaN(numValue)) {
      const clampedValue = Math.max(1, Math.min(100, numValue));
      map.set(key, clampedValue);
      this.detailMonthlyPercentages.set(map);
    }
  }

  createDetailAllocation(): void {
    const allocType = this.newDetailAllocation.allocationType || 'PROJECT';
    const requiresProject = allocType === 'PROJECT' || allocType === 'PROSPECT';
    if (requiresProject && !this.newDetailAllocation.projectId) return;

    if ((allocType === 'PROJECT' || allocType === 'PROSPECT') && this.detailMonthByMonthMode()) {
      const map = this.detailMonthlyPercentages();
      for (const { key } of this.detailMonthList()) {
        const val = map.get(key);
        if (val === undefined || val === null) {
          alert('Percentage is required for all months');
          return;
        }
        if (val < 1 || val > 100) {
          alert('Percentage must be between 1 and 100');
          return;
        }
      }
    }

    const currentYear = new Date().getFullYear();

    const payload: any = {
      employeeId: this.selectedSummary.employeeId,
      projectId: requiresProject ? this.newDetailAllocation.projectId : null,
      startDate: this.newDetailAllocation.startDate,
      endDate: this.newDetailAllocation.endDate,
      allocationType: allocType,
      year: currentYear
    };

    if (allocType === 'PROJECT' || allocType === 'PROSPECT') {
      if (this.detailMonthByMonthMode()) {
        const changes: any[] = [];
        const currentMap = this.detailMonthlyPercentages();
        for (const { year, month, key } of this.detailMonthList()) {
          changes.push({ year, month, percentage: currentMap.get(key) || 100 });
        }
        payload.monthlyAllocations = changes;
      } else {
        const val = parseInt(this.newDetailAllocation.currentMonthAllocation, 10) || 100;
        if (val < 1 || val > 100) {
          alert('Percentage must be between 1 and 100');
          return;
        }
        payload.currentMonthAllocation = val;
      }
    }

    this.apiService.createAllocation(payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.openAddAssignmentForm(); // reset form for the next entry
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
    this.monthByMonthMode.set(false);
    this.monthlyPercentages.set(new Map());
    this.monthList.set([]);

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

  onDateChange(): void {
    if (this.monthByMonthMode()) {
      this.generateMonthList();
    }
  }

  onMonthByMonthToggle(event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    this.monthByMonthMode.set(isChecked);
    if (isChecked) {
      this.generateMonthList();
    }
  }

  generateMonthList(): void {
    const startStr = this.newAllocation.startDate;
    const endStr = this.newAllocation.endDate;
    const list = [];
    if (startStr && endStr) {
      const start = new Date(startStr);
      const end = new Date(endStr);
      let current = new Date(start.getFullYear(), start.getMonth(), 1);
      const endMonth = new Date(end.getFullYear(), end.getMonth(), 1);

      while (current <= endMonth) {
        const year = current.getFullYear();
        const month = current.getMonth() + 1;
        const key = `${year}-${month.toString().padStart(2, '0')}`;
        const label = current.toLocaleString('default', { month: 'short', year: 'numeric' });
        list.push({ year, month, label, key });

        const currentMap = this.monthlyPercentages();
        if (!currentMap.has(key)) {
          const map = new Map(currentMap);
          map.set(key, 100);
          this.monthlyPercentages.set(map);
        }
        current.setMonth(current.getMonth() + 1);
      }
    }
    this.monthList.set(list);
  }

  getMonthlyPercentage(key: string): number {
    return this.monthlyPercentages().get(key) || 100;
  }

  updateMonthlyPercentage(key: string, value: string): void {
    const numValue = parseInt(value, 10);
    if (!isNaN(numValue)) {
      const map = new Map(this.monthlyPercentages());
      const clampedValue = Math.max(1, Math.min(100, numValue));
      map.set(key, clampedValue);
      this.monthlyPercentages.set(map);
    }
  }

  createAllocation(): void {
    const allocType = this.newAllocation.allocationType || 'PROJECT';
    const requiresProject = allocType === 'PROJECT' || allocType === 'PROSPECT';
    if (!this.newAllocation.employeeId || (requiresProject && !this.newAllocation.projectId)) return;

    if ((allocType === 'PROJECT' || allocType === 'PROSPECT') && this.monthByMonthMode()) {
      const map = this.monthlyPercentages();
      for (const val of map.values()) {
        if (val < 1 || val > 100) {
          alert('Percentages must be between 1 and 100');
          return;
        }
      }
    }

    const currentYear = new Date().getFullYear();

    const payload: any = {
      employeeId: this.newAllocation.employeeId,
      projectId: requiresProject ? this.newAllocation.projectId : null,
      startDate: this.newAllocation.startDate,
      endDate: this.newAllocation.endDate,
      allocationType: allocType,
      year: currentYear
    };

    if (allocType === 'PROJECT' || allocType === 'PROSPECT') {
      if (this.monthByMonthMode()) {
        const changes: any[] = [];
        const currentMap = this.monthlyPercentages();
        for (const { year, month, key } of this.monthList()) {
          changes.push({ year, month, percentage: currentMap.get(key) || 100 });
        }
        payload.monthlyAllocations = changes;
      } else {
        const val = parseInt(this.newAllocation.currentMonthAllocation, 10) || 100;
        if (val < 1 || val > 100) {
          alert('Percentage must be between 1 and 100');
          return;
        }
        payload.currentMonthAllocation = val;
      }
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
    if (this.newAllocation.allocationType === 'PROJECT' || this.newAllocation.allocationType === 'PROSPECT') {
      this.newAllocation.currentMonthAllocation = '100';
    }
  }

  onDetailStatusChange(): void {
    if (this.newDetailAllocation.allocationType === 'PROJECT' || this.newDetailAllocation.allocationType === 'PROSPECT') {
      this.newDetailAllocation.currentMonthAllocation = '100';
    }
  }
  // Employee Details Modal
  selectedEmployee = signal<Employee | null>(null);
  selectedEmployeeAllocations = signal<Allocation[]>([]);

  viewEmployeeDetails(summary: EmployeeAllocationSummary): void {
    // Clear existing set first
    this.selectedEmployeeAllocations.set([]);

    // Fetch both the full actual allocations and the employee details in parallel
    forkJoin({
      allocations: this.apiService.getAllocationsByEmployee(summary.employeeId),
      employee: this.apiService.getEmployee(summary.employeeId)
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.selectedEmployeeAllocations.set(data.allocations || []);
          this.selectedEmployee.set(data.employee);
        },
        error: (err) => {
          console.error('Failed to load employee details or allocations', err);
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

