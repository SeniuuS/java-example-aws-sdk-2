package org.example.service;

import software.amazon.awssdk.services.rekognition.model.FaceDetection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MovementService {

    private static final float YAW_THRESHOLD = 55;
    private static final float PITCH_THRESHOLD = 40;
    private static final float MOVEMENT_THRESHOLD = 5;

    private enum MovementType {
        YAW_RIGHT, YAW_LEFT, PITCH_UP, PITCH_DOWN, YAW, PITCH
    }

    private class Movement {
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

    public void detectMovement(List<FaceDetection> faces) {
        faces.forEach((face) -> {
            String age = face.face().ageRange().toString();
            String smile = face.face().smile().toString();
            System.out.println("The detected face is estimated to be"
                    + age + " years old.");
            System.out.println("There is a beard : " + face.face().beard().toString());
            System.out.println("There is a smile : " + smile);
            System.out.println(face.face().pose());
            System.out.println(face.timestamp().intValue());
        });
        List<Movement> movements = new ArrayList<>();
        movements.addAll(getMovementList(faces, MovementType.YAW));
        movements.addAll(getMovementList(faces, MovementType.PITCH));
        movements.sort(Comparator.comparingInt(Movement::getStartTimestamp));
        movements.forEach((movement -> System.out.println(movement.toString())));
    }

    private List<Movement> getMovementList(List<FaceDetection> faces, MovementType movementType) {
        List<Movement> movements = new ArrayList<>();
        int startTimestamp = 0;
        int endTimestamp = 0;
        float beforeValue = 0;
        int nbInc = 0;
        int nbDown = 0;
        boolean thresholdReached = false;
        boolean isNegative = false;
        for(FaceDetection face : faces) {
            boolean reset = false;
            float rawMovementValue = movementType == MovementType.YAW ? face.face().pose().yaw() : face.face().pose().pitch();
            float movementValue = Math.abs(rawMovementValue);
            if(beforeValue < movementValue && Math.abs(beforeValue - movementValue) > MOVEMENT_THRESHOLD) {
                beforeValue = movementValue;
                nbInc++;
                if(startTimestamp == 0)
                    startTimestamp = face.timestamp().intValue();
            }
            else if(nbInc > 0 && beforeValue > movementValue && Math.abs(movementValue - beforeValue) > MOVEMENT_THRESHOLD) {
                beforeValue = movementValue;
                nbDown++;
                if(endTimestamp == 0)
                    endTimestamp = face.timestamp().intValue();
            }
            if(movementValue > (movementType == MovementType.YAW ? YAW_THRESHOLD : PITCH_THRESHOLD)) {
                thresholdReached = true;
                isNegative = rawMovementValue < 0;
            }
            if(nbInc >= 1 && nbDown >= 1){
                if(thresholdReached) {
                    movements.add(new Movement(isNegative ?
                            (movementType == MovementType.YAW ? MovementType.YAW_LEFT : MovementType.PITCH_DOWN) :
                            (movementType == MovementType.YAW ? MovementType.YAW_RIGHT : MovementType.PITCH_UP),
                            startTimestamp,
                            endTimestamp));
                }
                reset = true;
            }
            if(reset) {
                thresholdReached = false;
                isNegative = false;
                nbInc = 0;
                nbDown = 0;
                startTimestamp = 0;
                endTimestamp = 0;
            }
        }
        return movements;
    }
}
