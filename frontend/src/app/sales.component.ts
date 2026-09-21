import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_BASE_URL } from './config';
import { ApiResponse } from './models';

interface PurchaseOption {
  id: string;
  entryNumber: string;
  supplierName: string;
  billDate: string;
  lotNumber?: string;
  netKapasWeight?: number;
  amount?: number;
}

interface Party {
  id: string;
  name: string;
}

interface Sale {
  id?: string;
  salesId?: string;
  billId: string;
  rawMaterialOutDate?: string;
  kapasPurchaseEntryId: string;
  customerId?: string;
  customerName: string;
  buyerContact?: string;
  buyerAddress?: string;
  baleKg?: number;
  seedKg?: number;
  soldProductWeight: number;
  saleRate?: number;
  salesValue?: number;
  notes?: string;
  supplierName?: string;
  lotNumber?: string;
  rawMaterialInDate?: string;
  rawMaterialNetWeight?: number;
  rawMaterialCost?: number;
}

@Component({
  selector: 'erp-sales',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page">
      <h1>Sales</h1>
      <p class="muted"><a routerLink="/">← Back to dashboard</a> · Record processed raw material sales and trace every lot.</p>

      <section class="card">
        <h2>{{ editing.id ? 'Edit sale' : 'Record sale' }}</h2>
        <div class="grid">
          <label>Sale / raw material out date *<input type="date" [(ngModel)]="editing.rawMaterialOutDate"></label>
          <label>Bill ID *<input [(ngModel)]="editing.billId" placeholder="Customer invoice number"></label>
          <label>Processed lot / purchase *<select [(ngModel)]="editing.kapasPurchaseEntryId" (ngModelChange)="selectPurchase($event)"><option value="">Select a purchase lot</option><option *ngFor="let purchase of purchases" [value]="purchase.id">{{purchase.lotNumber || purchase.entryNumber}} · {{purchase.supplierName}} · {{purchase.billDate}}</option></select></label>
          <label>Customer *<input list="customer-options" [(ngModel)]="editing.customerName" (ngModelChange)="selectCustomer($event)" placeholder="Search customer"><datalist id="customer-options"><option *ngFor="let customer of customers" [value]="customer.name"></option></datalist></label>
          <label>Buyer phone<input [(ngModel)]="editing.buyerContact"></label>
          <label>Buyer address<input [(ngModel)]="editing.buyerAddress"></label>
          <label>Raw material out date<input type="date" [(ngModel)]="editing.rawMaterialOutDate"></label>
          <label>Bale weight (kg) *<input type="number" min="0" step="0.001" [(ngModel)]="editing.baleKg"></label>
          <label>Seed retrieved (kg) *<input type="number" min="0" step="0.001" [(ngModel)]="editing.seedKg"></label>
          <label>Sold product weight (kg) *<input type="number" min="0" step="0.001" [(ngModel)]="editing.soldProductWeight"></label>
          <label>Sale rate<input type="number" min="0" step="0.01" [(ngModel)]="editing.saleRate"></label>
          <label>Sales value<input type="number" min="0" step="0.01" [(ngModel)]="editing.salesValue"></label>
        </div>
        <label>Notes<textarea rows="2" [(ngModel)]="editing.notes"></textarea></label>
        <div class="trace" *ngIf="selectedPurchase">
          <strong>Linked raw material</strong>
          <span>Supplier: {{selectedPurchase.supplierName}}</span>
          <span>Lot: {{selectedPurchase.lotNumber || '—'}}</span>
          <span>Arrived: {{selectedPurchase.billDate}}</span>
          <span>Net weight: {{selectedPurchase.netKapasWeight ?? '—'}} kg</span>
          <span>Cost: {{selectedPurchase.amount ?? '—'}}</span>
        </div>
        <p class="error" *ngIf="error">{{error}}</p>
        <button (click)="save()" [disabled]="saving">{{saving ? 'Saving…' : 'Save sale'}}</button>
        <button class="secondary" (click)="reset()">Clear</button>
      </section>

      <section class="card">
        <div class="heading"><div><h2>Sales register</h2><p class="muted">Search by sales ID or bill ID to retrieve the complete traceability record.</p></div><button class="secondary" (click)="load()">Refresh</button></div>
        <div class="lookup"><input [(ngModel)]="lookupId" placeholder="Enter sales ID or bill ID"><button (click)="lookup()" [disabled]="!lookupId.trim()">Retrieve</button><button class="secondary" (click)="clearLookup()">Clear</button></div>
        <div class="trace lookup-result" *ngIf="lookupResult">
          <strong>{{lookupResult.salesId || lookupResult.billId}}</strong>
          <span>Customer: {{lookupResult.customerName}}</span><span>Supplier: {{lookupResult.supplierName || '—'}}</span><span>Lot: {{lookupResult.lotNumber || '—'}}</span>
          <span>Raw material in: {{lookupResult.rawMaterialInDate || '—'}}</span><span>Raw material out: {{lookupResult.rawMaterialOutDate || '—'}}</span>
          <span>Raw material cost: {{lookupResult.rawMaterialCost ?? '—'}}</span><span>Raw material net: {{lookupResult.rawMaterialNetWeight ?? '—'}} kg</span>
          <span>Sold product: {{lookupResult.soldProductWeight}} kg</span><span>Sales value: {{lookupResult.salesValue ?? '—'}}</span>
        </div>
        <p class="error" *ngIf="lookupError">{{lookupError}}</p>
        <div class="lookup lot-lookup">
          <input [(ngModel)]="lotLookup" placeholder="Enter lot number, e.g. LOT-2026-000001">
          <button (click)="lookupLot()" [disabled]="!lotLookup.trim()">Find linked sales orders</button>
          <button class="secondary" (click)="clearLotLookup()">Clear</button>
        </div>
        <p class="muted" *ngIf="lotLookup.trim() && lotResultsLoaded">Sales orders linked to {{lotLookup.trim()}}: {{lotResults.length}}</p>
        <table *ngIf="lotResultsLoaded">
          <thead><tr><th>Sales ID</th><th>Bill ID</th><th>Out date</th><th>Customer</th><th>Sold kg</th><th>Sales value</th></tr></thead>
          <tbody>
            <tr *ngFor="let row of lotResults">
              <td>{{row.salesId}}</td><td>{{row.billId}}</td><td>{{row.rawMaterialOutDate}}</td><td>{{row.customerName}}</td><td>{{row.soldProductWeight}}</td><td>{{row.salesValue ?? '—'}}</td>
            </tr>
            <tr *ngIf="!lotResults.length"><td colspan="6" class="empty">No sales orders are linked to this lot.</td></tr>
          </tbody>
        </table>
        <table><thead><tr><th>Sales ID</th><th>Bill ID</th><th>Date</th><th>Customer</th><th>Supplier</th><th>Lot</th><th>Sold kg</th><th>Sales value</th><th></th></tr></thead>
          <tbody><tr *ngFor="let row of items"><td>{{row.salesId}}</td><td>{{row.billId}}</td><td>{{row.rawMaterialOutDate}}</td><td>{{row.customerName}}</td><td>{{row.supplierName || '—'}}</td><td>{{row.lotNumber || '—'}}</td><td>{{row.soldProductWeight}}</td><td>{{row.salesValue ?? '—'}}</td><td><button (click)="view(row)">View</button></td></tr>
          <tr *ngIf="!items.length"><td colspan="9" class="empty">No sales recorded yet.</td></tr></tbody>
        </table>
      </section>
    </div>
  `,
  styles: [`
    .page{max-width:1320px;margin:2rem auto;padding:0 1rem;font-family:Arial}.card{background:#fff;border:1px solid #ddd;border-radius:8px;padding:1rem;margin:1rem 0}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:.8rem}.grid label,label{display:flex;flex-direction:column;gap:.25rem;margin:.45rem 0;font-size:.85rem;font-weight:600;color:#475569}input,select,textarea{padding:.6rem;border:1px solid #bbb;border-radius:4px}button{background:#1769aa;color:#fff;border:0;border-radius:4px;padding:.55rem .9rem;cursor:pointer;margin:.25rem}button:disabled{opacity:.5}.secondary{background:#eef2f7;color:#334155}.heading,.lookup{display:flex;align-items:center;gap:.5rem;justify-content:space-between}.lookup{justify-content:flex-start;margin:1rem 0}.lookup input{min-width:280px}.trace{display:flex;flex-wrap:wrap;gap:.45rem 1rem;padding:.8rem;margin:1rem 0;background:#f2f7ff;border:1px solid #c9ddff;border-radius:5px;color:#334155}.trace strong{width:100%;color:#1769aa}.error{color:#c62828}.muted{color:#667085;font-size:.9rem}table{width:100%;border-collapse:collapse;margin-top:1rem}th,td{text-align:left;padding:.6rem;border-bottom:1px solid #eee;white-space:nowrap}.empty{text-align:center;color:#667085;padding:2rem}@media(max-width:800px){.grid{grid-template-columns:repeat(2,1fr)}.heading{align-items:flex-start;flex-direction:column}.lookup{flex-wrap:wrap}.lookup input{min-width:0;width:100%}}@media(max-width:480px){.grid{grid-template-columns:1fr}}
  `]
})
export class SalesComponent {
  private http = inject(HttpClient);
  purchases: PurchaseOption[] = [];
  customers: Party[] = [];
  items: Sale[] = [];
  selectedPurchase?: PurchaseOption;
  editing: Sale = this.newSale();
  lookupId = '';
  lookupResult?: Sale;
  lookupError = '';
  lotLookup = '';
  lotResults: Sale[] = [];
  lotResultsLoaded = false;
  error = '';
  saving = false;

  ngOnInit() { this.load(); this.loadMasters(); }

  load() { this.http.get<ApiResponse<Sale[]>>(`${API_BASE_URL}/sales`).subscribe({ next: r => this.items = r.data ?? [], error: e => this.error = e?.error?.error?.message ?? 'Could not load sales.' }); }

  loadMasters() {
    this.http.get<ApiResponse<PurchaseOption[]>>(`${API_BASE_URL}/kapas-purchases`).subscribe({ next: r => this.purchases = r.data ?? [] });
    this.http.get<ApiResponse<Party[]>>(`${API_BASE_URL}/master-data/customers`).subscribe({ next: r => this.customers = r.data ?? [] });
  }

  selectPurchase(id: string) {
    this.selectedPurchase = this.purchases.find(p => p.id === id);
    if (!this.selectedPurchase) return;
    this.editing.supplierName = this.selectedPurchase.supplierName;
    this.editing.lotNumber = this.selectedPurchase.lotNumber;
    this.editing.rawMaterialInDate = this.selectedPurchase.billDate;
    this.editing.rawMaterialNetWeight = this.selectedPurchase.netKapasWeight;
    this.editing.rawMaterialCost = this.selectedPurchase.amount;
  }

  selectCustomer(name: string) {
    this.editing.customerId = this.customers.find(c => c.name.toLowerCase() === name.trim().toLowerCase())?.id;
  }

  save() {
    this.error = '';
    if (!this.editing.billId || !this.editing.rawMaterialOutDate || !this.editing.kapasPurchaseEntryId || !this.editing.customerName || this.editing.baleKg == null || this.editing.seedKg == null || this.editing.soldProductWeight == null || this.editing.salesValue == null) {
      this.error = 'Bill ID, out date, processed lot, customer, bale weight, seed weight, sold weight, and sales value are required.';
      return;
    }
    this.saving = true;
    const request = this.http.post<ApiResponse<Sale>>(`${API_BASE_URL}/sales`, this.editing);
    request.subscribe({ next: () => { this.reset(); this.load(); }, error: e => { this.error = e?.error?.error?.message ?? 'Could not save sale.'; this.saving = false; } });
  }

  lookup() {
    this.lookupError = ''; this.lookupResult = undefined;
    const value = encodeURIComponent(this.lookupId.trim());
    this.http.get<ApiResponse<Sale>>(`${API_BASE_URL}/sales/lookup?salesId=${value}`).subscribe({ next: r => this.lookupResult = r.data, error: () =>
      this.http.get<ApiResponse<Sale>>(`${API_BASE_URL}/sales/lookup?billId=${value}`).subscribe({ next: r => this.lookupResult = r.data, error: e => this.lookupError = e?.error?.error?.message ?? 'No sale found for that sales ID or bill ID.' })
    });
  }

  lookupLot() {
    this.lookupError = '';
    this.lotResults = [];
    this.lotResultsLoaded = false;
    const lotNumber = encodeURIComponent(this.lotLookup.trim());
    this.http.get<ApiResponse<Sale[]>>(`${API_BASE_URL}/sales/lookup/lot?lotNumber=${lotNumber}`).subscribe({
      next: r => {
        this.lotResults = r.data ?? [];
        this.lotResultsLoaded = true;
      },
      error: e => this.lookupError = e?.error?.error?.message ?? 'Could not find sales linked to that lot.'
    });
  }

  view(row: Sale) { this.lookupId = row.salesId || row.billId; this.lookup(); }
  clearLookup() { this.lookupId = ''; this.lookupResult = undefined; this.lookupError = ''; }
  clearLotLookup() { this.lotLookup = ''; this.lotResults = []; this.lotResultsLoaded = false; }
  reset() { this.editing = this.newSale(); this.selectedPurchase = undefined; this.saving = false; }
  private newSale(): Sale { return { billId: '', rawMaterialOutDate: new Date().toISOString().slice(0, 10), kapasPurchaseEntryId: '', customerName: '', baleKg: 0, seedKg: 0, soldProductWeight: 0, salesValue: 0 }; }
}
