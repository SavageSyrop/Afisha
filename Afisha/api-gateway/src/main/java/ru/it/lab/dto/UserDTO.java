package ru.it.lab.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.it.lab.entitities.Role;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String password;
    private String username;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("dateOfBirth")
    private Date dateOfBirth;
    private String genderType;
    private Integer roleId;
    private Boolean isOpenProfile;
}
