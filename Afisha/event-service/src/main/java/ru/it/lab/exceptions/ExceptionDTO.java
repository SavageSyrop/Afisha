package ru.it.lab.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@AllArgsConstructor
@Getter
public class ExceptionDTO {
    @Enumerated(EnumType.STRING)
    private Integer status;
    private String error;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Date date;
    private String message;
    private String path;
}
