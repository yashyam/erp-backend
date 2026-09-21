package com.ginning.erp.bale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BaleDailySummaryResponse {
    private LocalDate productionDate;
    private String lotNumber;
    private long totalBales;
    private long startSerialNo;
    private long endSerialNo;
    private String firstBaleNumber;
    private String lastBaleNumber;
    private BigDecimal totalBaleWeight;
    private BigDecimal totalQuintals;
    private BigDecimal totalCandy;

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public long getTotalBales() { return totalBales; }
    public void setTotalBales(long totalBales) { this.totalBales = totalBales; }

    public long getStartSerialNo() { return startSerialNo; }
    public void setStartSerialNo(long startSerialNo) { this.startSerialNo = startSerialNo; }

    public long getEndSerialNo() { return endSerialNo; }
    public void setEndSerialNo(long endSerialNo) { this.endSerialNo = endSerialNo; }

    public String getFirstBaleNumber() { return firstBaleNumber; }
    public void setFirstBaleNumber(String firstBaleNumber) { this.firstBaleNumber = firstBaleNumber; }

    public String getLastBaleNumber() { return lastBaleNumber; }
    public void setLastBaleNumber(String lastBaleNumber) { this.lastBaleNumber = lastBaleNumber; }

    public BigDecimal getTotalBaleWeight() { return totalBaleWeight; }
    public void setTotalBaleWeight(BigDecimal totalBaleWeight) { this.totalBaleWeight = totalBaleWeight; }

    public BigDecimal getTotalQuintals() { return totalQuintals; }
    public void setTotalQuintals(BigDecimal totalQuintals) { this.totalQuintals = totalQuintals; }

    public BigDecimal getTotalCandy() { return totalCandy; }
    public void setTotalCandy(BigDecimal totalCandy) { this.totalCandy = totalCandy; }
}
