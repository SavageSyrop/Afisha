package ru.it.lab.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_approval_requests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventApprovalRequest extends AbstractEntity {
    @Column
    private Long eventId;
    @Column
    private Long organizerId;
    @Column
    private LocalDateTime creationTime;
}
