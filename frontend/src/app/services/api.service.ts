import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, Project, Allocation, DashboardStats } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
    private readonly API_URL = '/api';

    constructor(private http: HttpClient) { }

    // Dashboard
    getDashboardStats(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.API_URL}/dashboard/stats`);
    }

    // Employees
    getEmployees(): Observable<Employee[]> {
        return this.http.get<Employee[]>(`${this.API_URL}/employees`);
    }

    getEmployee(id: number): Observable<Employee> {
        return this.http.get<Employee>(`${this.API_URL}/employees/${id}`);
    }

    importEmployees(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.API_URL}/employees/import`, formData);
    }

    // Projects
    getProjects(): Observable<Project[]> {
        return this.http.get<Project[]>(`${this.API_URL}/projects`);
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
    getAllocations(): Observable<Allocation[]> {
        return this.http.get<Allocation[]>(`${this.API_URL}/allocations`);
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
