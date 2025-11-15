package com.furniture.modal;


import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Home {

    private List<HomeCategory> grid;

    private List<HomeCategory> shopByCategories;

    private List<HomeCategory> holidayCategories;

    private List<HomeCategory> dealCategories;

    private List<Deal> deals;

}
