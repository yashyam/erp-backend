import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_BASE_URL } from './config';
import { ApiResponse } from './models';

interface User { id: string; username: string; email: string; firstName?: string; lastName?: string; status: string; enabled: boolean; roles: string[]; }

@Component({
  selector: 'erp-users',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="users-page">
      <header><div><p class="eyebrow">ADMINISTRATION</p><h1>User administration</h1><p>Assign operational roles to users.</p></div><a routerLink="/">Back to dashboard</a></header>
      <p class="error" *ngIf="error">{{error}}</p>
      <p class="hint">Role changes take effect for the managed user on their next login. If your own super-admin access was just granted, sign out and sign in again.</p>
      <section class="card"><input class="search" placeholder="Search users…" [(ngModel)]="query">
        <table><thead><tr><th>User</th><th>Email</th><th>Status</th><th>Roles</th><th></th></tr></thead>
        <tbody><tr *ngFor="let user of filtered"><td><b>{{user.username}}</b><br><small>{{user.firstName}} {{user.lastName}}</small></td><td>{{user.email}}</td><td>{{user.status}}</td><td>{{user.roles.join(', ')}}</td><td><button (click)="edit(user)">Manage roles</button></td></tr><tr *ngIf="!filtered.length"><td colspan="5">No users found.</td></tr></tbody></table>
      </section>
      <div class="backdrop" *ngIf="editing"><section class="modal"><h2>Roles for {{editing.username}}</h2><label *ngFor="let role of roles"><input type="checkbox" [checked]="selected.has(role)" (change)="toggle(role, $event)"> {{role}}</label><p class="error" *ngIf="error">{{error}}</p><button class="primary" (click)="save()" [disabled]="saving">{{saving ? 'Saving…' : 'Save roles'}}</button><button (click)="editing=undefined">Cancel</button></section></div>
    </main>`,
  styles: [`.users-page{max-width:1200px;margin:0 auto;padding:34px 42px;font-family:Arial;color:#172033}.users-page header{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:24px}.users-page header a{background:#2563eb;color:#fff;padding:10px 14px;border-radius:6px;text-decoration:none}.eyebrow{font-size:11px;letter-spacing:1.5px;color:#8291a8;font-weight:700}.card{background:#fff;border:1px solid #e1e7ef;border-radius:10px;padding:18px;overflow:auto}.search{padding:10px;width:280px;margin-bottom:16px}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:12px 8px;border-bottom:1px solid #edf1f5}button{border:0;border-radius:5px;padding:9px 12px;background:#2563eb;color:white;cursor:pointer;margin:4px}.primary{background:#166534}.error{color:#b91c1c}.hint{color:#64748b;font-size:13px}.backdrop{position:fixed;inset:0;background:#17203366;display:grid;place-items:center}.modal{background:#fff;padding:24px;border-radius:10px;min-width:320px}.modal label{display:block;padding:9px 0}@media(max-width:600px){.users-page{padding:24px 16px}.users-page header{display:block}.users-page header a{display:inline-block;margin-top:12px}}`]
})
export class UsersComponent {
  private http = inject(HttpClient);
  users: User[] = [];
  roles = ['ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_WORKER', 'ROLE_TECHNICAL_SUPPORT', 'ROLE_SUPER_ADMIN'];
  query = ''; editing?: User; selected = new Set<string>(); saving = false; error = '';

  ngOnInit() { this.load(); }
  get filtered() { const query = this.query.toLowerCase(); return this.users.filter(user => `${user.username} ${user.email} ${user.roles.join(' ')}`.toLowerCase().includes(query)); }
  load() {
    this.http.get<ApiResponse<User[]>>(`${API_BASE_URL}/users`).subscribe({
      next: response => { this.users = response.data ?? []; this.error = ''; },
      error: error => this.error = error.status === 403
        ? 'User administration requires a fresh ROLE_SUPER_ADMIN session. Sign out and sign in again as a super admin.'
        : error?.error?.error?.message ?? 'Could not load users'
    });
  }
  edit(user: User) { this.editing = user; this.selected = new Set(user.roles); this.error = ''; }
  toggle(role: string, event: Event) { const checked = (event.target as HTMLInputElement).checked; checked ? this.selected.add(role) : this.selected.delete(role); }
  save() {
    if (!this.editing || !this.selected.size) { this.error = 'Select at least one role'; return; }
    this.saving = true;
    this.http.put<ApiResponse<User>>(`${API_BASE_URL}/users/${this.editing.id}/roles`, { roles: [...this.selected] }).subscribe({
      next: response => { this.users = this.users.map(user => user.id === response.data.id ? response.data : user); this.editing = undefined; this.saving = false; },
      error: error => {
        this.error = error.status === 403
          ? 'Your session is not authorized to manage roles. Sign out and sign in again as a super admin.'
          : error?.error?.error?.message ?? 'Could not update roles';
        this.saving = false;
      }
    });
  }
}
