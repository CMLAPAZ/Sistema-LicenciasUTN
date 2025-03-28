package com.licencias.exception;
public class EntityNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L; // Agregar esto

    public EntityNotFoundException(String message) {
        super(message);
    }
}
