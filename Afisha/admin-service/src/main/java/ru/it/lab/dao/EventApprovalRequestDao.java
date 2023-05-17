package ru.it.lab.dao;

import ru.it.lab.entities.EventApprovalRequest;
import ru.it.lab.entities.RoleRequest;

public interface EventApprovalRequestDao extends AbstractDao<EventApprovalRequest>{

    EventApprovalRequest getByEventId(long id);
}
