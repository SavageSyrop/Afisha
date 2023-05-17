package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.it.lab.enums.EventType;
import ru.it.lab.enums.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Entity
@Table(name="events")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
public class Event extends AbstractEntity{
    @Column
    private Long organizerId;
    @Type( type = "pgsql_enum" )
    @Column
    @Enumerated(EnumType.STRING)
    private EventType type;
    @Column
    private String name;
    @Column
    private String info;
    @Column
    private Integer price;
    @Column
    private LocalDateTime startTime;
    @Column
    private String location;
    @Column
    private Float rating;
    @Column
    private Boolean isAccepted;
}
