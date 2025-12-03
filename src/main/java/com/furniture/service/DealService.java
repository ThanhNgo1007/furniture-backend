package com.furniture.service;

import java.util.List;

import com.furniture.modal.Deal;

public interface DealService {
    List<Deal> getDeals();
    Deal createDeal(Deal deal);
    Deal updateDeal(Deal deal, Long id) throws Exception;
    void deleteDeal(Long id);
    void bulkDeleteDeals(List<Long> ids);

}
