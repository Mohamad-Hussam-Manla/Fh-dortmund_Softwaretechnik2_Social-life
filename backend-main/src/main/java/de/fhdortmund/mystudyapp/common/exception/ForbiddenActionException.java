package de.fhdortmund.mystudyapp.common.exception;

public class ForbiddenActionException extends RuntimeException {

    public ForbiddenActionException(String message) {
        super(message);
    }

    public ForbiddenActionException(String action, String reason) {
        super(String.format("Action '%s' is forbidden: %s", action, reason));
    }
}