package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;
import ru.it.lab.enums.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Entity
@Table(name="event_comments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventComment extends AbstractEntity{
    @Column
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Column
    private String comment;
    @Column
    private LocalDateTime creationTime;
}
