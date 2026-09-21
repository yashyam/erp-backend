package com.ginning.erp.common.exception;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found: %s", resourceName, identifier), 
              404);
    }

    public ResourceNotFoundException(String resourceName) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found", resourceName), 
              404);
    }

}
