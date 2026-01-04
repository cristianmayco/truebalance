package com.truebalance.truebalance.domain.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String categoryName) {
        super("Categoria com nome '" + categoryName + "' já existe");
    }
}
