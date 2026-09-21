import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

export const superAdminGuard = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasRole('ROLE_SUPER_ADMIN') ? true : router.createUrlTree(['/']);
};
