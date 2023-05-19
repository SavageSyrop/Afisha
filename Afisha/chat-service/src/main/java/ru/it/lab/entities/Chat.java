package ru.it.lab.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chats")
public class Chat extends AbstractEntity {

    @Column
    private String name;

    public Chat(String name) {
        this.name = name;
    }
}
