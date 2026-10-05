package com.softdv.softdevoluciones.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> manejarReglaDeNegocio(
            ResponseStatusException exception
    ) {
        String mensaje = exception.getReason() != null
                ? exception.getReason()
                : "No se pudo procesar la solicitud";

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(),
                mensaje
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(
            MethodArgumentNotValidException exception
    ) {
        List<String> errores = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()
                        + ": "
                        + error.getDefaultMessage())
                .distinct()
                .toList();

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Revisa los datos enviados"
        );

        error.setProperty("errores", errores);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> manejarJsonInvalido(
            HttpMessageNotReadableException exception
    ) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "El JSON contiene datos inválidos. Revisa los tipos "
                        + "de datos y los valores de estado o motivo"
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> manejarParametroInvalido(
            MethodArgumentTypeMismatchException exception
    ) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Valor inválido para el parámetro: "
                        + exception.getName()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> manejarConflictoDeDatos(
            DataIntegrityViolationException exception
    ) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Los datos entran en conflicto con un registro existente "
                        + "o con las restricciones de la base de datos"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}