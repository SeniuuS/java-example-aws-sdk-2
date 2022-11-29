package org.example.service;

import com.amazonaws.services.rekognition.model.FaceDetection;
import org.example.model.Movement;
import org.example.model.MovementType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MovementService {

    private static final float YAW_THRESHOLD = 50;
    private static final float PITCH_THRESHOLD = 25;
    private static final float MOVEMENT_THRESHOLD = 5;

    public List<Movement> detectMovement(List<FaceDetection> faces) {
        faces.forEach((face) -> {
            String age = face.getFace().getAgeRange().toString();
            String smile = face.getFace().getSmile().toString();
            System.out.println("The detected face is estimated to be"
                    + age + " years old.");
            System.out.println("There is a beard : " + face.getFace().getBeard().toString());
            System.out.println("There is a smile : " + smile);
            System.out.println(face.getFace().getPose());
            System.out.println(face.getTimestamp().intValue());
        });
        List<Movement> movements = new ArrayList<>();
        movements.addAll(getMovementList(faces, MovementType.YAW));
        movements.addAll(getMovementList(faces, MovementType.PITCH));
        movements.sort(Comparator.comparingInt(Movement::getStartTimestamp));
        return movements;
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
            float rawMovementValue = movementType == MovementType.YAW ? face.getFace().getPose().getYaw() : face.getFace().getPose().getPitch();
            float movementValue = Math.abs(rawMovementValue);
            if(beforeValue < movementValue && Math.abs(beforeValue - movementValue) > MOVEMENT_THRESHOLD) {
                beforeValue = movementValue;
                nbInc++;
                if(startTimestamp == 0)
                    startTimestamp = face.getTimestamp().intValue();
            }
            else if(nbInc > 0 && beforeValue > movementValue && Math.abs(movementValue - beforeValue) > MOVEMENT_THRESHOLD) {
                beforeValue = movementValue;
                nbDown++;
                if(endTimestamp == 0)
                    endTimestamp = face.getTimestamp().intValue();
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
