package org.aircall.pager.pager.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class c, String identifier) {
        super(String.format("%s %s not found.", c.getCanonicalName(), identifier));
    }
}
