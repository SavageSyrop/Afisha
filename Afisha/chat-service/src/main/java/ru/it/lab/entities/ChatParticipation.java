package ru.it.lab.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_participations")
public class ChatParticipation extends AbstractEntity {

    @Column
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}
