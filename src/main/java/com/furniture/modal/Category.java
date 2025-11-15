package com.furniture.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @NotNull
    @Column(unique = true)
    private String categoryId;

    @ManyToOne
    private Category parentCategory;

    @NotNull
    private Integer level;

}
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@EqualsAndHashCode
//public class Category {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    private String name;
//
//    @NotNull
//    @Column(unique = true)
//    private String categoryId;
//
//    @NotNull
//    private Integer level;
//
//    @ManyToMany
//    @JoinTable(
//            name = "category_parent",
//            joinColumns = @JoinColumn(name = "child_id"),
//            inverseJoinColumns = @JoinColumn(name = "parent_id")
//    )
//    private Set<Category> parentCategory = new HashSet<>();
//}
//}

