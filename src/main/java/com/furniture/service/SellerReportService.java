package com.furniture.service;

import com.furniture.modal.Seller;
import com.furniture.modal.SellerReport;

public interface SellerReportService {

    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport(SellerReport sellerReport);
}
