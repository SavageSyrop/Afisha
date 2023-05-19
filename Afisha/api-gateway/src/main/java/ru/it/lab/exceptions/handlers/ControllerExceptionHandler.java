package ru.it.lab.exceptions.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.AccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.it.lab.exceptions.AuthorizationErrorException;
import ru.it.lab.exceptions.ExceptionDTO;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(Exception exception, HttpServletRequest request) {
        return constructResponseEntity(exception, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({AuthorizationErrorException.class, AccessException.class})
    public ResponseEntity<Object> handleAuthorizationFail(Exception exception, HttpServletRequest request) {
        return constructResponseEntity(exception, HttpStatus.UNAUTHORIZED, request);
    }


    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Object> handleGRPCException(StatusRuntimeException e, HttpServletRequest request) {
        return constructResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegal(IllegalArgumentException e, HttpServletRequest request) {
        return constructResponseEntity(e, HttpStatus.BAD_REQUEST, request);
    }


    private ResponseEntity<Object> constructResponseEntity(Exception exception, HttpStatus httpStatus, HttpServletRequest request) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(gson.toJson(new ExceptionDTO(httpStatus.value(), httpStatus.name(), Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()), exception.getMessage(), request.getRequestURI())), httpStatus);
    }
}
