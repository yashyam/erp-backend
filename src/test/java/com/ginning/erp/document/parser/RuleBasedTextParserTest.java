package com.ginning.erp.document.parser;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedTextParserTest {

    private final RuleBasedTextParser parser = new RuleBasedTextParser();

    @Test
    void parsesCottonTaxInvoice() {
        String text = """
                Shree Balaji Cotton Traders Pvt Ltd
                Invoice No. : KP/2026/114        Dated : 10-Sep-26
                Motor Vehicle No. : GJ05AB1234
                Lot Number: LOT-778
                Godown : Main Yard
                Gross  Weight :  12,450.500
                Less Tare : 3,200.250
                Net Weight : 9250.250
                Rate : 7,250.00
                Amount : 670,643.13
                """;
        ExtractionResult r = parser.parse(text);
        assertEquals("Shree Balaji Cotton Traders Pvt Ltd", r.supplierName());
        assertEquals("KP/2026/114", r.invoiceNumber());
        assertEquals(LocalDate.of(2026, 9, 10), r.invoiceDate());
        assertEquals("GJ05AB1234", r.vehicleNumber());
        assertEquals(new BigDecimal("12450.500"), r.data().get("grossWeight"));
        assertEquals(new BigDecimal("3200.250"), r.data().get("tareWeight"));
        assertEquals(new BigDecimal("9250.250"), r.data().get("netWeight"));
        assertEquals("LOT-778", r.data().get("lotNumber"));
        assertEquals("Main Yard", r.data().get("godown"));
        assertEquals(new BigDecimal("7250.00"), r.rate());
        assertEquals(new BigDecimal("670643.13"), r.totalAmount());
    }

    @Test
    void parsesAlternateLabelsAndFourDigitYear() {
        String text = """
                Supplier Name: Kisan Agro Industries
                Bill Number: B-991
                Invoice Date: 10-Sep-2026
                Vehicle Number: MH12XY9999
                Gross Weight: 1000
                Tare Wt: 250
                LOT No: L-12
                Rate: 60
                """;
        ExtractionResult r = parser.parse(text);
        assertEquals("Kisan Agro Industries", r.supplierName());
        assertEquals("B-991", r.invoiceNumber());
        assertEquals(LocalDate.of(2026, 9, 10), r.invoiceDate());
        assertEquals(new BigDecimal("750"), r.data().get("netWeight"));
        assertEquals("L-12", r.data().get("lotNumber"));
    }

    @Test
    void parsesTallyColumnarEInvoice() {
        String text = """
                Tax Invoice e-Invoice
                Ack Date : 10-Sep-26
                JINENDRA TRADERS (26-27) Invoice No. e-Way Bill No. Dated
                R M C Yard GST-80-26-27 132546527517 10-Sep-26
                GSTIN/UIN: 29CZGPK9434D1Z4
                Consignee (Ship to)
                SHRI SAIBALAJI TRADERS Dispatched through Destination
                OPP E B SUBSTATION Bill of Lading/LR-RR No. Motor Vehicle No.
                THENI dt. 10-Sep-26 TN88C4146
                Sl Description of Goods HSN/SAC Quantity Rate per Amount
                1 Raw Cotton 52010011 14,165 kgs 96.11 kgs 13,61,349.00
                Gross Weight 14560
                Less Tare          395
                Net Weight     14165
                IGST @ 5% 5 % 68,067.45
                Total 14,165 kgs \u20b9 14,29,416.00
                HSN/SAC Taxable IGST Total
                52010011 13,61,349.00 5% 68,067.45 68,067.45
                Total 13,61,349.00 68,067.45 68,067.45
                """;
        ExtractionResult r = parser.parse(text);
        assertEquals("JINENDRA TRADERS (26-27)", r.supplierName());
        assertEquals("GST-80-26-27", r.invoiceNumber());
        assertEquals(LocalDate.of(2026, 9, 10), r.invoiceDate());
        assertEquals("TN88C4146", r.vehicleNumber(), "must not pick up an address line");
        assertEquals(new BigDecimal("14165"), r.quantity(), "must be the line-item qty, not gross weight");
        assertEquals(new BigDecimal("96.11"), r.rate());
        assertEquals(new BigDecimal("68067.45"), r.tax(), "must not pick up the HSN code");
        assertEquals(new BigDecimal("1429416.00"), r.totalAmount(), "must be the currency-marked grand total");
        assertEquals(new BigDecimal("14560"), r.data().get("grossWeight"));
        assertEquals(new BigDecimal("395"), r.data().get("tareWeight"));
        assertEquals(new BigDecimal("14165"), r.data().get("netWeight"));
    }

    @Test
    void stillParsesClassicNumericInvoice() {
        String text = "Supplier: ACME Ltd\nInvoice No: INV-1\nInvoice Date: 01/02/2024\nQuantity: 10\nRate: 5\nTax: 2\nTotal: 52";
        ExtractionResult r = parser.parse(text);
        assertEquals("ACME Ltd", r.supplierName());
        assertEquals(LocalDate.of(2024, 2, 1), r.invoiceDate());
        assertEquals(new BigDecimal("10"), r.quantity());
        assertEquals(new BigDecimal("52"), r.totalAmount());
        assertNull(r.data().get("grossWeight"));
    }
}
