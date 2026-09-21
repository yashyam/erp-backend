import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { API_BASE_URL } from './config';
import { ApiResponse } from './models';
import { AuthService } from './auth.service';

interface LotOption {
  id: string;
  lotNumber?: string;
  supplierName?: string;
  billDate?: string;
  netKapasWeight?: number;
  amount?: number;
  lotStatus?: string;
}

interface BaleItem {
  id?: string;
  baleNumber?: string;
  lotId?: string;
  lotNumber: string;
  lotStatus?: string;
  productionDate?: string;
  serialNo?: number;
  baleWeight?: number;
  candy?: number;
  quintals?: number;
}

interface DailySummary {
  productionDate?: string;
  lotNumber?: string;
  totalBales?: number;
  startSerialNo?: number;
  endSerialNo?: number;
  firstBaleNumber?: string;
  lastBaleNumber?: string;
  totalBaleWeight?: number;
  totalQuintals?: number;
  totalCandy?: number;
}

@Component({
  selector: 'erp-bales',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page">
      <h1>Bale production</h1>
      <p class="muted"><a routerLink="/">← Back to dashboard</a> · Record bale lots and daily production summaries.</p>

      <section class="card">
        <h2>Create bale</h2>
        <div class="grid">
          <label>Lot *
            <select [(ngModel)]="editing.lotId" (ngModelChange)="selectLot($event)">
              <option value="">Select a lot</option>
              <option *ngFor="let lot of lots" [value]="lot.id">{{lot.lotNumber || 'Unknown lot'}} · {{lot.supplierName || 'Supplier'}} · {{lot.billDate || '—'}}</option>
            </select>
          </label>
          <label>Lot number<input [(ngModel)]="editing.lotNumber" readonly></label>
          <label>Production date *<input type="date" [(ngModel)]="editing.productionDate"></label>
          <label>Bale weight (kg) *<input type="number" min="0.001" step="0.001" [(ngModel)]="editing.baleWeight"></label>
          <label>Candy<input type="number" min="0" step="0.001" [(ngModel)]="editing.candy"></label>
          <label>Quintals<input type="number" min="0" step="0.001" [(ngModel)]="editing.quintals"></label>
        </div>
        <div class="trace" *ngIf="selectedLot">
          <strong>Selected lot</strong>
          <span>Lot: {{selectedLot.lotNumber || '—'}}</span>
          <span>Supplier: {{selectedLot.supplierName || '—'}}</span>
          <span>Status: {{selectedLot.lotStatus || 'YET_TO_START'}}</span>
          <span>Net weight: {{selectedLot.netKapasWeight ?? '—'}} kg</span>
        </div>
        <p class="error" *ngIf="error">{{error}}</p>
        <button (click)="save()" [disabled]="saving">{{saving ? 'Saving…' : 'Save bale'}}</button>
        <button class="secondary" (click)="reset()">Clear</button>
      </section>

      <section class="card">
        <div class="heading">
          <div><h2>Daily summary</h2><p class="muted">Production totals by lot and date.</p></div>
          <button class="secondary" (click)="loadSummary()">Refresh</button>
        </div>
        <div class="lookup">
          <label>From <input type="date" [(ngModel)]="fromDate"></label>
          <label>To <input type="date" [(ngModel)]="toDate"></label>
          <button (click)="loadSummary()">Load</button>
        </div>
        <table>
          <thead>
            <tr><th>Date</th><th>Lot</th><th>Bales</th><th>Serial range</th><th>Bale number range</th><th>Weight</th><th>Quintals</th><th>Candy</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of summary">
              <td>{{row.productionDate}}</td>
              <td>{{row.lotNumber || '—'}}</td>
              <td>{{row.totalBales ?? 0}}</td>
              <td>{{row.startSerialNo ?? 0}} - {{row.endSerialNo ?? 0}}</td>
              <td>{{row.firstBaleNumber || '—'}} - {{row.lastBaleNumber || '—'}}</td>
              <td>{{row.totalBaleWeight ?? 0}}</td>
              <td>{{row.totalQuintals ?? 0}}</td>
              <td>{{row.totalCandy ?? 0}}</td>
            </tr>
            <tr *ngIf="!summary.length"><td colspan="8" class="empty">No bale production for the selected range.</td></tr>
          </tbody>
        </table>
      </section>

      <section class="card">
        <div class="heading"><div><h2>Bale register</h2><p class="muted">Latest production entries.</p></div></div>
        <table>
          <thead>
            <tr><th>Bale No.</th><th>Lot</th><th>Date</th><th>S.No</th><th>Weight</th><th>Candy</th><th>Quintals</th><th></th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of items">
              <td>{{item.baleNumber || '—'}}</td>
              <td>{{item.lotNumber || '—'}}</td>
              <td>{{item.productionDate || '—'}}</td>
              <td>{{item.serialNo ?? '—'}}</td>
              <td>{{item.baleWeight ?? '—'}}</td>
              <td>{{item.candy ?? '—'}}</td>
              <td>{{item.quintals ?? '—'}}</td>
              <td><button *ngIf="auth.hasAuthority('BALE_DELETE')" class="danger-text" (click)="reverse(item)">Reverse</button></td>
            </tr>
            <tr *ngIf="!items.length"><td colspan="8" class="empty">No bales recorded yet.</td></tr>
          </tbody>
        </table>
      </section>
    </div>
  `,
  styles: [`
    .page{max-width:1320px;margin:2rem auto;padding:0 1rem;font-family:Arial}.card{background:#fff;border:1px solid #ddd;border-radius:8px;padding:1rem;margin:1rem 0}.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:.8rem}.grid label,label{display:flex;flex-direction:column;gap:.25rem;margin:.45rem 0;font-size:.85rem;font-weight:600;color:#475569}input,select,textarea{padding:.6rem;border:1px solid #bbb;border-radius:4px}button{background:#1769aa;color:#fff;border:0;border-radius:4px;padding:.55rem .9rem;cursor:pointer;margin:.25rem}button:disabled{opacity:.5}.secondary{background:#eef2f7;color:#334155}.heading,.lookup{display:flex;align-items:center;gap:.5rem;justify-content:space-between}.lookup{justify-content:flex-start;margin:1rem 0}.lookup label{min-width:180px}.trace{display:flex;flex-wrap:wrap;gap:.45rem 1rem;padding:.8rem;margin:1rem 0;background:#f2f7ff;border:1px solid #c9ddff;border-radius:5px;color:#334155}.trace strong{width:100%;color:#1769aa}.error{color:#c62828}.muted{color:#667085;font-size:.9rem}table{width:100%;border-collapse:collapse;margin-top:1rem}th,td{text-align:left;padding:.6rem;border-bottom:1px solid #eee;white-space:nowrap}.empty{text-align:center;color:#667085;padding:2rem}@media(max-width:800px){.grid{grid-template-columns:repeat(2,1fr)}.heading{align-items:flex-start;flex-direction:column}.lookup{flex-wrap:wrap}}@media(max-width:480px){.grid{grid-template-columns:1fr}}
  `]
})
export class BaleProductionComponent implements OnInit {
  private http = inject(HttpClient);
  auth = inject(AuthService);

  lots: LotOption[] = [];
  items: BaleItem[] = [];
  summary: DailySummary[] = [];
  selectedLot?: LotOption;
  editing = this.newBale();
  error = '';
  saving = false;
  fromDate = new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10);
  toDate = new Date().toISOString().slice(0, 10);

  ngOnInit() { this.loadLots(); this.load(); this.loadSummary(); }

  private newBale() {
    return {
      lotId: '',
      lotNumber: '',
      productionDate: new Date().toISOString().slice(0, 10),
      baleWeight: 0,
      candy: 0,
      quintals: 0
    };
  }

  selectLot(value: string) {
    this.selectedLot = this.lots.find(l => l.id === value);
    this.editing.lotId = value;
    this.editing.lotNumber = this.selectedLot?.lotNumber ?? '';
    if (this.selectedLot) this.error = '';
  }

  loadLots() {
    this.http.get<ApiResponse<LotOption[]>>(`${API_BASE_URL}/kapas-purchases`).subscribe({
      next: r => {
        this.lots = r.data ?? [];
        if (this.editing.lotId) this.selectLot(this.editing.lotId);
      },
      error: e => {
        this.lots = [];
        this.error = e?.error?.error?.message ?? 'Could not load purchase lots. Please sign in with Bale Production access.';
      }
    });
  }

  load() {
    this.http.get<ApiResponse<BaleItem[]>>(`${API_BASE_URL}/bales`).subscribe({
      next: r => this.items = r.data ?? [],
      error: e => {
        this.items = [];
        this.error = e?.error?.error?.message ?? 'Could not load bale production records.';
      }
    });
  }

  loadSummary() {
    const params: string[] = [];
    if (this.fromDate) params.push(`fromDate=${encodeURIComponent(this.fromDate)}`);
    if (this.toDate) params.push(`toDate=${encodeURIComponent(this.toDate)}`);
    const url = `${API_BASE_URL}/bales/summary${params.length ? `?${params.join('&')}` : ''}`;
    this.http.get<ApiResponse<DailySummary[]>>(url).subscribe({
      next: r => this.summary = r.data ?? [],
      error: e => {
        this.summary = [];
        this.error = e?.error?.error?.message ?? 'Could not load the production summary.';
      }
    });
  }

  save() {
    if (!this.editing.lotId || !this.editing.lotNumber || !this.editing.productionDate || this.editing.baleWeight == null || Number(this.editing.baleWeight) <= 0) {
      this.error = 'Lot, production date, and positive bale weight are required.';
      return;
    }
    if (!this.selectedLot || this.selectedLot.id !== this.editing.lotId) {
      this.error = 'Select a valid purchase lot before saving the bale.';
      return;
    }
    this.saving = true;
    this.error = '';
    const payload = {
      lotId: this.editing.lotId,
      lotNumber: this.editing.lotNumber,
      productionDate: this.editing.productionDate,
      baleWeight: Number(this.editing.baleWeight),
      candy: this.editing.candy == null ? null : Number(this.editing.candy),
      quintals: this.editing.quintals == null ? null : Number(this.editing.quintals)
    };

    this.http.post<ApiResponse<BaleItem>>(`${API_BASE_URL}/bales`, payload).subscribe({
      next: () => {
        this.saving = false;
        this.reset();
        this.load();
        this.loadSummary();
      },
      error: e => {
        this.saving = false;
        this.error = e?.error?.error?.message ?? 'Could not create bale.';
      }
    });
  }

  reset() {
    this.editing = this.newBale();
    this.selectedLot = undefined;
    this.error = '';
  }

  reverse(item: BaleItem) {
    if (!item.id || !confirm(`Reverse ${item.baleNumber || 'this bale'}?`)) return;
    this.http.delete(`${API_BASE_URL}/bales/${item.id}`).subscribe({
      next: () => {
        this.load();
        this.loadSummary();
      },
      error: e => this.error = e?.error?.error?.message ?? 'Could not reverse the bale.'
    });
  }
}
