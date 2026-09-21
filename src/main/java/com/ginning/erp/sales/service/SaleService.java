package com.ginning.erp.sales.service;

import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.common.exception.ResourceNotFoundException;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import com.ginning.erp.kapas.repository.KapasPurchaseEntryRepository;
import com.ginning.erp.masterdata.repository.CustomerRepository;
import com.ginning.erp.sales.dto.*;
import com.ginning.erp.sales.entity.Sale;
import com.ginning.erp.sales.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SaleService {
    private final SaleRepository repository;
    private final KapasPurchaseEntryRepository purchaseRepository;
    private final CustomerRepository customerRepository;
    public SaleService(SaleRepository repository, KapasPurchaseEntryRepository purchaseRepository, CustomerRepository customerRepository) {
        this.repository=repository; this.purchaseRepository=purchaseRepository; this.customerRepository=customerRepository;
    }
    @Transactional(readOnly=true) public List<SaleResponse> list(){return repository.findByDeletedAtIsNullOrderByRawMaterialOutDateDescCreatedAtDesc().stream().map(SaleResponse::from).toList();}
    @Transactional(readOnly=true) public SaleResponse get(UUID id){return SaleResponse.from(find(id));}
    @Transactional(readOnly=true) public SaleResponse lookup(String salesId,String billId) {
        if ((salesId==null || salesId.isBlank()) == (billId==null || billId.isBlank()))
            throw new ApplicationException("LOOKUP_REQUIRED","Provide exactly one of salesId or billId",400);
        return SaleResponse.from(salesId!=null && !salesId.isBlank()
            ? repository.findBySalesIdIgnoreCaseAndDeletedAtIsNull(salesId.trim()).orElseThrow(()->notFound())
            : repository.findByBillIdIgnoreCaseAndDeletedAtIsNull(billId.trim()).orElseThrow(()->notFound()));
    }
    @Transactional(readOnly=true) public List<SaleResponse> lookupByLot(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank())
            throw new ApplicationException("LOT_LOOKUP_REQUIRED", "Provide a lot number", 400);
        return repository.findByLotNumberIgnoreCaseAndDeletedAtIsNullOrderByRawMaterialOutDateDescCreatedAtDesc(lotNumber.trim())
                .stream().map(SaleResponse::from).toList();
    }
    @Transactional public SaleResponse create(SaleRequest r) {
        if(repository.existsByBillIdIgnoreCaseAndDeletedAtIsNull(r.getBillId().trim())) throw new ApplicationException("SALES_BILL_EXISTS","A sale with this bill ID already exists",409);
        KapasPurchaseEntry p=purchaseRepository.findByIdAndDeletedAtIsNull(r.getKapasPurchaseEntryId()).orElseThrow(()->new ResourceNotFoundException("Kapas purchase entry not found"));
        if(r.getRawMaterialOutDate().isBefore(p.getBillDate())) throw new ApplicationException("SALES_INVALID_DATE","Outbound date cannot be before raw material inbound date",400);
        if(r.getCustomerId()!=null && customerRepository.findByIdAndDeletedAtIsNull(r.getCustomerId()).isEmpty()) throw new ResourceNotFoundException("Customer not found");
        Sale s=new Sale(); s.setSalesId(String.format("SAL-%d-%06d",LocalDate.now().getYear(),repository.nextNumber())); s.setBillId(r.getBillId().trim());
        s.setKapasPurchaseEntryId(p.getId()); s.setCustomerId(r.getCustomerId()); s.setCustomerName(r.getCustomerName().trim());
        s.setSupplierId(p.getSupplierId()); s.setSupplierName(p.getSupplierName()); s.setLotNumber(p.getLotNumber()); s.setRawMaterialInDate(p.getBillDate()); s.setRawMaterialOutDate(r.getRawMaterialOutDate());
        s.setRawMaterialNetWeight(p.getNetKapasWeight());
        BigDecimal purchaseCost = p.getAmount();
        if (purchaseCost == null && p.getNetKapasWeight() != null && p.getRate() != null)
            purchaseCost = p.getNetKapasWeight().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP).multiply(p.getRate()).setScale(2, RoundingMode.HALF_UP);
        s.setRawMaterialCost(purchaseCost);
        s.setBaleKg(r.getBaleKg()); s.setSeedKg(r.getSeedKg()); s.setSoldProductWeight(r.getSoldProductWeight()); s.setSalePrice(r.getSalePrice()); s.setSaleRate(r.getSaleRate()); s.setSalesValue(r.getSalesValue()); s.setBuyerContact(trim(r.getBuyerContact())); s.setBuyerAddress(trim(r.getBuyerAddress())); s.setNotes(trim(r.getNotes()));
        if(s.getRawMaterialNetWeight()==null || s.getRawMaterialCost()==null) throw new ApplicationException("SALES_PURCHASE_DATA_MISSING","Linked purchase has no net weight or cost",400);
        return SaleResponse.from(repository.save(s));
    }
    private Sale find(UUID id){return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(()->notFound());}
    private ResourceNotFoundException notFound(){return new ResourceNotFoundException("Sale not found");}
    private static String trim(String v){return v==null||v.isBlank()?null:v.trim();}
}
