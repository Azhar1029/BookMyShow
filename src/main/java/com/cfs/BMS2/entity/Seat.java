package com.cfs.BMS2.entity;

import com.cfs.BMS2.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber;

    @Column(name = "seat_row")
    private String row; // A B C

    @Column(name = "seat_col")
    private Integer col; // 1 2 3

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

}
