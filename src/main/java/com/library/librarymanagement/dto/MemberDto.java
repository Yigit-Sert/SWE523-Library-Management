package com.library.librarymanagement.dto;

import lombok.Data;

@Data
public class MemberDto {
    private Long id;
    private String name;
    private String address;
    private String telephone;
}