package ru.it.lab.entitities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.it.lab.enums.RoleType;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="roles")
@NoArgsConstructor
@Getter
@Setter
public class Role extends AbstractEntity {
    @Enumerated(EnumType.STRING)
    @Column
    private RoleType name;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<Permission> permissions;
}