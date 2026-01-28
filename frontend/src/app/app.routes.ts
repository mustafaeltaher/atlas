import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
    {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    {
        path: 'employees',
        loadComponent: () => import('./pages/employees/employees.component').then(m => m.EmployeesComponent),
        canActivate: [authGuard]
    },
    {
        path: 'projects',
        loadComponent: () => import('./pages/projects/projects.component').then(m => m.ProjectsComponent),
        canActivate: [authGuard]
    },
    {
        path: 'allocations',
        loadComponent: () => import('./pages/allocations/allocations.component').then(m => m.AllocationsComponent),
        canActivate: [authGuard]
    },
    {
        path: 'profile',
        loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent),
        canActivate: [authGuard]
    },
    {
        path: 'reports',
        loadComponent: () => import('./pages/reports/reports').then(m => m.ReportsComponent),
        canActivate: [authGuard]
    },
    { path: '**', redirectTo: '/dashboard' }
];
