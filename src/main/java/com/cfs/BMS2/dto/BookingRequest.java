package com.cfs.BMS2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private Long userId;
    private Long showId;
    private List<Long> seatIds;
}
