package ru.it.lab.entitities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.it.lab.enums.RoleType;

import java.util.List;


@NoArgsConstructor
@Getter
@Setter
public class Role extends AbstractEntity {
    private RoleType name;
    private List<Permission> permissions;
}