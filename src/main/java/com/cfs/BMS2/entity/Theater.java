package com.cfs.BMS2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theaters")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}
