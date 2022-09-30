package org.example.service;

import software.amazon.awssdk.services.rekognition.model.FaceDetection;

import java.util.List;

public class MovementService {

    private static final float YAW_THRESHOLD = 25;
    private static final float PITCH_THRESHOLD = 25;
    private static final float MOVEMENT_THRESHOLD = 2;

    private enum MovementType {
        YAW_RIGHT, YAW_LEFT, PITCH_UP, PITCH_DOWN
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
                System.out.println("There is a smile : " + smile);
                System.out.println(face.face().pose());
        });
        Movement yaw = hasMovedYaw(faces);
        if(yaw != null)
            System.out.println(yaw.toString());
        Movement pitch = hasMovedPitch(faces);
        if(pitch != null)
            System.out.println(pitch.toString());
    }

    private Movement hasMovedYaw(List<FaceDetection> faces) {
        int startTimestamp = 0;
        int endTimestamp = 0;
        float beforeValue = 0;
        int nbInc = 0;
        int nbDown = 0;
        boolean thresholdReached = false;
        boolean isNegative = false;
        for(FaceDetection face : faces) {
            float rawYaw = face.face().pose().yaw();
            float yaw = Math.abs(rawYaw);
            if(beforeValue < yaw && Math.abs(beforeValue - yaw) > MOVEMENT_THRESHOLD) {
                beforeValue = yaw;
                nbInc++;
                if(startTimestamp == 0)
                    startTimestamp = face.timestamp().intValue();
            }
            else if(beforeValue > yaw && Math.abs(yaw - beforeValue) > MOVEMENT_THRESHOLD) {
                beforeValue = yaw;
                nbDown++;
                if(endTimestamp == 0)
                    endTimestamp = face.timestamp().intValue();
            }
            if(yaw > YAW_THRESHOLD) {
                thresholdReached = true;
                isNegative = rawYaw < 0;
            }
        }
        return thresholdReached && nbInc >= 2 && nbDown >= 2 ? new Movement(isNegative ? MovementType.YAW_LEFT : MovementType.YAW_RIGHT, startTimestamp, endTimestamp) : null;
    }


    private Movement hasMovedPitch(List<FaceDetection> faces) {
        int startTimestamp = 0;
        int endTimestamp = 0;
        float beforeValue = 0;
        int nbInc = 0;
        int nbDown = 0;
        boolean thresholdReached = false;
        boolean isNegative = false;
        for(FaceDetection face : faces) {
            float rawPitch = face.face().pose().pitch();
            float pitch = Math.abs(rawPitch);
            if(beforeValue < pitch && Math.abs(beforeValue - pitch) > MOVEMENT_THRESHOLD) {
                beforeValue = pitch;
                nbInc++;
                if(startTimestamp == 0)
                    startTimestamp = face.timestamp().intValue();
            }
            else if(beforeValue > pitch && Math.abs(pitch - beforeValue) > MOVEMENT_THRESHOLD) {
                beforeValue = pitch;
                nbDown++;
                if(endTimestamp == 0)
                    endTimestamp = face.timestamp().intValue();
            }
            if(pitch > PITCH_THRESHOLD) {
                thresholdReached = true;
                isNegative = rawPitch < 0;
            }
        }
        return thresholdReached && nbInc >= 2 && nbDown >= 2 ? new Movement(isNegative ? MovementType.PITCH_DOWN : MovementType.PITCH_UP, startTimestamp, endTimestamp) : null;
    }

}
