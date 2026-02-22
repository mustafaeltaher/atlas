import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DelegateResponse, DelegateRequest, LoginResponse, ImpersonateRequest } from '../models';

@Injectable({
    providedIn: 'root'
})
export class DelegateService {
    private apiUrl = '/api/delegates';
    private authUrl = '/api/auth';

    constructor(private http: HttpClient) { }

    getMyDelegates(): Observable<DelegateResponse[]> {
        return this.http.get<DelegateResponse[]>(`${this.apiUrl}/my-delegates`);
    }

    getAvailableAccounts(): Observable<DelegateResponse[]> {
        return this.http.get<DelegateResponse[]>(`${this.apiUrl}/available-accounts`);
    }

    getPotentialDelegates(search: string = ''): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/potential?search=${search}`);
    }

    grantAccess(request: DelegateRequest): Observable<DelegateResponse> {
        return this.http.post<DelegateResponse>(this.apiUrl, request);
    }

    revokeAccess(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    impersonate(request: ImpersonateRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.authUrl}/impersonate`, request);
    }
}
