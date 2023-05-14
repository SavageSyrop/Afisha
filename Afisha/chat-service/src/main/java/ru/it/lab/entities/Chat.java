package ru.it.lab.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chats")
public class Chat extends ru.it.lab.entities.AbstractEntity {

    @Column
    private String name;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chat", cascade = CascadeType.REMOVE)
    private List<Message> messages;


    public Chat(String name) {
        this.name = name;
    }
}
