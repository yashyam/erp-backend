import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { GOOGLE_CLIENT_ID } from './config';

@Component({
  standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `<main class="auth-page"><section class="auth-card"><div class="brand-mark">E</div><h1>Welcome back</h1><p class="muted">Sign in to your ERP workspace</p>
    <form (ngSubmit)="submit()"><label>Email or username<input name="identity" [(ngModel)]="identity" required autocomplete="username"></label><label>Password<input name="password" type="password" [(ngModel)]="password" required autocomplete="current-password"></label><p class="error" *ngIf="error">{{error}}</p><button class="primary wide" [disabled]="busy">{{busy ? 'Signing in…' : 'Sign in'}}</button></form><div id="google-login" class="google-login"></div><p class="oauth-note">Google sign-in is available when configured by the administrator.</p>
    <p class="switch">New here? <a routerLink="/register">Create an account</a></p></section></main>`,
  styles: [`.auth-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:linear-gradient(135deg,#eaf2ff,#f8fafc)}.auth-card{background:#fff;width:min(420px,100%);padding:42px;border-radius:20px;box-shadow:0 18px 50px #25385818}.brand-mark{background:#2563eb;color:#fff;width:42px;height:42px;display:grid;place-items:center;border-radius:12px;font-weight:800;font-size:22px;margin-bottom:22px}h1{margin:0 0 6px}.muted,.switch,.oauth-note{color:#64748b}.switch{text-align:center;margin-bottom:0}.switch a{color:#2563eb;font-weight:600}.wide{width:100%;margin-top:10px}.error{color:#b91c1c;font-size:13px}.google-login{margin-top:18px;display:flex;justify-content:center}.oauth-note{text-align:center;font-size:12px}`]
})
export class LoginComponent {
  identity = ''; password = ''; error = ''; busy = false;
  constructor(private auth: AuthService, private router: Router) {}
  ngAfterViewInit() {
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.onload = () => {
      const google = (window as any).google;
      if (!google) return;
      if (!GOOGLE_CLIENT_ID) return;
      google.accounts.id.initialize({ client_id: GOOGLE_CLIENT_ID, callback: (response: { credential: string }) => this.googleLogin(response.credential) });
      google.accounts.id.renderButton(document.getElementById('google-login'), { theme: 'outline', size: 'large', width: 330 });
    };
    document.head.appendChild(script);
  }
  googleLogin(idToken: string) {
    this.busy = true; this.error = '';
    this.auth.loginWithGoogle(idToken).subscribe({ next: () => this.router.navigateByUrl('/'), error: e => { this.error = e.error?.error?.message ?? 'Unable to sign in with Google.'; this.busy = false; } });
  }
  submit() { this.busy = true; this.error = ''; this.auth.login(this.identity, this.password).subscribe({ next: () => this.router.navigateByUrl('/'), error: e => { this.error = e.error?.error?.message ?? 'Unable to sign in. Check your details.'; this.busy = false; } }); }
}
