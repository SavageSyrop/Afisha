package ru.it.lab.entitities;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


public abstract class AbstractEntity implements Serializable {
    @Getter
    @Setter
    protected Long id;
}

