package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.it.lab.enums.PermissionType;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends AbstractEntity {


    private PermissionType name;

    private Role role;

    public Permission(PermissionType name) {
        this.name = name;
    }
}