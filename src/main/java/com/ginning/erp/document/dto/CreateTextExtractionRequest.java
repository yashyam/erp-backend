package com.ginning.erp.document.dto;
import jakarta.validation.constraints.NotBlank;
public record CreateTextExtractionRequest(@NotBlank(message="sourceText is required") String sourceText) {}
