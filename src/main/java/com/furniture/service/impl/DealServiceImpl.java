package com.furniture.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.modal.Category;
import com.furniture.modal.Deal;
import com.furniture.repository.CategoryRepository;
import com.furniture.repository.DealRepository;
import com.furniture.service.DealService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Deal> getDeals() {
        return dealRepository.findAll();
    }

    @Override
    public Deal createDeal(Deal deal) {

        Category category = categoryRepository.findById(deal.getCategory().getId()).orElse(null);
        Deal newDeal = dealRepository.save(deal);
        newDeal.setCategory(category);
        newDeal.setDiscount(deal.getDiscount());
        newDeal.setImage(deal.getImage());
        return dealRepository.save(newDeal);
    }

    @Override
    public Deal updateDeal(Deal deal, Long id) throws Exception {
        Deal existingDeal = dealRepository.findById(id).orElse(null);
        Category category = categoryRepository.findById(deal.getCategory().getId()).orElse(null);

        if (existingDeal != null) {
            if(deal.getDiscount() != null){
                existingDeal.setDiscount(deal.getDiscount());
            }
            if(category != null){
                existingDeal.setCategory(category);
            }
            if(deal.getImage() != null){
                existingDeal.setImage(deal.getImage());
            }
            return dealRepository.save(existingDeal);
        }
        throw new Exception("Deal not found");
    }

    @Override
    public void deleteDeal(Long id) throws Exception {
        Deal deal = dealRepository.findById(id).orElseThrow(()->
                new Exception("Deal not found"));
        dealRepository.delete(deal);

    }
}
