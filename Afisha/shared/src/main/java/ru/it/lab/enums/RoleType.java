package ru.it.lab.enums;

public enum RoleType {
    ADMIN(1L),
    USER(2L),
    ORGANIZER(3L);

    public final Long id;

    RoleType(Long id) {
        this.id = id;
    }
}
