package ru.it.lab.entitities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.it.lab.enums.PermissionType;


@Getter
@Setter
@NoArgsConstructor
public class Permission extends AbstractEntity {
    private PermissionType name;
    private Role role;

    public Permission(PermissionType name) {
        this.name = name;
    }
}