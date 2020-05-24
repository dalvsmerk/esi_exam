package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.RelationViolationException;

public class MaintenanceTaskScheduleException extends RelationViolationException {
    public MaintenanceTaskScheduleException(String reason) { super(reason); }
}
