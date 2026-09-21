import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

export const permissionGuard = (permission: string) => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasAuthority(permission) ? true : router.createUrlTree(['/']);
};
