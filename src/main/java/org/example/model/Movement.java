package org.example.model;

import org.example.service.MovementService;

public class Movement {
    private MovementType movementType;
    private int startTimestamp;
    private int endTimestamp;

    public Movement(MovementType movementType, int startTimestamp, int endTimestamp) {
        this.movementType = movementType;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public int getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(int startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public int getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(int endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movement movement = (Movement) o;

        if (startTimestamp != movement.startTimestamp) return false;
        if (endTimestamp != movement.endTimestamp) return false;
        return movementType == movement.movementType;
    }

    @Override
    public int hashCode() {
        int result = movementType != null ? movementType.hashCode() : 0;
        result = 31 * result + startTimestamp;
        result = 31 * result + endTimestamp;
        return result;
    }

    @Override
    public String toString() {
        return "Movement{" +
                "movementType=" + movementType +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                '}';
    }
}
