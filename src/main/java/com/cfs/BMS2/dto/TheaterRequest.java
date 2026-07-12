package com.cfs.BMS2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TheaterRequest {

    private String name;
    private String address;
    private Long cityId;
}
