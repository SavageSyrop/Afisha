package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.it.lab.enums.EventParticipationType;
import ru.it.lab.enums.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="event_participations")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class EventParticipation extends AbstractEntity {
    @Column
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Type( type = "pgsql_enum" )
    @Column(name = "participation")
    @Enumerated(EnumType.STRING)
    private EventParticipationType participationType;
}
