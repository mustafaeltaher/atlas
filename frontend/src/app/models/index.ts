export interface User {
    username: string;
    email: string;
    role: 'SYSTEM_ADMIN' | 'EXECUTIVE' | 'HEAD' | 'DEPARTMENT_MANAGER' | 'TEAM_LEAD';
    managerLevel: number;
    employeeId?: number;
}

export interface LoginResponse {
    token: string;
    username: string;
    email: string;
    role: string;
    managerLevel: number;
    employeeId?: number;
}

export interface Employee {
    id: number;
    oracleId: string;
    name: string;
    gender: string;
    grade: string;
    jobLevel: string;
    title: string;
    primarySkill: string;
    secondarySkill: string;
    hiringType: string;
    location: string;
    legalEntity: string;
    nationality: string;
    hireDate: string;
    email: string;
    parentTower: string;
    tower: string;
    managerName?: string;
    isActive: boolean;
    currentUtilization: number;
    utilizationStatus: 'ACTIVE' | 'BENCH' | 'PROSPECT';
}

export interface Project {
    id: number;
    projectId: string;
    name: string;
    description: string;
    parentTower: string;
    tower: string;
    startDate: string;
    endDate: string;
    status: 'ACTIVE' | 'PENDING' | 'COMPLETED' | 'ON_HOLD';
    managerName?: string;
    allocatedEmployees: number;
    averageUtilization: number;
}

export interface Allocation {
    id: number;
    employeeId: number;
    employeeName: string;
    projectId: number;
    projectName: string;
    confirmedAssignment: string;
    startDate: string;
    endDate: string;
    status: 'ACTIVE' | 'PENDING' | 'COMPLETED';
    currentMonthUtilization: string;
    utilizationPercentage: number;
}

export interface DashboardStats {
    totalEmployees: number;
    activeEmployees: number;
    averageUtilization: number;
    benchCount: number;
    prospectCount: number;
    activeProjects: number;
    pendingProjects: number;
    employeeTrend: number;
    utilizationTrend: number;
    benchTrend: number;
    projectTrend: number;
}
