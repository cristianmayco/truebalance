package com.truebalance.truebalance.domain.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Categoria com ID " + id + " não encontrada");
    }
}
