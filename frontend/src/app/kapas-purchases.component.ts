import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_BASE_URL } from './config';
import { ApiResponse } from './models';
import { AuthService } from './auth.service';

export interface KapasPurchaseEntry {
  id?: string; entryNumber?: string; billDate: string; supplierName: string; billNumber: string;
  supplierId?: string; vehicleNumber?: string; numberOfBags: number; grossWeight?: number; tareWeight?: number; netKapasWeight?: number;
  rate?: number; quintals?: number; amount?: number; lotNumber?: string; godownId?: string | null; godownName?: string;
  textExtractionId?: string; createdBy?: string;
}
interface Godown { id: string; name: string; }
interface Supplier { id: string; name: string; code?: string; }

@Component({
  selector: 'erp-kapas-purchases', standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `
<div class="page">
  <h1>Kapas (Raw Cotton) Purchase Bills</h1>
  <p class="muted"><a routerLink="/">← Back to dashboard</a> · <a routerLink="/text-extractions">Text extractions</a></p>

  <section class="card"><h2>{{editing.id ? 'Edit entry ' + (editing.entryNumber || '') : 'Kapas Purchase'}}</h2>
    <h3>Section A — Purchase Details</h3>
    <div class="grid purchase-grid">
      <label>Date *<input type="date" [(ngModel)]="editing.billDate"></label>
      <label>Supplier Name *<input list="supplier-options" [(ngModel)]="editing.supplierName" (ngModelChange)="selectSupplierByName($event)" placeholder="Search supplier"><datalist id="supplier-options"><option *ngFor="let supplier of suppliers" [value]="supplier.name"></option></datalist><small class="field-error" *ngIf="validationAttempted && editing.supplierName && !editing.supplierId">Supplier is not linked to supplier master. Select a suggestion or add it.</small></label><button type="button" class="secondary" (click)="supplierModal=true">+ Add New Supplier</button>
      <label>Bill Number *<input [(ngModel)]="editing.billNumber"></label>
      <label>Vehicle Number *<input [(ngModel)]="editing.vehicleNumber"></label>
      <label>No. of Bags *<input type="number" min="0" step="1" [(ngModel)]="editing.numberOfBags"></label>
    </div>
    <h3>Section B — Weighment Details</h3>
    <div class="grid weighment-grid">
      <label>Gross Weight *<input type="number" step="0.001" min="0" [(ngModel)]="editing.grossWeight" (ngModelChange)="calculateNetWeight()"></label>
      <label>Tare Weight *<input type="number" step="0.001" min="0" [(ngModel)]="editing.tareWeight" (ngModelChange)="calculateNetWeight()"></label>
      <label>Net Kapas Weight *<input type="number" [value]="editing.netKapasWeight ?? ''" readonly></label>
    </div>
    <h3>Section C — Purchase Value</h3>
    <div class="grid purchase-value-grid">
      <label>Rate per Quintal *<input type="number" step="0.01" min="0" [(ngModel)]="editing.rate" (ngModelChange)="calculatePurchaseValue()"></label>
      <label>Quintals<input type="number" [value]="editing.quintals ?? ''" readonly></label>
      <label>Amount Without Tax<input type="number" [value]="editing.amount ?? ''" readonly></label>
    </div>
    <h3>Section D — Storage / Identification</h3>
    <div class="grid">
      <label>Lot Number<input [value]="editing.lotNumber || 'Generated on save'" readonly></label>
      <label>Godown *<input list="godown-options" [(ngModel)]="selectedGodownName" (ngModelChange)="selectGodown($event)" placeholder="Search godown"><datalist id="godown-options"><option *ngFor="let g of godowns" [value]="g.name"></option></datalist></label>
    </div>
    <div class="validation-errors" *ngIf="validationAttempted && validationMessages.length">
      <strong>Cannot save this purchase:</strong>
      <ul><li *ngFor="let message of validationMessages">{{message}}</li></ul>
    </div>
    <p class="error" *ngIf="error">{{error}}</p>
    <button *ngIf="canCreate" (click)="reviewBeforeSave()" [disabled]="saving">{{saving ? 'Saving…' : 'Review & save purchase'}}</button>
    <button class="secondary" (click)="reset()">Clear</button>
  </section>

  <section class="card"><div class="entries-heading"><h2>Entries</h2><button (click)="load()">Refresh</button></div>
    <div class="entry-filters">
      <label>Bill date<input list="bill-date-options" placeholder="Search bill date" [(ngModel)]="filters.billDate"><datalist id="bill-date-options"><option *ngFor="let value of filterOptions.billDates" [value]="value"></option></datalist></label>
      <label>Supplier<input list="supplier-options" placeholder="Search supplier" [(ngModel)]="filters.supplier"><datalist id="supplier-options"><option *ngFor="let value of filterOptions.suppliers" [value]="value"></option></datalist></label>
      <label>Lot number<input list="lot-options" placeholder="Search lot" [(ngModel)]="filters.lotNumber"><datalist id="lot-options"><option *ngFor="let value of filterOptions.lotNumbers" [value]="value"></option></datalist></label>
      <label>Entry number<input list="entry-number-options" placeholder="Search number" [(ngModel)]="filters.entryNumber"><datalist id="entry-number-options"><option *ngFor="let value of filterOptions.entryNumbers" [value]="value"></option></datalist></label>
      <label>Vehicle number<input list="vehicle-options" placeholder="Search vehicle" [(ngModel)]="filters.vehicleNumber"><datalist id="vehicle-options"><option *ngFor="let value of filterOptions.vehicleNumbers" [value]="value"></option></datalist></label><label>Godown<input list="history-godown-options" placeholder="Search godown" [(ngModel)]="filters.godown"><datalist id="history-godown-options"><option *ngFor="let value of filterOptions.godowns" [value]="value"></option></datalist></label>
      <button class="secondary clear-filter" (click)="clearFilters()">Clear filters</button>
    </div>
    <p class="filter-summary">Showing {{filteredItems.length}} of {{items.length}} entries</p>
    <table><thead><tr><th>Number</th><th>Bill date</th><th>Supplier</th><th>Bill no</th><th>Vehicle</th><th>Bags</th><th>Gross</th><th>Tare</th><th>Net</th><th>Quintals</th><th>Rate</th><th>Amount</th><th>Lot</th><th>Godown</th><th></th></tr></thead>
      <tbody>
        <tr *ngFor="let row of filteredItems">
          <td>{{row.entryNumber}}</td><td>{{row.billDate}}</td><td>{{row.supplierName}}</td><td>{{row.billNumber}}</td>
          <td>{{row.vehicleNumber || '—'}}</td><td>{{row.numberOfBags}}</td><td>{{row.grossWeight ?? '—'}}</td><td>{{row.tareWeight ?? '—'}}</td>
          <td>{{row.netKapasWeight ?? '—'}}</td><td>{{row.quintals ?? '—'}}</td><td>{{row.rate ?? '—'}}</td><td>{{row.amount ?? '—'}}</td>
          <td>{{row.lotNumber || '—'}}</td><td>{{row.godownName || '—'}}</td>
          <td class="actions"><button (click)="view(row)">View</button><button *ngIf="canUpdate" (click)="edit(row)">Edit</button><button *ngIf="canDelete" class="danger" (click)="remove(row)">Delete</button></td>
        </tr>
        <tr *ngIf="!filteredItems.length"><td colspan="15" class="empty">{{items.length ? 'No entries match the selected filters.' : 'No kapas purchase entries yet.'}}</td></tr>
      </tbody>
    </table>
  </section>
  <div class="overlay" *ngIf="supplierModal"><section class="modal"><h2>Add New Supplier</h2><label>Name *<input [(ngModel)]="newSupplier.name"></label><label>Code<input [(ngModel)]="newSupplier.code"></label><label>Phone<input [(ngModel)]="newSupplier.phone"></label><label>Address<input [(ngModel)]="newSupplier.address"></label><p class="error" *ngIf="supplierError">{{supplierError}}</p><button (click)="addSupplier()">Save supplier</button><button class="secondary" (click)="supplierModal=false">Cancel</button></section></div>
  <div class="overlay" *ngIf="confirmation"><section class="modal"><h2>Confirm Kapas Purchase</h2><p>Date: {{editing.billDate}}</p><p>Supplier: {{editing.supplierName}}</p><p>Bill: {{editing.billNumber}}</p><p>Vehicle: {{editing.vehicleNumber}}</p><p>Bags: {{editing.numberOfBags}}</p><p>Gross / Tare / Net: {{editing.grossWeight}} / {{editing.tareWeight}} / {{editing.netKapasWeight}}</p><p>Rate / Quintals / Amount: {{editing.rate}} / {{editing.quintals}} / {{editing.amount}}</p><p>Lot: {{editing.lotNumber || 'Generated on save'}}</p><p>Godown: {{selectedGodownName}}</p><button (click)="saveConfirmed()">Save Purchase</button><button class="secondary" (click)="confirmation=false">Cancel</button></section></div>
  <div class="overlay" *ngIf="detail"><section class="modal"><h2>Purchase {{detail.entryNumber}}</h2><p>Lot: {{detail.lotNumber}}</p><p>Supplier: {{detail.supplierName}} ({{detail.supplierId}})</p><p>Godown: {{detail.godownName}} ({{detail.godownId}})</p><p>Created by: {{detail.createdBy}}</p><p>Net weight: {{detail.netKapasWeight}} kg · {{detail.quintals}} quintals</p><p>Amount: {{detail.amount}}</p><button (click)="detail=undefined">Close</button></section></div>
</div>`,
    styles: [`.page{max-width:1300px;margin:2rem auto;padding:0 1rem;font-family:Arial}.card{background:#fff;border:1px solid #ddd;border-radius:8px;padding:1rem;margin:1rem 0}button{background:#1769aa;color:white;border:0;border-radius:4px;padding:.55rem .9rem;cursor:pointer;margin:.25rem}button:disabled{opacity:.5}button.secondary{background:#6b7280}button.danger{background:#b91c1c}select,input{padding:.5rem;border:1px solid #bbb;border-radius:4px}input[readonly]{background:#f3f4f6;color:#374151}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:.6rem;border-bottom:1px solid #eee}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:.8rem;margin:1rem 0}.purchase-grid{grid-template-columns:repeat(5,1fr)}.weighment-grid{grid-template-columns:repeat(3,1fr)}.purchase-value-grid{grid-template-columns:repeat(3,1fr)}.grid label{display:flex;flex-direction:column;gap:.25rem}h3{font-size:1rem;margin:1.25rem 0 .5rem;color:#243b53}.calculation{font-family:monospace;background:#f8fafc;border:1px solid #d9e2ec;border-radius:4px;padding:.8rem}.muted{color:#666}.error{color:#b91c1c}.field-error{color:#b91c1c;font-size:.8rem;font-weight:600}.validation-errors{color:#b91c1c;background:#fef2f2;border:1px solid #fecaca;border-radius:4px;padding:.7rem 1rem}.validation-errors ul{margin:.4rem 0 0;padding-left:1.2rem}.empty{text-align:center;color:#888}.actions{white-space:nowrap}.entries-heading{display:flex;align-items:center;justify-content:space-between}.entries-heading h2{margin:0}.entry-filters{display:grid;grid-template-columns:repeat(5,minmax(130px,1fr)) auto;gap:.7rem;align-items:end;padding:1rem 0 .3rem}.entry-filters label{display:flex;flex-direction:column;gap:.3rem;font-size:.8rem;font-weight:600;color:#555}.entry-filters input{width:100%;box-sizing:border-box}.clear-filter{white-space:nowrap}.filter-summary{color:#666;font-size:.85rem;margin:.45rem 0 .8rem}@media(max-width:1000px){.entry-filters{grid-template-columns:repeat(3,minmax(150px,1fr))}.purchase-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:700px){.grid,.purchase-grid,.weighment-grid,.purchase-value-grid{grid-template-columns:repeat(2,1fr)}.entry-filters{grid-template-columns:repeat(2,minmax(130px,1fr))}}`]
})
export class KapasPurchasesComponent {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  items: KapasPurchaseEntry[] = [];
  godowns: Godown[] = [];
  suppliers: Supplier[] = [];
  editing: KapasPurchaseEntry = this.blank();
  filters = { billDate: '', supplier: '', lotNumber: '', entryNumber: '', vehicleNumber: '', godown: '' };
  selectedGodownName = ''; supplierModal = false; confirmation = false; detail?: KapasPurchaseEntry;
  newSupplier = { name: '', code: '', phone: '', address: '' }; supplierError = '';
  saving = false; error = ''; validationAttempted = false;
  get canCreate() { return this.auth.hasAuthority('KAPAS_PURCHASE_CREATE'); }
  get canUpdate() { return this.auth.hasAuthority('KAPAS_PURCHASE_UPDATE'); }
  get canDelete() { return this.auth.hasAuthority('KAPAS_PURCHASE_DELETE'); }

  get filteredItems() {
    const matches = (value: string | undefined, filter: string) => !filter || (value ?? '').toLowerCase().includes(filter.toLowerCase());
    return this.items.filter(row => matches(row.billDate, this.filters.billDate)
      && matches(row.supplierName, this.filters.supplier)
      && matches(row.lotNumber, this.filters.lotNumber)
      && matches(row.entryNumber, this.filters.entryNumber)
      && matches(row.vehicleNumber, this.filters.vehicleNumber)
      && matches(row.godownName, this.filters.godown));
  }

  get filterOptions() {
    const unique = (values: (string | undefined)[]) => [...new Set(values.filter((value): value is string => !!value))].sort();
    return {
      billDates: unique(this.items.map(row => row.billDate)),
      suppliers: unique(this.items.map(row => row.supplierName)),
      lotNumbers: unique(this.items.map(row => row.lotNumber)),
      entryNumbers: unique(this.items.map(row => row.entryNumber)),
      vehicleNumbers: unique(this.items.map(row => row.vehicleNumber)),
      godowns: unique(this.items.map(row => row.godownName))
    };
  }

  ngOnInit() { this.load(); this.loadGodowns(); this.loadSuppliers(); }
  blank(): KapasPurchaseEntry { return { billDate: new Date().toISOString().slice(0, 10), supplierName: '', billNumber: '', vehicleNumber: '', numberOfBags: 0, grossWeight: undefined, tareWeight: undefined, netKapasWeight: undefined, rate: undefined, quintals: undefined, amount: undefined, lotNumber: undefined, godownId: null }; }
  reset() { this.editing = this.blank(); this.selectedGodownName = ''; this.error = ''; this.validationAttempted = false; }
  clearFilters() { this.filters = { billDate: '', supplier: '', lotNumber: '', entryNumber: '', vehicleNumber: '', godown: '' }; }
  get validationMessages(): string[] {
    const messages: string[] = [];
    if (!this.editing.billDate) messages.push('Bill date is required.');
    if (!this.editing.supplierName?.trim()) messages.push('Supplier name is required.');
    else if (!this.editing.supplierId) messages.push('Select an existing supplier or add this supplier using “Add New Supplier”.');
    if (!this.editing.billNumber?.trim()) messages.push('Bill number is required.');
    if (!this.editing.vehicleNumber?.trim()) messages.push('Vehicle number is required.');
    if (!(this.editing.numberOfBags > 0)) messages.push('Number of bags must be greater than zero.');
    if (!(this.editing.grossWeight != null && this.editing.grossWeight > 0)) messages.push('Gross weight must be greater than zero.');
    if (!(this.editing.tareWeight != null && this.editing.tareWeight >= 0)) messages.push('Tare weight is required and cannot be negative.');
    if (!(this.editing.netKapasWeight != null && this.editing.netKapasWeight > 0)) messages.push('Net kapas weight must be greater than zero. Check gross and tare weights.');
    if (!(this.editing.rate != null && this.editing.rate > 0)) messages.push('Rate per quintal must be greater than zero.');
    if (!this.editing.godownId) messages.push('Godown must be selected from the list.');
    return messages;
  }
  valid() { return this.validationMessages.length === 0; }
  calculateNetWeight() { if (this.editing.grossWeight != null && this.editing.tareWeight != null) this.editing.netKapasWeight = Math.max(0, this.editing.grossWeight - this.editing.tareWeight); else this.editing.netKapasWeight = undefined; this.calculatePurchaseValue(); }
  calculatePurchaseValue() { this.editing.quintals = this.editing.netKapasWeight != null ? this.editing.netKapasWeight / 100 : undefined; this.editing.amount = this.editing.quintals != null && this.editing.rate != null ? this.editing.quintals * this.editing.rate : undefined; }
  load() { this.http.get<ApiResponse<KapasPurchaseEntry[]>>(`${API_BASE_URL}/kapas-purchases`).subscribe({ next: r => this.items = r.data ?? [], error: () => this.items = [] }); }
  loadGodowns() { this.http.get<ApiResponse<Godown[]>>(`${API_BASE_URL}/master-data/godowns`).subscribe({ next: r => this.godowns = r.data ?? [], error: () => this.godowns = [] }); }
  loadSuppliers() { this.http.get<ApiResponse<Supplier[]>>(`${API_BASE_URL}/master-data/suppliers`).subscribe({ next: r => this.suppliers = r.data ?? [], error: () => this.suppliers = [] }); }
  selectSupplier(id?: string) { this.editing.supplierId = id; this.editing.supplierName = this.suppliers.find(s => s.id === id)?.name ?? ''; }
  selectSupplierByName(name: string) { this.editing.supplierName = name; this.editing.supplierId = this.suppliers.find(s => s.name.toLowerCase() === name.toLowerCase())?.id; }
  selectGodown(name: string) { this.selectedGodownName = name; this.editing.godownId = this.godowns.find(g => g.name === name)?.id ?? null; }
  edit(row: KapasPurchaseEntry) { this.editing = { ...row, godownId: row.godownId ?? null }; this.selectedGodownName = row.godownName ?? ''; this.calculateNetWeight(); this.error = ''; }
  view(row: KapasPurchaseEntry) { this.detail = row; }
  reviewBeforeSave() {
    this.validationAttempted = true;
    this.error = '';
    if (this.valid()) this.confirmation = true;
    else if (this.editing.supplierName?.trim() && !this.editing.supplierId) {
      this.error = `Supplier "${this.editing.supplierName.trim()}" is not registered in the supplier master. Click Add New Supplier, create it, then select it from the supplier list.`;
    } else {
      this.error = this.validationMessages.join(' ');
    }
  }
  saveConfirmed() { this.confirmation = false; this.save(); }
  addSupplier() { if (!this.newSupplier.name.trim()) { this.supplierError = 'Supplier name is required'; return; } const payload = { ...this.newSupplier, code: this.newSupplier.code || `SUP-${Date.now()}` }; this.http.post<ApiResponse<Supplier>>(`${API_BASE_URL}/master-data/suppliers`, payload).subscribe({ next: r => { this.suppliers = [...this.suppliers, r.data]; this.selectSupplier(r.data.id); this.supplierModal = false; this.newSupplier = { name: '', code: '', phone: '', address: '' }; this.supplierError = ''; }, error: e => this.supplierError = e?.error?.error?.message ?? 'Could not create supplier' }); }
  save() {
    if (!this.valid()) return;
    this.saving = true; this.error = '';
    const url = `${API_BASE_URL}/kapas-purchases${this.editing.id ? `/${this.editing.id}` : ''}`;
    const req = this.editing.id ? this.http.put<ApiResponse<KapasPurchaseEntry>>(url, this.editing) : this.http.post<ApiResponse<KapasPurchaseEntry>>(url, this.editing);
    req.subscribe({ next: () => { this.saving = false; this.reset(); this.load(); }, error: e => { this.saving = false; this.error = e?.error?.error?.message ?? 'Could not save entry'; } });
  }
  remove(row: KapasPurchaseEntry) { if (row.id && confirm(`Delete ${row.entryNumber}?`)) this.http.delete(`${API_BASE_URL}/kapas-purchases/${row.id}`).subscribe(() => this.load()); }
}
