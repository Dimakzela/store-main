package com.example.store.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super("%s with ID %s not found".formatted(resource, id));
    }
}
