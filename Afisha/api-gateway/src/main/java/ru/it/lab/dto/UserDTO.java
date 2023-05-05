package ru.it.lab.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.it.lab.entitities.Role;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String password;
    private String username;
    private String email;
    private Integer age;
    private Role role;
    private Boolean isOpenProfile;
}
