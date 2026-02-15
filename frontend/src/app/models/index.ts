export interface User {
    username: string;
    email: string;
    isTopLevel: boolean;
    employeeName: string;
    employeeId?: number;
    isImpersonating?: boolean;
    impersonatorUsername?: string;
}

export interface LoginResponse {
    token: string;
    username: string;
    email: string;
    isTopLevel: boolean;
    employeeName: string;
    employeeId?: number;
    isImpersonating?: boolean;
    impersonatorUsername?: string;
}

export interface DelegateResponse {
    id: number;
    delegatorName: string;
    delegatorUsername: string;
    delegateName: string;
    delegateUsername: string;
    createdAt: string;
}

export interface DelegateRequest {
    delegateUsername: string;
}

export interface ImpersonateRequest {
    targetUsername: string;
}

export interface EmployeeSkill {
    skillName: string;
    skillLevel: 'PRIMARY' | 'SECONDARY';
    skillGrade: 'ADVANCED' | 'INTERMEDIATE' | 'BEGINNER';
}

export interface Employee {
    id: number;
    oracleId: number;
    name: string;
    gender: string;
    grade: string;
    jobLevel: string;
    title: string;
    hiringType: string;
    location: string;
    legalEntity: string;
    costCenter: string;
    nationality: string;
    hireDate: string;
    resignationDate?: string;
    reasonOfLeave?: string;
    email: string;
    towerId?: number;
    towerName: string;
    parentTowerName: string;
    managerId?: number;
    managerName?: string;
    skills: EmployeeSkill[];
    status: 'ACTIVE' | 'MATERNITY' | 'VACATION' | 'RESIGNED';
    totalAllocation: number;
    allocationStatus: 'ACTIVE' | 'BENCH' | 'PROSPECT' | null;
}

export interface Project {
    id: number;
    projectId: string;
    description: string;
    projectType: 'PROJECT' | 'OPPORTUNITY';
    region: string;
    vertical: string;
    startDate: string;
    endDate: string;
    status: 'ACTIVE' | 'COMPLETED' | 'ON_HOLD';
    managerId?: number;
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
    projectId: number | null;
    projectName: string | null;
    startDate: string;
    endDate: string;
    allocationType: 'PROJECT' | 'PROSPECT' | 'VACATION' | 'MATERNITY';
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
