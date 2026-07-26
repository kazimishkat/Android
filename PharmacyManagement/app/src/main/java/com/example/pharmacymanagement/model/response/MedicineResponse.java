package com.example.pharmacymanagement.model.response;

import com.example.pharmacymanagement.enums.DosageForm;
import com.example.pharmacymanagement.enums.DrugSchedule;
import com.example.pharmacymanagement.enums.StorageCondition;
import com.example.pharmacymanagement.enums.UnitOfMeasure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MedicineResponse {
    private Long id;
    private String medicineCode;
    private String brandName;
    private Long genericMedicineId;
    private String genericName;
    private String manufacturer;
    private DosageForm dosageForm;
    private String strength;
    private UnitOfMeasure unitOfMeasure;
    private Integer unitsPerPack;
    private DrugSchedule drugSchedule;
    private StorageCondition storageCondition;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private BigDecimal defaultPurchasePrice;
    private BigDecimal defaultSellingPrice;
    private Boolean isActive;
    private String image;
}
