package com.rwa.common.exception;

public class ResourceNotFoundException extends RwaBaseException {
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with ID: %s", resource, id));
    }
}
