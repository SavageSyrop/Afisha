package ru.it.lab.dao;


import ru.it.lab.entities.Role;
import ru.it.lab.entities.SupportRequest;

import java.util.List;

public interface SupportRequestDao extends AbstractDao<SupportRequest>{
    List<SupportRequest> getAllByUser(String username);

    List<SupportRequest> getAllOpenRequests();
}
