import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { API_BASE_URL } from './config';

interface Extraction {
  id: string; extractionNumber: string; sourceText: string; status: string; createdAt?: string;
  extractionMethod?: string; confidenceScore?: number; supplierName?: string; invoiceNumber?: string;
  invoiceDate?: string; vehicleNumber?: string; quantity?: number; rate?: number; tax?: number;
  totalAmount?: number; missingFields?: string[]; extractedData?: Record<string, unknown>;
  grossWeight?: number; tareWeight?: number; netKapasWeight?: number; numberOfBags?: number;
}

@Component({
  selector: 'erp-text-extractions', standalone: true, imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <h1>Raw Material Lifecycle / Text Extractions</h1>
      <section class="card">
        <h2>Attach bill</h2>
        <p class="hint">Attach a supplier bill (PDF or .txt). The fields are extracted and shown for your review. No kapas purchase entry is stored until you confirm the data.</p>
        <input type="file" accept=".pdf,.txt,.csv" (change)="onFileSelected($event)">
        <button (click)="upload()" [disabled]="!file || uploading">{{ uploading ? 'Extracting…' : 'Attach & extract' }}</button>
        <p class="note" *ngIf="uploadMessage">{{ uploadMessage }}</p>
        <p class="error" *ngIf="uploadError">{{ uploadError }}</p>
      </section>
      <section class="card">
        <h2>Submit source text</h2>
        <textarea [(ngModel)]="sourceText" rows="7" placeholder="Paste invoice or weighbridge text..."></textarea>
        <button (click)="submit()" [disabled]="!sourceText.trim()">Extract fields</button>
      </section>
      <section class="card">
        <h2>Extractions</h2>
        <div class="filters"><select [(ngModel)]="status" (change)="load()"><option value="">All statuses</option><option>PENDING</option><option>PROCESSING</option><option>COMPLETED</option><option>REVIEW_REQUIRED</option><option>FAILED</option></select><button (click)="load()">Refresh</button></div>
        <table><thead><tr><th>Number</th><th>Status</th><th>Supplier</th><th>Invoice</th><th>Created</th><th></th></tr></thead><tbody>
          <tr *ngFor="let item of items"><td>{{ item.extractionNumber }}</td><td><span class="badge">{{ item.status }}</span></td><td>{{ item.supplierName || '—' }}</td><td>{{ item.invoiceNumber || '—' }}</td><td>{{ item.createdAt || '' }}</td><td><button (click)="select(item)">Review</button></td></tr>
        </tbody></table>
      </section>
      <section class="card review-card" *ngIf="selected">
        <h2>Review extracted data</h2>
        <p><b>{{ selected.extractionNumber }}</b> · Check the values below before storing the kapas purchase entry.</p>
        <div class="missing-warning" *ngIf="selected.missingFields?.length"><b>Some details could not be extracted:</b> {{ selected.missingFields?.join(', ') }}. Please enter or correct them below.</div>
        <div class="duplicate-warning" *ngIf="duplicateError">Duplicate bill: {{ duplicateError }}</div>
        <div class="grid">
          <label>Supplier<input [(ngModel)]="selected.supplierName" (ngModelChange)="refreshMissingFields()"></label><label>Bill number<input [(ngModel)]="selected.invoiceNumber" (ngModelChange)="refreshMissingFields()"></label><label>Bill date<input type="date" [(ngModel)]="selected.invoiceDate" (ngModelChange)="refreshMissingFields()"></label><label>Vehicle number<input [(ngModel)]="selected.vehicleNumber" (ngModelChange)="refreshMissingFields()"></label><label>No. of bags<input type="number" min="0" [(ngModel)]="selected.numberOfBags" (ngModelChange)="refreshMissingFields()"></label><label>Gross weight<input type="number" min="0" step="0.001" [(ngModel)]="selected.grossWeight" (ngModelChange)="refreshMissingFields()"></label><label>Tare weight<input type="number" min="0" step="0.001" [(ngModel)]="selected.tareWeight" (ngModelChange)="refreshMissingFields()"></label><label>Net kapas weight<input type="number" [value]="selected.netKapasWeight ?? ''" readonly></label><label>Quantity<input type="number" [(ngModel)]="selected.quantity"></label><label>Rate<input type="number" [(ngModel)]="selected.rate"></label><label>Tax<input type="number" [(ngModel)]="selected.tax"></label><label>Total amount<input type="number" [(ngModel)]="selected.totalAmount"></label>
        </div>
        <button (click)="save()">Save review</button><button (click)="confirmAndCreateKapasEntry()" [disabled]="creatingKapas || !!duplicateError || !!selected.missingFields?.length">{{ creatingKapas ? 'Saving…' : 'Confirm data & create purchase entry' }}</button>
        <p class="note" *ngIf="kapasMessage">{{ kapasMessage }}</p>
      </section>
    </div>
  `,
  styles: [`
    .page{max-width:1100px;margin:2rem auto;padding:0 1rem;font-family:Arial}.card{background:#fff;border:1px solid #ddd;border-radius:8px;padding:1rem;margin:1rem 0}textarea{width:100%;box-sizing:border-box;margin-bottom:.7rem}button{background:#1769aa;color:white;border:0;border-radius:4px;padding:.55rem .9rem;cursor:pointer;margin:.25rem}button:disabled{opacity:.5}.filters{display:flex;gap:.5rem;margin-bottom:1rem}select,input{padding:.5rem;border:1px solid #bbb;border-radius:4px}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:.6rem;border-bottom:1px solid #eee}.badge{padding:.25rem .5rem;border-radius:10px;background:#e7f1ff;font-size:.8rem}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:.8rem;margin:1rem 0}.grid label{display:flex;flex-direction:column;gap:.25rem}.note{color:#1769aa;margin:.5rem 0 0}.error{color:#c62828;margin:.5rem 0 0}.missing-warning{padding:.75rem;background:#fff8e1;border:1px solid #f0c36d;color:#7a4f01;border-radius:4px}.duplicate-warning{padding:.75rem;background:#fff1f0;border:1px solid #ef9a9a;color:#b71c1c;border-radius:4px}.hint{color:#555;font-size:.9rem;margin:.2rem 0 .8rem}@media(max-width:700px){.grid{grid-template-columns:repeat(2,1fr)}}
  `]
})
export class TextExtractionsComponent {
  private http = inject(HttpClient); private router = inject(Router);
  items: Extraction[] = []; selected?: Extraction; sourceText = ''; status = '';
  creatingKapas = false; kapasMessage = ''; duplicateError = ''; file: File | null = null;
  uploading = false; uploadMessage = ''; uploadError = '';

  onFileSelected(event: Event) { const input = event.target as HTMLInputElement; this.file = input.files?.[0] ?? null; this.uploadMessage = ''; this.uploadError = ''; }
  upload() {
    if (!this.file) return; this.uploading = true; this.uploadMessage = ''; this.uploadError = '';
    const form = new FormData(); form.append('file', this.file);
    this.http.post<any>(`${API_BASE_URL}/document/text-extractions/upload`, form).subscribe({
      next: response => { const data = response?.data; this.uploading = false; this.uploadMessage = response?.message ?? 'Bill extracted. Review the data below.'; this.uploadError = data?.kapasEntryError ?? ''; this.file = null; this.load(); if (data?.extraction) this.select(data.extraction); this.duplicateError = data?.duplicate ? data.kapasEntryError : ''; },
      error: error => { this.uploading = false; this.uploadError = (error?.status === 401 || error?.status === 403) ? 'Your session has expired. Please sign in again and retry the upload.' : error?.error?.error?.message ?? 'Could not extract the attached bill'; }
    });
  }
  ngOnInit() { this.load(); }
  load() { let params = new HttpParams().set('page', '0').set('size', '50'); if (this.status) params = params.set('status', this.status); this.http.get<any>(`${API_BASE_URL}/document/text-extractions`, { params }).subscribe(response => this.items = response.data?.content ?? []); }
  submit() { this.http.post<any>(`${API_BASE_URL}/document/text-extractions`, { sourceText: this.sourceText }).subscribe(response => { this.sourceText = ''; this.load(); this.select(response.data); }); }
  select(item: Extraction) { this.selected = { ...item, grossWeight: this.number(item.extractedData?.['grossWeight']), tareWeight: this.number(item.extractedData?.['tareWeight']), netKapasWeight: this.number(item.extractedData?.['netWeight']), numberOfBags: this.number(item.extractedData?.['numberOfBags']) }; this.duplicateError = ''; this.kapasMessage = ''; this.refreshMissingFields(); }
  private number(value: unknown) { return typeof value === 'number' ? value : value == null ? undefined : Number(value); }
  refreshMissingFields() { if (!this.selected) return; if (this.selected.grossWeight != null && this.selected.tareWeight != null) this.selected.netKapasWeight = this.selected.grossWeight - this.selected.tareWeight; else this.selected.netKapasWeight = undefined; const missing: string[] = []; if (!this.selected.supplierName?.trim()) missing.push('Supplier name'); if (!this.selected.invoiceNumber?.trim()) missing.push('Bill number'); if (!this.selected.invoiceDate) missing.push('Bill date'); if (!this.selected.vehicleNumber?.trim()) missing.push('Vehicle number'); if (this.selected.numberOfBags == null) missing.push('No. of bags'); if (this.selected.grossWeight == null) missing.push('Gross weight'); if (this.selected.tareWeight == null) missing.push('Tare weight'); this.selected.missingFields = missing; }
  save(onSaved?: () => void) {
    if (!this.selected) return;
    const { id, supplierName, invoiceNumber, invoiceDate, vehicleNumber, quantity, rate, tax, totalAmount, extractedData } = this.selected;
    const updatedData = { ...(extractedData ?? {}), grossWeight: this.selected.grossWeight, tareWeight: this.selected.tareWeight, numberOfBags: this.selected.numberOfBags, netWeight: this.selected.grossWeight != null && this.selected.tareWeight != null ? this.selected.grossWeight - this.selected.tareWeight : undefined };
    this.http.put(`${API_BASE_URL}/document/text-extractions/${id}`, { supplierName, invoiceNumber, invoiceDate, vehicleNumber, quantity, rate, tax, totalAmount, extractedData: updatedData }).subscribe({ next: () => { this.selected!.extractedData = updatedData; this.refreshMissingFields(); this.load(); onSaved?.(); } });
  }
  confirmAndCreateKapasEntry() { if (!this.selected) return; this.creatingKapas = true; this.kapasMessage = ''; this.save(() => this.createKapasEntry()); }
  createKapasEntry() {
    if (!this.selected) return;
    this.http.post<any>(`${API_BASE_URL}/kapas-purchases/from-extraction/${this.selected.id}`, {}).subscribe({
      next: response => { this.creatingKapas = false; this.kapasMessage = `Created ${response?.data?.entryNumber ?? 'entry'}. Opening kapas purchases…`; this.router.navigateByUrl('/kapas-purchases'); },
      error: error => { this.creatingKapas = false; this.kapasMessage = error?.error?.error?.message ?? 'Could not create kapas purchase entry'; if (error?.error?.error?.code === 'KAPAS_BILL_EXISTS') this.duplicateError = this.kapasMessage; }
    });
  }
}
