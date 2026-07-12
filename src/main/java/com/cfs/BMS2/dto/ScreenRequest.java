package com.cfs.BMS2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequest {

    private String name;
    private Integer totalSeats;
    private Long theaterId;
}