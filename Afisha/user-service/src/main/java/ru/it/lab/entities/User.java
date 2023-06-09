package ru.it.lab.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import ru.it.lab.enums.GenderType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "users")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("dateOfBirth")
    private Date dateOfBirth;
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private GenderType genderType;
    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @Column
    private Boolean isOpenProfile;

    @Column
    private String activationCode;

    @Column
    private String restorePasswordCode;

    @Column
    @ColumnDefault(value = "false")
    private Boolean isBanned;

    public User(String username, String password, String email, Date dateOfBirth, GenderType genderType, Role role, Boolean isOpenProfile) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.genderType = genderType;
        this.role = role;
        this.isOpenProfile = isOpenProfile;
    }
}
