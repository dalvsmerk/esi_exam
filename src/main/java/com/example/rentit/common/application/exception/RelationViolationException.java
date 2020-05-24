package com.example.rentit.common.application.exception;

public abstract class RelationViolationException extends Exception {
    public RelationViolationException(String reason) { super(reason); }
}
