package ru.it.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupportRequestDTO {
    private String question;
    private String answer;
    private Long userId;
    private Long adminId;
    private LocalDateTime creationTime;
    private LocalDateTime closeTime;
}
