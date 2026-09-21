import { Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { DashboardComponent } from './dashboard.component';
import { authGuard } from './auth.guard';
import { TextExtractionsComponent } from './text-extractions.component';
import { KapasPurchasesComponent } from './kapas-purchases.component';
import { ReportsComponent } from './reports.component';
import { UsersComponent } from './users.component';
import { superAdminGuard } from './super-admin.guard';
import { permissionGuard } from './permission.guard';
import { SalesComponent } from './sales.component';
import { BaleProductionComponent } from './bale-production.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'text-extractions', canActivate: [authGuard, permissionGuard('KAPAS_PURCHASE_READ')], component: TextExtractionsComponent },
  { path: 'kapas-purchases', canActivate: [authGuard, permissionGuard('KAPAS_PURCHASE_READ')], component: KapasPurchasesComponent },
  { path: 'bales', canActivate: [authGuard, permissionGuard('BALE_READ')], component: BaleProductionComponent },
  { path: 'sales', canActivate: [authGuard, permissionGuard('SALES_READ')], component: SalesComponent },
  { path: 'reports', canActivate: [authGuard, permissionGuard('REPORT_READ')], component: ReportsComponent },
  { path: 'users', canActivate: [authGuard, superAdminGuard], component: UsersComponent },
  { path: '', canActivate: [authGuard], component: DashboardComponent },
  { path: '**', redirectTo: '' }
];
