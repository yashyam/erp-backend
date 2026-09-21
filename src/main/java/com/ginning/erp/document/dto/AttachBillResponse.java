package com.ginning.erp.document.dto;

import com.ginning.erp.document.entity.TextExtraction;
import com.ginning.erp.kapas.dto.KapasPurchaseEntryResponse;

/** Result of attaching a bill: what was extracted, and the kapas entry created from it (if requested). */
public record AttachBillResponse(TextExtraction extraction, KapasPurchaseEntryResponse kapasPurchaseEntry, String kapasEntryError, boolean duplicate) {}
