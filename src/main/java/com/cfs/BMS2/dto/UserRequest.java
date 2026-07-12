package com.cfs.BMS2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String name;
    private String email;
    private String password;
    private String phone;
}
