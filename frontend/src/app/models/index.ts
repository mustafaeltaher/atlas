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
    costCenter: string;
    nationality: string;
    hireDate: string;
    email: string;
    parentTower: string;
    tower: string;
    managerId?: number;
    managerName?: string;
    isActive: boolean;
    status: 'ACTIVE' | 'MATERNITY' | 'LONG_LEAVE' | 'RESIGNED';
    totalAllocation: number;
    allocationStatus: 'ACTIVE' | 'BENCH' | 'PROSPECT' | null;
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
    status: 'ACTIVE' | 'COMPLETED' | 'ON_HOLD';
    managerName?: string;
    allocatedEmployees: number;
    averageAllocation: number;
}

export interface MonthlyAllocation {
    id?: number;
    allocationId?: number;
    year: number;
    month: number;
    percentage: number;
}

export interface Allocation {
    id: number;
    employeeId: number;
    employeeName: string;
    employeeOracleId: string;
    projectId: number;
    projectName: string;
    startDate: string;
    endDate: string;
    status: 'ACTIVE' | 'PROSPECT';
    currentMonthAllocation: number | null;
    allocationPercentage: number;
    year?: number;
    monthlyAllocations?: MonthlyAllocation[];
}

export interface EmployeeAllocationSummary {
    employeeId: number;
    employeeName: string;
    employeeOracleId: string;
    totalAllocationPercentage: number;
    projectCount: number;
    allocations: Allocation[];
}

export interface Manager {
    id: number;
    name: string;
    oracleId?: string;
}

export interface DashboardStats {
    totalEmployees: number;
    activeEmployees: number;
    averageAllocation: number;
    benchCount: number;
    prospectCount: number;
    activeProjects: number;
    completedProjects: number;
    onHoldProjects: number;
    employeeTrend: number;
    allocationTrend: number;
    benchTrend: number;
    projectTrend: number;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}
