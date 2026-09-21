import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { API_BASE_URL } from './config';
import { ApiResponse, MasterRecord, RecordKind } from './models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="shell">
      <aside [class.open]="menuOpen">
        <div class="logo"><span>E</span><strong>ERP Console</strong></div>
        <nav>
          <button *ngFor="let item of nav" [class.active]="kind()===item.key" (click)="select(item.key)"><span>{{item.icon}}</span>{{item.label}}</button>
          <a class="nav-link" *ngIf="auth.hasAuthority('KAPAS_PURCHASE_READ')" routerLink="/text-extractions"><span>⛏</span>Raw Material Lifecycle</a>
          <a class="nav-link" *ngIf="auth.hasAuthority('KAPAS_PURCHASE_READ')" routerLink="/kapas-purchases"><span>🧺</span>Kapas Purchases</a>
          <a class="nav-link" *ngIf="auth.hasAuthority('BALE_READ')" routerLink="/bales"><span>▤</span>Bale Production</a>
          <a class="nav-link" *ngIf="auth.hasAuthority('SALES_READ')" routerLink="/sales"><span>₹</span>Sales</a>
          <a class="nav-link" *ngIf="auth.hasAuthority('REPORT_READ')" routerLink="/reports"><span>📊</span>Reports</a>
          <a class="nav-link" *ngIf="auth.hasRole('ROLE_SUPER_ADMIN')" routerLink="/users"><span>♙</span>User administration</a>
        </nav>
        <div class="aside-foot">Operations workspace<br><small>Connected to API</small></div>
      </aside>
      <div class="backdrop" (click)="menuOpen=false"></div>
      <section class="content">
        <header>
          <button class="menu" (click)="menuOpen=!menuOpen">☰</button>
          <div><p class="eyebrow">OVERVIEW</p><h1>{{current.label}}</h1></div>
          <div class="profile">
            <button class="profile-trigger" (click)="profileOpen=!profileOpen" [attr.aria-expanded]="profileOpen" aria-label="Open user profile">
              <div class="avatar">{{initials}}</div>
              <span>{{auth.session()?.username || 'Administrator'}}</span>
              <span class="chevron">⌄</span>
            </button>
            <div class="profile-menu" *ngIf="profileOpen">
              <p class="profile-name">{{auth.session()?.username || 'Administrator'}}</p>
              <p class="profile-email">{{auth.session()?.email || 'Email unavailable'}}</p>
              <div class="profile-divider"></div>
              <p class="profile-label">Roles</p>
              <span class="role-chip" *ngFor="let role of roles">{{role}}</span>
              <button class="logout profile-logout" (click)="logout()">Sign out</button>
            </div>
          </div>
        </header>
        <main>
          <div class="welcome">
            <div>
              <p class="eyebrow">GOOD DAY</p>
              <h2>Manage your {{current.label.toLowerCase()}}</h2>
              <p class="muted">Create, update, and review records in one place.</p>
            </div>
            <button class="primary" *ngIf="auth.hasAuthority('MASTER_DATA_WRITE')" (click)="openCreate()">＋ Add {{current.singular}}</button>
          </div>
          <div class="stats">
            <div><span>Total records</span><strong>{{records().length}}</strong></div>
            <div><span>API status</span><strong class="online">● Online</strong></div>
          </div>
          <section class="panel">
            <div class="panel-head">
              <div>
                <h3>{{current.label}}</h3>
                <p class="muted">All active {{current.label.toLowerCase()}} in your organization</p>
              </div>
              <input class="search" placeholder="Search records…" [(ngModel)]="query">
            </div>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Contact</th>
                    <th>Phone</th>
                    <th *ngIf="kind()==='godowns'">Capacity</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let row of filtered">
                    <td><b>{{row.code}}</b></td>
                    <td>{{row.name}}</td>
                    <td>{{row.contactPerson || '—'}}</td>
                    <td>{{row.phone || '—'}}</td>
                    <td *ngIf="kind()==='godowns'">{{row.capacity || '—'}}</td>
                    <td class="actions">
                      <button *ngIf="auth.hasAuthority('MASTER_DATA_WRITE')" (click)="openEdit(row)" aria-label="Edit">Edit</button>
                      <button *ngIf="auth.hasAuthority('MASTER_DATA_WRITE')" class="danger-text" (click)="remove(row)">Delete</button>
                    </td>
                  </tr>
                  <tr *ngIf="!filtered.length"><td colspan="6" class="empty">No records found. Add your first {{current.singular.toLowerCase()}}.</td></tr>
                </tbody>
              </table>
            </div>
          </section>
        </main>
      </section>
    </div>
    <div class="modal-backdrop" *ngIf="editing" (click)="editing=null">
      <section class="modal" (click)="$event.stopPropagation()">
        <button class="close" (click)="editing=null">×</button>
        <p class="eyebrow">{{editing.id ? 'EDIT' : 'NEW'}}</p>
        <h2>{{editing.id ? 'Edit' : 'Add'}} {{current.singular}}</h2>
        <form (ngSubmit)="save()">
          <div class="two">
            <label>Code *<input name="code" [(ngModel)]="editing.code" required></label>
            <label>Name *<input name="name" [(ngModel)]="editing.name" required></label>
          </div>
          <div class="two">
            <label>Contact person<input name="contact" [(ngModel)]="editing.contactPerson"></label>
            <label>Phone<input name="phone" [(ngModel)]="editing.phone"></label>
          </div>
          <label *ngIf="kind()!=='godowns'">Email<input name="email" type="email" [(ngModel)]="editing.email"></label>
          <label *ngIf="kind()==='godowns'">Capacity<input name="capacity" type="number" min="0" [(ngModel)]="editing.capacity"></label>
          <label>Address<textarea name="address" rows="3" [(ngModel)]="editing.address"></textarea></label>
          <div class="modal-actions">
            <button type="button" class="secondary" (click)="editing=null">Cancel</button>
            <button class="primary" [disabled]="saving">{{saving ? 'Saving…' : 'Save record'}}</button>
          </div>
        </form>
      </section>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  nav = [{ key: 'suppliers' as RecordKind, label: 'Suppliers', singular: 'Supplier', icon: '◈' }, { key: 'customers' as RecordKind, label: 'Customers', singular: 'Customer', icon: '◎' }, { key: 'godowns' as RecordKind, label: 'Godowns', singular: 'Godown', icon: '▦' }];
  kind = signal<RecordKind>('suppliers');
  records = signal<MasterRecord[]>([]);
  editing: MasterRecord | null = null;
  query = '';
  saving = false;
  menuOpen = false;
  profileOpen = false;

  constructor(private http: HttpClient, public auth: AuthService, private router: Router) {}

  get current() { return this.nav.find(n => n.key === this.kind()) ?? this.nav[0]; }
  get filtered() {
    const q = this.query.toLowerCase();
    return this.records().filter(r => `${r.code} ${r.name} ${r.contactPerson ?? ''}`.toLowerCase().includes(q));
  }
  get initials() { return (this.auth.session()?.username ?? 'AD').slice(0, 2).toUpperCase(); }
  get roles() { return this.auth.roles().map(role => role.replace(/^ROLE_/, '').replaceAll('_', ' ')); }

  ngOnInit() { this.load(); }
  select(k: RecordKind) { this.kind.set(k); this.menuOpen = false; this.query = ''; this.load(); }
  load() { this.http.get<ApiResponse<MasterRecord[]>>(`${API_BASE_URL}/master-data/${this.kind()}`).subscribe({ next: r => this.records.set(r.data ?? []), error: () => this.records.set([]) }); }
  openCreate() { this.editing = { code: '', name: '' }; }
  openEdit(row: MasterRecord) { this.editing = { ...row }; }
  save() { if (!this.editing) return; this.saving = true; const url = `${API_BASE_URL}/master-data/${this.kind()}${this.editing.id ? `/${this.editing.id}` : ''}`; const req = this.editing.id ? this.http.put<ApiResponse<MasterRecord>>(url, this.editing) : this.http.post<ApiResponse<MasterRecord>>(url, this.editing); req.subscribe({ next: () => { this.editing = null; this.saving = false; this.load(); }, error: () => this.saving = false }); }
  remove(row: MasterRecord) { if (row.id && confirm(`Delete ${row.name}?`)) this.http.delete(`${API_BASE_URL}/master-data/${this.kind()}/${row.id}`).subscribe(() => this.load()); }
  logout() { this.auth.logout()?.subscribe({ complete: () => this.router.navigateByUrl('/login'), error: () => this.router.navigateByUrl('/login') }); }
}
