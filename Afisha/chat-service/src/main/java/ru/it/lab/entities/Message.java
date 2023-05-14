package ru.it.lab.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message extends AbstractEntity {
    @OneToOne
    @JoinColumn(columnDefinition = "sender_id")
    private User sender;

    @Column
    private String text;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime sendingTime;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;


    public Message(User sender, String text, Chat chat) {
        this.sender = sender;
        this.text = text;
        this.sendingTime = LocalDateTime.now();
        this.chat = chat;
    }

    public String getSenderName() {
        return this.sender.getUsername();
    }
}
