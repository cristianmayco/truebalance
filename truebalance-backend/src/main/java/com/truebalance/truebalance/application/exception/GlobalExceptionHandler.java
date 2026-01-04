package com.truebalance.truebalance.application.exception;

import com.truebalance.truebalance.application.dto.ErrorResponse;
import com.truebalance.truebalance.domain.exception.BillNotFoundException;
import com.truebalance.truebalance.domain.exception.CategoryAlreadyExistsException;
import com.truebalance.truebalance.domain.exception.CategoryNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditLimitExceededException;
import com.truebalance.truebalance.domain.exception.InvalidPaymentAmountException;
import com.truebalance.truebalance.domain.exception.InvoiceAlreadyClosedException;
import com.truebalance.truebalance.domain.exception.InvoiceClosedException;
import com.truebalance.truebalance.domain.exception.InvoiceNotFoundException;
import com.truebalance.truebalance.domain.exception.PartialPaymentNotAllowedException;
import com.truebalance.truebalance.domain.exception.PartialPaymentNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CreditCardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCreditCardNotFound(CreditCardNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CreditLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCreditLimitExceeded(CreditLimitExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Conflito detectado: o registro foi modificado por outra transação. Por favor, tente novamente."));
    }

    // Handlers for 404 - Not Found
    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBillNotFound(BillNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PartialPaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePartialPaymentNotFound(PartialPaymentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Handlers for 400 - Bad Request
    @ExceptionHandler(PartialPaymentNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handlePartialPaymentNotAllowed(PartialPaymentNotAllowedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPaymentAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentAmount(InvalidPaymentAmountException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExists(CategoryAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Handlers for 409 - Conflict
    @ExceptionHandler(InvoiceClosedException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceClosed(InvoiceClosedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvoiceAlreadyClosedException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceAlreadyClosed(InvoiceAlreadyClosedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Handler for validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Handler for IllegalStateException (generic business rule violations)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
