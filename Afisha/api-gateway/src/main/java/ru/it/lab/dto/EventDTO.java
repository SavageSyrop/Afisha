package ru.it.lab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    @NotNull
    private String type;
    @NotNull
    private String name;
    @NotNull
    private String info;
    @NotNull
    private Integer price;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime startTime;
    @NotNull
    private String location;
}
