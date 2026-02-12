import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, Project, Allocation, EmployeeAllocationSummary, DashboardStats, Page, Manager } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
    private readonly API_URL = '/api';

    constructor(private http: HttpClient) { }

    // Generic methods
    get<T>(endpoint: string): Observable<T> {
        return this.http.get<T>(`${this.API_URL}${endpoint}`);
    }

    post<T>(endpoint: string, body: any): Observable<T> {
        return this.http.post<T>(`${this.API_URL}${endpoint}`, body);
    }

    // Dashboard
    getDashboardStats(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.API_URL}/dashboard/stats`);
    }

    // Employees
    getEmployees(page: number = 0, size: number = 10, search?: string, managerId?: number, tower?: string, status?: string): Observable<Page<Employee>> {
        let url = `${this.API_URL}/employees?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (managerId) url += `&managerId=${managerId}`;
        if (tower) url += `&tower=${encodeURIComponent(tower)}`;
        if (status) url += `&status=${encodeURIComponent(status)}`;
        return this.http.get<Page<Employee>>(url);
    }

    getEmployeeTowers(managerId?: number, status?: string): Observable<{ towers: string[] }> {
        let url = `${this.API_URL}/employees/towers?`;
        if (managerId) url += `managerId=${managerId}&`;
        if (status) url += `status=${encodeURIComponent(status)}&`;
        return this.http.get<{ towers: string[] }>(url);
    }

    getEmployeeStatuses(managerId?: number, tower?: string): Observable<string[]> {
        let params = new HttpParams();
        if (managerId) params = params.set('managerId', managerId.toString());
        if (tower) params = params.set('tower', tower);
        return this.http.get<string[]>(`${this.API_URL}/employees/statuses`, { params });
    }

    getProjectStatuses(region?: string): Observable<string[]> {
        let params = new HttpParams();
        if (region) params = params.set('region', region);
        return this.http.get<string[]>(`${this.API_URL}/projects/statuses`, { params });
    }

    getAllocationTypes(managerId?: number): Observable<string[]> {
        let params = new HttpParams();
        if (managerId) params = params.set('managerId', managerId.toString());
        return this.http.get<string[]>(`${this.API_URL}/allocations/allocation-types`, { params });
    }

    getAllocationManagers(allocationType?: string): Observable<Manager[]> {
        let params = new HttpParams();
        if (allocationType) params = params.set('allocationType', allocationType);
        return this.http.get<Manager[]>(`${this.API_URL}/allocations/managers`, { params });
    }

    getEmployee(id: number): Observable<Employee> {
        return this.http.get<Employee>(`${this.API_URL}/employees/${id}`);
    }

    getManagers(tower?: string, status?: string): Observable<Manager[]> {
        let url = `${this.API_URL}/employees/managers?`;
        if (tower) url += `tower=${encodeURIComponent(tower)}&`;
        if (status) url += `status=${encodeURIComponent(status)}&`;
        return this.http.get<Manager[]>(url);
    }

    importEmployees(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.API_URL}/employees/import`, formData);
    }

    // Projects
    getProjects(page: number = 0, size: number = 10, search?: string, region?: string, status?: string): Observable<Page<Project>> {
        let url = `${this.API_URL}/projects?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (region) url += `&region=${encodeURIComponent(region)}`;
        if (status) url += `&status=${encodeURIComponent(status)}`;
        return this.http.get<Page<Project>>(url);
    }

    getProjectRegions(status?: string): Observable<string[]> {
        let url = `${this.API_URL}/projects/regions?`;
        if (status) url += `status=${encodeURIComponent(status)}&`;
        return this.http.get<string[]>(url);
    }

    getProject(id: number): Observable<Project> {
        return this.http.get<Project>(`${this.API_URL}/projects/${id}`);
    }

    createProject(project: Partial<Project>): Observable<Project> {
        return this.http.post<Project>(`${this.API_URL}/projects`, project);
    }

    updateProject(id: number, project: Partial<Project>): Observable<Project> {
        return this.http.put<Project>(`${this.API_URL}/projects/${id}`, project);
    }

    // Allocations
    getGroupedAllocations(page: number = 0, size: number = 10, search?: string, allocationType?: string, managerId?: number): Observable<Page<EmployeeAllocationSummary>> {
        let url = `${this.API_URL}/allocations/grouped?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (allocationType) url += `&allocationType=${encodeURIComponent(allocationType)}`;
        if (managerId) url += `&managerId=${managerId}`;
        return this.http.get<Page<EmployeeAllocationSummary>>(url);
    }

    getAllocations(page: number = 0, size: number = 10, search?: string, allocationType?: string, managerId?: number): Observable<Page<Allocation>> {
        let url = `${this.API_URL}/allocations?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (allocationType) url += `&allocationType=${encodeURIComponent(allocationType)}`;
        if (managerId) url += `&managerId=${managerId}`;
        return this.http.get<Page<Allocation>>(url);
    }

    getAllocationsByEmployee(employeeId: number): Observable<Allocation[]> {
        return this.http.get<Allocation[]>(`${this.API_URL}/allocations/employee/${employeeId}`);
    }

    getAllocationsByProject(projectId: number): Observable<Allocation[]> {
        return this.http.get<Allocation[]>(`${this.API_URL}/allocations/project/${projectId}`);
    }

    createAllocation(allocation: Partial<Allocation>): Observable<Allocation> {
        return this.http.post<Allocation>(`${this.API_URL}/allocations`, allocation);
    }

    updateAllocation(id: number, allocation: Partial<Allocation>): Observable<Allocation> {
        return this.http.put<Allocation>(`${this.API_URL}/allocations/${id}`, allocation);
    }

    deleteAllocation(id: number): Observable<void> {
        return this.http.delete<void>(`${this.API_URL}/allocations/${id}`);
    }
}
