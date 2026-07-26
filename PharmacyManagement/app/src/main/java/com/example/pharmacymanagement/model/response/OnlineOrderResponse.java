package com.example.pharmacymanagement.model.response;

import com.example.pharmacymanagement.enums.OnlineOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OnlineOrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private String branchName;
    private Long prescriptionId;
    private OnlineOrderStatus status;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String paymentStatus;
    private String paymentTransactionId;
    private String deliveryAddress;
    private List<OnlineOrderItemResponseDto> items;

    // ── 🟢 আপনার যুক্ত করা রিলেশন ট্র্যাকিংয়ের জন্য ডেলিভারি ফিল্ডসমূহ ──
    private String deliveryCompanyName; // e.g., Pathao
    private String trackingNumber;      // কুরিয়ার কোম্পানির ট্র্যাকিং আইডি
    private String deliveryStatus;      // e.g., IN_TRANSIT, DELIVERED

    @Data
    public static class OnlineOrderItemResponseDto {
        private Long id;
        private Long medicineId;
        private String medicineBrandName;
        private String medicineGenericName; // ওমনিচ্যানেল জেনেরিক ট্র্যাকিং
        private Integer quantity;
        private Double pricePerUnit;
        private Double lineTotal; // quantity * pricePerUnit (সার্ভিস লেয়ারে ক্যালকুলেট হবে)
    }
}
