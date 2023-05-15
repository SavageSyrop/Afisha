package ru.it.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RoleRequestDTO {
    private String username;
    private Long creation_time;
    private Long roleId;
}
