import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, Project, Allocation, DashboardStats, Page, Manager } from '../models';

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
    getEmployees(page: number = 0, size: number = 10, search?: string, managerId?: number): Observable<Page<Employee>> {
        let url = `${this.API_URL}/employees?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (managerId) url += `&managerId=${managerId}`;
        return this.http.get<Page<Employee>>(url);
    }

    getEmployee(id: number): Observable<Employee> {
        return this.http.get<Employee>(`${this.API_URL}/employees/${id}`);
    }

    getManagers(): Observable<Manager[]> {
        return this.http.get<Manager[]>(`${this.API_URL}/employees/managers`);
    }

    importEmployees(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.API_URL}/employees/import`, formData);
    }

    // Projects
    getProjects(page: number = 0, size: number = 10, search?: string, tower?: string, status?: string): Observable<Page<Project>> {
        let url = `${this.API_URL}/projects?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (tower) url += `&tower=${encodeURIComponent(tower)}`;
        if (status) url += `&status=${encodeURIComponent(status)}`;
        return this.http.get<Page<Project>>(url);
    }

    getProjectTowers(): Observable<string[]> {
        return this.http.get<string[]>(`${this.API_URL}/projects/towers`);
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
    getAllocations(page: number = 0, size: number = 10, search?: string, status?: string, managerId?: number): Observable<Page<Allocation>> {
        let url = `${this.API_URL}/allocations?page=${page}&size=${size}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (status) url += `&status=${encodeURIComponent(status)}`;
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
