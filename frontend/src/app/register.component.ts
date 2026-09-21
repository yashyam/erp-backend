import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `<main class="auth-page"><section class="auth-card"><div class="brand-mark">E</div><h1>Create account</h1><p class="muted">Set up your ERP workspace</p>
    <form (ngSubmit)="submit()"><div class="two"><label>First name<input name="firstName" [(ngModel)]="form.firstName"></label><label>Last name<input name="lastName" [(ngModel)]="form.lastName"></label></div><label>Username<input name="username" [(ngModel)]="form.username" required></label><label>Email<input name="email" type="email" [(ngModel)]="form.email" required></label><label>Password<input name="password" type="password" [(ngModel)]="form.password" minlength="8" required></label><p class="error" *ngIf="error">{{error}}</p><button class="primary wide" [disabled]="busy">{{busy ? 'Creating…' : 'Create account'}}</button></form><p class="switch">Already registered? <a routerLink="/login">Sign in</a></p></section></main>`,
  styles: [`.auth-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:linear-gradient(135deg,#eaf2ff,#f8fafc)}.auth-card{background:#fff;width:min(470px,100%);padding:42px;border-radius:20px;box-shadow:0 18px 50px #25385818}.brand-mark{background:#2563eb;color:#fff;width:42px;height:42px;display:grid;place-items:center;border-radius:12px;font-weight:800;font-size:22px;margin-bottom:22px}h1{margin:0 0 6px}.muted,.switch{color:#64748b}.switch{text-align:center;margin-bottom:0}.switch a{color:#2563eb;font-weight:600}.wide{width:100%;margin-top:10px}.error{color:#b91c1c;font-size:13px}.two{display:grid;grid-template-columns:1fr 1fr;gap:14px}@media(max-width:480px){.two{grid-template-columns:1fr}}`]
})
export class RegisterComponent {
  form = { firstName: '', lastName: '', username: '', email: '', password: '', phone: '' }; error = ''; busy = false;
  constructor(private auth: AuthService, private router: Router) {}
  submit() { this.busy = true; this.auth.register(this.form).subscribe({ next: () => this.router.navigateByUrl('/login'), error: e => { this.error = e.error?.error?.message ?? 'Registration failed.'; this.busy = false; } }); }
}
