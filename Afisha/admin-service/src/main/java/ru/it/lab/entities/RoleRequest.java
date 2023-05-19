package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RoleRequest extends AbstractEntity {
    @Column
    private String username;
    @Column
    private LocalDateTime creationTime;
    @Column
    private Long roleId;
}
