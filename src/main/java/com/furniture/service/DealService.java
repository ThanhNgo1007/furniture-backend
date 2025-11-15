package com.furniture.service;

import com.furniture.modal.Deal;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DealService {
    List<Deal> getDeals();
    Deal createDeal(Deal deal);
    Deal updateDeal(Deal deal, Long id) throws Exception;
    void deleteDeal(Long id) throws Exception;


}
