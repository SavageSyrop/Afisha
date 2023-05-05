package ru.it.lab.entitities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String email;
    @Column
    private Integer age;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private Boolean isOpenProfile;
}
