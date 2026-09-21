package com.ginning.erp.document.parser;

import com.ginning.erp.common.exception.ApplicationException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/** Turns an attached bill (PDF or plain text) into raw text that {@link TextParser} can read. */
@Component
public class FileTextExtractor {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> TEXT_EXTENSIONS = Set.of("txt", "csv", "text");

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException("ATTACHMENT_EMPTY", "Attached file is empty", 400);
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApplicationException("ATTACHMENT_TOO_LARGE", "Attached file exceeds the 10 MB limit", 400);
        }
        String extension = extension(file.getOriginalFilename());
        try {
            String text = "pdf".equals(extension) ? fromPdf(file.getBytes())
                    : TEXT_EXTENSIONS.contains(extension) ? new String(file.getBytes(), StandardCharsets.UTF_8)
                    : null;
            if (text == null) {
                throw new ApplicationException("ATTACHMENT_UNSUPPORTED_TYPE", "Unsupported file type '" + extension + "'. Attach a PDF or a text file.", 400);
            }
            if (text.isBlank()) {
                throw new ApplicationException("ATTACHMENT_NO_TEXT", "No readable text found in the attachment. Scanned images are not supported yet.", 422);
            }
            return text;
        } catch (IOException e) {
            throw new ApplicationException("ATTACHMENT_UNREADABLE", "Could not read the attached file: " + e.getMessage(), 400);
        }
    }

    private String fromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
