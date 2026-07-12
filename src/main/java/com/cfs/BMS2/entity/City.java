package com.cfs.BMS2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cities")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String state;

}
