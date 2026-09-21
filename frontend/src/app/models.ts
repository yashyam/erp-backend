export interface ApiResponse<T> { success: boolean; data: T; message?: string; error?: { message: string }; }
export interface Session { accessToken: string; refreshToken: string; username?: string; email?: string; tokenType?: string; }
export interface MasterRecord { id?: string; code: string; name: string; contactPerson?: string; phone?: string; email?: string; address?: string; gstin?: string; pan?: string; capacity?: number; }
export type RecordKind = 'suppliers' | 'customers' | 'godowns';
