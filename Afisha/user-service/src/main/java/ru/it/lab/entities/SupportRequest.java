package ru.it.lab.entities;

import com.sun.istack.NotNull;
import lombok.*;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="support_requests")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SupportRequest extends AbstractEntity {
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column
    private String question;
    @Column
    private String answer;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime creationTime;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime closeTime;
}
