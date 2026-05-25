package de.fhdortmund.mystudyapp.common.exception;

public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(String message) {
        super(message);
    }

    public CapacityExceededException(int maxCapacity) {
        super(String.format("Event has reached its maximum capacity of %d attendees", maxCapacity));
    }
}