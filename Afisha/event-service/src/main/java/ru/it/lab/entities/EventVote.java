package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "event_votes")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventVote extends AbstractEntity {
    @Column
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Column
    private Short voteValue;
}
