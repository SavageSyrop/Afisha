package ru.it.lab.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message extends AbstractEntity {
    @Column
    private Long senderId;

    @Column
    private String text;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime sendingTime;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}
