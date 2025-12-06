package com.furniture.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.furniture.modal.Category;
import com.furniture.modal.Deal;
import com.furniture.repository.CategoryRepository;
import com.furniture.repository.DealRepository;
import com.furniture.service.DealService;

import lombok.NonNull;
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
    public Deal createDeal(@NonNull Deal deal) {

        if (deal.getCategory() == null || deal.getCategory().getId() == null) {
             throw new IllegalArgumentException("Category or Category ID cannot be null");
        }
        Category category = null; // Declare category here
        if (deal.getCategory() != null && deal.getCategory().getId() != null) {
             Long categoryId = deal.getCategory().getId();
             category = categoryRepository.findById(Objects.requireNonNull(categoryId)).orElse(null);
        }
        Deal newDeal = dealRepository.save(deal);
        newDeal.setCategory(category);
        newDeal.setDiscount(deal.getDiscount());
        newDeal.setImage(deal.getImage());
        return dealRepository.save(newDeal);
    }

    @Override
    public Deal updateDeal(@NonNull Deal deal, @NonNull Long id) throws Exception {
        Deal existingDeal = dealRepository.findById(id).orElse(null);
        Category category = null;
        if (deal.getCategory() != null && deal.getCategory().getId() != null) {
            Long categoryId = deal.getCategory().getId();
            category = categoryRepository.findById(Objects.requireNonNull(categoryId)).orElse(null);
        }

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
    public void deleteDeal(@NonNull Long id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));
        if (deal != null) {
            dealRepository.delete(deal);
        }
    }

    @Override
    public void bulkDeleteDeals(@NonNull List<Long> ids) {
        dealRepository.deleteAllById(ids);
    }
}
