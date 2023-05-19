package ru.it.lab.dao;

import ru.it.lab.entities.EventApprovalRequest;

public interface EventApprovalRequestDao extends AbstractDao<EventApprovalRequest> {

    EventApprovalRequest getByEventId(long id);
}
