import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { API_BASE_URL } from './config';
import { ApiResponse, Session } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly key = 'erp_session';
  readonly session = signal<Session | null>(this.read());
  constructor(private http: HttpClient) {}
  login(emailOrUsername: string, password: string) {
    return this.http.post<ApiResponse<Session>>(`${API_BASE_URL}/auth/login`, { emailOrUsername, password }).pipe(tap(r => this.save(r.data)));
  }
  loginWithGoogle(idToken: string) {
    return this.http.post<ApiResponse<Session>>(`${API_BASE_URL}/auth/google`, { idToken }).pipe(tap(r => this.save(r.data)));
  }
  register(payload: Record<string, string>) { return this.http.post<ApiResponse<unknown>>(`${API_BASE_URL}/auth/register`, payload); }
  logout() { const token = this.session()?.accessToken; this.clear(); return token ? this.http.post(`${API_BASE_URL}/auth/logout`, {}) : undefined; }
  clear() { localStorage.removeItem(this.key); this.session.set(null); }
  hasRole(role: string) {
    return this.hasAuthority(role);
  }
  hasAuthority(authority: string) {
    const token = this.session()?.accessToken;
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as { roles?: string[] };
      return (payload.roles ?? []).includes(authority);
    } catch {
      return false;
    }
  }
  roles(): string[] {
    const token = this.session()?.accessToken;
    if (!token) return [];
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as { roles?: string[] };
      return (payload.roles ?? []).filter(role => role.startsWith('ROLE_'));
    } catch {
      return [];
    }
  }
  private save(value: Session) { localStorage.setItem(this.key, JSON.stringify(value)); this.session.set(value); }
  private read(): Session | null { try { return JSON.parse(localStorage.getItem(this.key) ?? 'null') as Session | null; } catch { return null; } }
}
