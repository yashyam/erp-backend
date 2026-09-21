import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { API_BASE_URL } from './config';

interface Report {
  summary: {
    purchaseCount: number;
    totalBags: number;
    totalNetWeight: number;
    averageRate: number;
    totalPurchaseValue: number;
  };
  supplierWise: any[];
  godownWise: any[];
  lotWise: any[];
}

@Component({
  selector: 'erp-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="reports-page">
      <header class="reports-header">
        <div>
          <p class="eyebrow">INSIGHTS</p>
          <h1>Kapas Purchase Reports</h1>
          <p>Operational totals and traceability summaries for active purchase records.</p>
        </div>
        <a routerLink="/kapas-purchases">Open purchase history</a>
      </header>

      <section class="filters-panel">
        <div class="filter-row">
          <label>
            Supplier
            <input list="supplier-options" [(ngModel)]="filters.supplierName" (change)="applyFilters()" placeholder="Type or select supplier" />
            <datalist id="supplier-options">
              <option *ngFor="let supplier of supplierOptions" [value]="supplier"></option>
            </datalist>
          </label>

          <label>
            Lot
            <input list="lot-options" [(ngModel)]="filters.lotNumber" (change)="applyFilters()" placeholder="Type or select lot" />
            <datalist id="lot-options">
              <option *ngFor="let lot of lotOptions" [value]="lot"></option>
            </datalist>
          </label>

          <label>
            Preset
            <select [(ngModel)]="filters.preset" (change)="onPresetChange()">
              <option value="all">All time</option>
              <option value="30">Last 30 days</option>
              <option value="60">Last 60 days</option>
              <option value="90">Last 90 days</option>
              <option value="custom">Custom range</option>
            </select>
          </label>

          <label>
            From date
            <input type="date" [max]="today" [(ngModel)]="filters.fromDate" (change)="applyFilters()" />
          </label>

          <label>
            To date
            <input type="date" [max]="today" [(ngModel)]="filters.toDate" (change)="applyFilters()" />
          </label>
        </div>
      </section>

      <section class="metrics">
        <article><span>Purchases</span><strong>{{ report?.summary?.purchaseCount ?? 0 }}</strong></article>
        <article><span>Total bags</span><strong>{{ report?.summary?.totalBags ?? 0 }}</strong></article>
        <article><span>Net kapas</span><strong>{{ report?.summary?.totalNetWeight ?? 0 }} kg</strong></article>
        <article><span>Purchase value</span><strong>₹{{ report?.summary?.totalPurchaseValue ?? 0 | number:'1.2-2' }}</strong></article>
        <article><span>Average rate</span><strong>₹{{ report?.summary?.averageRate ?? 0 | number:'1.2-2' }}</strong></article>
      </section>

      <section class="report-grid">
        <article class="report-card">
          <h2>Supplier-wise purchase</h2>
          <table>
            <thead><tr><th>Supplier</th><th>Bags</th><th>Net kg</th><th>Avg rate</th><th>Value</th></tr></thead>
            <tbody>
              <tr *ngFor="let row of report?.supplierWise">
                <td>{{ row.supplier || 'Unassigned' }}</td>
                <td>{{ row.totalBags }}</td>
                <td>{{ row.totalNetWeight }}</td>
                <td>₹{{ row.averageRate | number:'1.2-2' }}</td>
                <td>₹{{ row.totalPurchaseValue | number:'1.2-2' }}</td>
              </tr>
            </tbody>
          </table>
        </article>

        <article class="report-card">
          <h2>Godown-wise kapas</h2>
          <table>
            <thead><tr><th>Godown</th><th>Bags</th><th>Net kg</th><th>Lots</th><th>Value</th></tr></thead>
            <tbody>
              <tr *ngFor="let row of report?.godownWise">
                <td>{{ row.godown || 'Unassigned' }}</td>
                <td>{{ row.totalBags }}</td>
                <td>{{ row.totalNetWeight }}</td>
                <td>{{ row.lotCount }}</td>
                <td>₹{{ row.totalPurchaseValue | number:'1.2-2' }}</td>
              </tr>
            </tbody>
          </table>
        </article>
      </section>

      <section class="report-card">
        <h2>Lot-wise purchase traceability</h2>
        <table>
          <thead><tr><th>Lot</th><th>Date</th><th>Supplier</th><th>Net kg</th><th>Rate</th><th>Godown</th></tr></thead>
          <tbody>
            <tr *ngFor="let row of report?.lotWise">
              <td><b>{{ row.lotNumber || '—' }}</b></td>
              <td>{{ row.date | date:'dd MMM yyyy' }}</td>
              <td>{{ row.supplier || 'Unassigned' }}</td>
              <td>{{ row.netWeight }}</td>
              <td>₹{{ row.rate | number:'1.2-2' }}</td>
              <td>{{ row.godown || 'Unassigned' }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  `,
  styles: [
    `.reports-page{max-width:1400px;margin:0 auto;padding:34px 42px;color:#172033;background:#f5f7fb;min-height:100vh;font-family:'DM Sans',Arial}`,
    `.reports-header{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:28px}`,
    `.reports-header h1{margin:.2rem 0;font-size:30px}`,
    `.reports-header p{color:#64748b}`,
    `.reports-header a{background:#2563eb;color:white;text-decoration:none;padding:11px 16px;border-radius:8px;font-weight:600}`,
    `.eyebrow{font-size:11px;letter-spacing:1.5px;color:#8291a8;font-weight:700}`,
    `.filters-panel{background:#fff;border:1px solid #e1e7ef;border-radius:12px;padding:18px;margin-bottom:24px;box-shadow:0 8px 24px #2538580b}`,
    `.filter-row{display:grid;grid-template-columns:repeat(5,minmax(150px,1fr));gap:16px}`,
    `.filter-row label{display:flex;flex-direction:column;gap:8px;font-size:13px;color:#475569;font-weight:600}`,
    `.filter-row select,.filter-row input{padding:10px 12px;border:1px solid #d9e2ec;border-radius:8px;background:#fff;font:inherit}`,
    `.metrics{display:grid;grid-template-columns:repeat(5,1fr);gap:14px;margin-bottom:22px}`,
    `.metrics article,.report-card{background:#fff;border:1px solid #e1e7ef;border-radius:10px;padding:18px;box-shadow:0 8px 24px #2538580b}`,
    `.metrics span{display:block;color:#64748b;font-size:13px}`,
    `.metrics strong{display:block;margin-top:9px;font-size:22px}`,
    `.report-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin-bottom:18px}`,
    `.report-card h2{font-size:17px;margin:0 0 16px}`,
    `.report-card{overflow:auto}`,
    `table{width:100%;border-collapse:collapse;font-size:13px}`,
    `th,td{text-align:left;padding:11px 8px;border-bottom:1px solid #edf1f5;white-space:nowrap}`,
    `th{color:#64748b;font-size:11px;text-transform:uppercase;letter-spacing:.05em}`,
    `@media(max-width:1000px){.metrics{grid-template-columns:repeat(3,1fr)}.report-grid{grid-template-columns:1fr}.filter-row{grid-template-columns:repeat(2,minmax(150px,1fr))}}`,
    `@media(max-width:600px){.reports-page{padding:24px 16px}.reports-header{display:block}.reports-header a{display:inline-block;margin-top:12px}.metrics{grid-template-columns:repeat(2,1fr)}.filter-row{grid-template-columns:1fr}}`
  ]
})
export class ReportsComponent implements OnInit {
  private http = inject(HttpClient);
  report?: Report;
  supplierOptions: string[] = [];
  lotOptions: string[] = [];
  today = this.toInputDate(new Date());
  private optionSource?: Report;
  filters = {
    supplierName: '',
    lotNumber: '',
    fromDate: '',
    toDate: '',
    preset: 'all'
  };

  ngOnInit() {
    this.load();
  }

  onPresetChange() {
    if (this.filters.preset === 'all') {
      this.filters.fromDate = '';
      this.filters.toDate = '';
      this.applyFilters();
      return;
    }

    if (this.filters.preset === 'custom') {
      this.applyFilters();
      return;
    }

    const days = Number(this.filters.preset);
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - days + 1);
    this.filters.toDate = this.toInputDate(to);
    this.filters.fromDate = this.toInputDate(from);
    this.applyFilters();
  }

  applyFilters() {
    this.normalizeDates();
    const params = this.buildParams();
    this.http.get<any>(`${API_BASE_URL}/reports/kapas-purchases`, { params }).subscribe({
      next: response => {
        this.report = response?.data ?? response;
        if (!this.optionSource) {
          this.optionSource = this.report;
          this.updateOptions();
        }
      },
      error: () => {
        this.report = undefined;
      }
    });
  }

  private load() {
    this.applyFilters();
  }

  private buildParams(): HttpParams {
    let params = new HttpParams();
    if (this.filters.supplierName) params = params.set('supplierName', this.filters.supplierName.trim());
    if (this.filters.lotNumber) params = params.set('lotNumber', this.filters.lotNumber.trim());
    if (this.filters.fromDate) params = params.set('fromDate', this.filters.fromDate);
    if (this.filters.toDate) params = params.set('toDate', this.filters.toDate);
    return params;
  }

  private normalizeDates() {
    if (this.filters.fromDate > this.today) {
      this.filters.fromDate = this.today;
    }
    if (this.filters.toDate > this.today) {
      this.filters.toDate = this.today;
    }
  }

  private updateOptions() {
    const supplierValues = this.optionSource?.supplierWise?.map((row: any) => row.supplier).filter(Boolean) ?? [];
    const lotValues = this.optionSource?.lotWise?.map((row: any) => row.lotNumber).filter(Boolean) ?? [];
    this.supplierOptions = [...new Set(supplierValues)];
    this.lotOptions = [...new Set(lotValues)];
  }

  private toInputDate(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
