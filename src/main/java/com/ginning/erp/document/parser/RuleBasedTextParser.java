package com.ginning.erp.document.parser;

import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.format.DateTimeFormatter; import java.time.format.DateTimeParseException; import java.util.*; import java.util.regex.*;

@Component
public class RuleBasedTextParser implements TextParser {
  private static final List<DateTimeFormatter> NUMERIC_DATE_FORMATS = List.of(
      DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  private static final List<DateTimeFormatter> ALPHA_DATE_FORMATS = List.of(
      DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH), DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH));

  /** Matches a label tolerating extra spacing inside it and optional trailing dots/colons. */
  private static String labelPattern(String label) {
    StringBuilder sb = new StringBuilder();
    for (String part : label.trim().split("\\s+")) {
      if (sb.length() > 0) sb.append("\\s+");
      sb.append(Pattern.quote(part.replaceAll("[.:]+$", ""))).append("\\.?");
    }
    return sb.toString();
  }
  /** Trims a captured value at the start of the next label on the same line (e.g. "KP/1  Dated : ..."). */
  private static final Pattern NEXT_LABEL = Pattern.compile("\\s{2,}(?=[A-Za-z][A-Za-z.\\s]{0,30}?\\s*[:#])");
  /** Column headings printed by Tally-style invoices alongside the seller/buyer block. */
  private static final Pattern COLUMN_LABEL = Pattern.compile("(?i)\\s{1,}(?=(invoice no|e-way bill no|dated|delivery note|mode/terms|reference no|other references|buyer|dispatch doc|dispatched through|destination|bill of lading|motor vehicle no|terms of delivery|consignee|state name|gstin)\\b)");
  private String value(String text, String... labels) { for(String label:labels){ Matcher m=Pattern.compile("(?im)\\b"+labelPattern(label)+"[ \\t]*[:#-]*[ \\t]*([^\\r\\n,;]+)").matcher(text); if(m.find()){ String v=NEXT_LABEL.split(m.group(1).trim())[0].trim(); if(!v.isEmpty()) return v; } } return null; }
  /** Like value() but anchored on a number so grouping commas survive and stray words are skipped.
   *  Stays on the label's own line so column headings can't absorb a value from the row below.
   *  A label immediately followed by "-digits-digits" (e.g. "GST-80-26-27") is an identifier, not a value. */
  private String numberValue(String text, String... labels) {
    for(String label:labels){
      Matcher m=Pattern.compile("(?im)\\b"+labelPattern(label)+"[ \\t]*[:#-]*[ \\t]*(?:(?:rs|inr|₹)\\.?[ \\t]*)?([-+]?\\d[\\d,]*(?:\\.\\d+)?)([ \\t]*-[ \\t]*\\d)?").matcher(text);
      while(m.find()){ if(m.group(2)==null) return m.group(1); }
    }
    return null;
  }
  /** Reads a value printed underneath its column heading, e.g. "Invoice No." on one line and the number on the next. */
  private String columnValue(String text, String label, Pattern valuePattern) {
    String[] lines = text.split("\\r?\\n");
    Pattern labelPattern = Pattern.compile("(?i)\\b" + labelPattern(label) + "\\b");
    for (int i = 0; i < lines.length; i++) {
      if (!labelPattern.matcher(lines[i]).find()) continue;
      for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
        Matcher v = valuePattern.matcher(lines[j]);
        if (v.find()) return v.group(1);
      }
    }
    return null;
  }
  /** Indian registration plates, e.g. "TN88C4146" or "KA 16 B 7788". */
  private String vehiclePlate(String text){
    Matcher m=Pattern.compile("\\b([A-Z]{2}[ -]?\\d{1,2}[ -]?[A-Z]{1,3}[ -]?\\d{4})\\b").matcher(text);
    return m.find()?m.group(1).replaceAll("[ -]",""):null;
  }
  /** Last money value on a GST line, e.g. "IGST @ 5% 5 % 68,067.45". */
  private BigDecimal taxLineAmount(String text){
    BigDecimal found=null;
    for(String line:text.split("\\r?\\n")){
      if(!Pattern.compile("(?i)^\\s*(?:i|c|s|ut)?gst\\b").matcher(line).find()) continue;
      BigDecimal last=lastMoney(line);
      if(last!=null) found=last;
    }
    return found;
  }
  /** Grand total printed on a "Total" row, taking the right-most money value (skips the quantity column).
   *  A currency-marked row (e.g. "Total 14,165 kgs ₹ 14,29,416.00") wins over the HSN tax-summary totals. */
  private BigDecimal grandTotal(String text){
    BigDecimal currencyMarked=null, firstTotal=null;
    for(String line:text.split("\\r?\\n")){
      if(!Pattern.compile("(?i)^\\s*total\\b").matcher(line).find()) continue;
      BigDecimal last=lastMoney(line);
      if(last==null) continue;
      if(firstTotal==null) firstTotal=last;
      if(currencyMarked==null&&Pattern.compile("[₹$]|\\bINR\\b").matcher(line).find()) currencyMarked=last;
    }
    return currencyMarked!=null?currencyMarked:firstTotal;
  }
  /** Right-most decimal money amount on a line, ignoring quantities such as "14,165 kgs". */
  private BigDecimal lastMoney(String line){
    Matcher m=Pattern.compile("(?<![.,\\d])(\\d[\\d,]*\\.\\d{2})(?![\\d.])").matcher(line);
    String last=null; while(m.find()) last=m.group(1);
    return last==null?null:new BigDecimal(last.replace(",",""));
  }
  /** Reads a rate quoted inside an invoice line-item row, e.g. "14,165 kgs  96.11 kgs  13,61,349.00". */
  private BigDecimal inlineRate(String text){
    Matcher m=Pattern.compile("(?im)\\d[\\d,]*(?:\\.\\d+)?\\s*(kgs?|qtl|quintals?|mt|bales?)\\s+([-+]?\\d[\\d,]*(?:\\.\\d+)?)\\s*(?:/?\\s*(?:kgs?|qtl|quintals?|mt|bales?)\\b|per\\b)").matcher(text);
    return m.find()?new BigDecimal(m.group(2).replace(",","")):null;
  }
  /** Reads a billed quantity from an invoice line-item row, e.g. "Raw Cotton 52010011 14,165 kgs". */
  private BigDecimal inlineQuantity(String text){
    Matcher m=Pattern.compile("(?im)(?<![.,\\d])(\\d[\\d,]*(?:\\.\\d+)?)\\s*(?:kgs?|qtl|quintals?|mt|bales?)\\b").matcher(text);
    return m.find()?new BigDecimal(m.group(1).replace(",","")):null;
  }
  private BigDecimal decimal(String v){ if(v==null)return null; Matcher m=Pattern.compile("[-+]?\\d+(?:,\\d{2,3})*(?:\\.\\d+)?").matcher(v); return m.find()?new BigDecimal(m.group().replace(",","")):null; }
  private LocalDate date(String v){
    if(v==null)return null;
    Matcher m=Pattern.compile("(\\d{1,2})[-/ ]([A-Za-z]{3,})[-/ ](\\d{2,4})").matcher(v);
    if(m.find()){
      String month=m.group(2);
      String norm=m.group(1)+"-"+month.substring(0,1).toUpperCase(Locale.ENGLISH)+month.substring(1,Math.min(3,month.length())).toLowerCase(Locale.ENGLISH)+"-"+m.group(3);
      for(DateTimeFormatter f:ALPHA_DATE_FORMATS){try{return LocalDate.parse(norm,f);}catch(DateTimeParseException ignored){}}
    }
    for(DateTimeFormatter f:NUMERIC_DATE_FORMATS){try{return LocalDate.parse(v.replaceAll("[^0-9/-]","").trim(),f);}catch(DateTimeParseException ignored){}}
    return null;
  }
  /** Fallback: the first line that looks like a seller/company name when no explicit supplier label exists. */
  private String companyLine(String text){
    for(String raw:text.split("\\r?\\n")){
      String line=COLUMN_LABEL.split(raw.trim())[0].trim();
      if(line.isEmpty()||line.length()>120) continue;
      if(Pattern.compile("(?i)(gstin|challan|weight|vehicle|lot|godown|^rate|^amount|^total|consignee|buyer|ship to|bill to)").matcher(line).find()) continue;
      if(Pattern.compile("(?i)(\\b(pvt|private|ltd|limited|llp|inc|company|corporation|industries|traders|trading|enterprise|enterprises|cotton|ginning|agro|mills|sons|brothers)\\b|&)").matcher(line).find()) return line.replaceAll("[,;]+$","").trim();
    }
    return null;
  }
  public ExtractionResult parse(String text){
    String supplier=value(text,"supplier name","vendor name","seller name","supplier","vendor","seller");
    if(supplier==null) supplier=companyLine(text); else supplier=COLUMN_LABEL.split(supplier)[0].trim();

    String invoice=value(text,"invoice number","invoice no","bill number","bill no");
    if(invoice!=null) invoice=COLUMN_LABEL.split(invoice)[0].trim();
    if(invoice==null||!invoice.matches(".*\\d.*")) invoice=columnValue(text,"invoice no",Pattern.compile("\\b([A-Z0-9]+(?:[-/][A-Z0-9]+){1,4})\\b"));

    String date=value(text,"invoice date","invoice dated","dated","date");
    LocalDate parsedDate=date(date);
    if(parsedDate==null) parsedDate=date(columnValue(text,"dated",Pattern.compile("(\\d{1,2}[-/][A-Za-z]{3,}[-/]\\d{2,4})")));
    if(parsedDate==null){ Matcher dm=Pattern.compile("(\\d{1,2}[-/][A-Za-z]{3,}[-/]\\d{2,4})").matcher(text); if(dm.find()) parsedDate=date(dm.group(1)); }

    String vehicle=value(text,"motor vehicle no","vehicle number","vehicle no","truck no");
    if(vehicle==null||vehiclePlate(vehicle)==null) vehicle=vehiclePlate(text);

    BigDecimal gross=decimal(numberValue(text,"gross weight","gross wt","gross")), tare=decimal(numberValue(text,"less tare","tare weight","tare wt","tare")), net=decimal(numberValue(text,"net kapas weight","net weight","net wt","kapas weight"));
    BigDecimal numberOfBags=decimal(numberValue(text,"number of bags","no of bags","no. of bags","bags","bag count"));
    String lot=value(text,"lot number","lot no","lot"), godown=value(text,"godown name","godown","warehouse");
    if(net==null&&gross!=null&&tare!=null&&gross.subtract(tare).signum()>=0) net=gross.subtract(tare);
    BigDecimal quantity=decimal(numberValue(text,"quantity","qty")); if(quantity==null) quantity=inlineQuantity(text); if(quantity==null) quantity=net; if(quantity==null) quantity=decimal(numberValue(text,"weight"));
    BigDecimal rate=decimal(numberValue(text,"unit rate","rate per","rate")); if(rate==null){ rate=inlineRate(text); }
    BigDecimal tax=decimal(numberValue(text,"tax amount","total tax")); if(tax==null) tax=taxLineAmount(text); if(tax==null) tax=decimal(numberValue(text,"tax"));
    BigDecimal total=decimal(numberValue(text,"total amount","net amount","grand total","amount")); if(total==null) total=grandTotal(text); if(total==null) total=decimal(numberValue(text,"total"));
    Map<String,Object> data=new LinkedHashMap<>(); if(supplier!=null)data.put("supplierName",supplier); if(invoice!=null)data.put("invoiceNumber",invoice); if(vehicle!=null)data.put("vehicleNumber",vehicle);
    if(parsedDate!=null)data.put("invoiceDate",parsedDate.toString()); if(quantity!=null)data.put("quantity",quantity); if(rate!=null)data.put("rate",rate); if(tax!=null)data.put("tax",tax); if(total!=null)data.put("totalAmount",total);
    if(gross!=null)data.put("grossWeight",gross); if(tare!=null)data.put("tareWeight",tare); if(net!=null)data.put("netWeight",net); if(numberOfBags!=null)data.put("numberOfBags",numberOfBags); if(lot!=null)data.put("lotNumber",lot); if(godown!=null)data.put("godown",godown);
    int found=(supplier!=null?1:0)+(invoice!=null?1:0)+(parsedDate!=null?1:0)+(vehicle!=null?1:0)+(quantity!=null?1:0)+(rate!=null?1:0)+(tax!=null?1:0)+(total!=null?1:0);
    return new ExtractionResult(supplier,invoice,parsedDate,vehicle,quantity,rate,tax,total,data,BigDecimal.valueOf(found/8.0));
  }
}
