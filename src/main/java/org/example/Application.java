package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.*;
import org.example.service.AnalysisService;
import org.example.service.DownloadService;
import org.example.service.MovementService;
import org.example.service.UploadService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.model.FaceDetection;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Application {

    public static final String REKO_BUCKET_NAME = "seniuus-test-rekognition-uploaded-video-us";
//    public static final String MOVEMENT_VIDEO = "G:\\Workspace\\Cleardil\\20220329_205229.mp4";
//    public static final String MOVEMENT_VIDEO = "E:\\Pictures\\Camera Roll\\WIN_20221005_21_25_43_Pro.mp4";
//    public static final String MOVEMENT_VIDEO = "E:\\Pictures\\Camera Roll\\WIN_20221019_16_45_20_Pro.mp4";
    public static final String MOVEMENT_VIDEO = "E:\\response.webm";
//    public static final String MOVEMENT_VIDEO = "F:\\Download\\VID_20221012_200747.mp4";
//    public static final String MOVEMENT_VIDEO = "D:\\Pictures\\Camera Roll\\WIN_20220930_11_29_05_Pro.mp4";
    public static final String TRAN_BUCKET_NAME = "seniuus-test-transcribe-uploaded-video-us";
    public static final String VOICE_VIDEO = "G:\\Workspace\\Cleardil\\20220911_2003232.mp4";
//    public static final String VOICE_VIDEO = "E:\\Pictures\\Camera Roll\\20221019_1755192.mp4";
    public static Region REGION = Region.US_WEST_2;

    public static void main(String[] args) throws JsonProcessingException {
        runForReko();
//        runForTranscribe();
    }

    private static void runForTranscribe() throws JsonProcessingException {
        Path videoPath = Paths.get(VOICE_VIDEO);
        String bucket = TRAN_BUCKET_NAME;

        UploadService uploadService = new UploadService(REGION, bucket);
        DownloadService downloadService = new DownloadService();
        AnalysisService analysisService = new AnalysisService(REGION, bucket);

        try {
            videoPath = uploadService.upload(videoPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String resultUri = analysisService.startVoiceDetection(videoPath);
        if(!StringUtils.isEmpty(resultUri)) {
            String value = downloadService.downloadFile(resultUri);
            System.out.println(value);
            AmazonTranscription transcription = new ObjectMapper().readValue(value, AmazonTranscription.class);
            for(AmazonTranscriptionItem item : transcription.getResults().getItems()) {
                for(AmazonTranscriptionAlternative alt : item.getAlternatives()) {
                    System.out.println(alt.getContent());
                }
            }
        }
    }

    private static void runForReko() {
        Path videoPath = Paths.get(MOVEMENT_VIDEO);
        String bucket = REKO_BUCKET_NAME;

        UploadService uploadService = new UploadService(REGION, bucket);
        AnalysisService analysisService = new AnalysisService(REGION, bucket);
        MovementService movementService = new MovementService();

        try {
            videoPath = uploadService.upload(videoPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<FaceDetection> faces = analysisService.startFaceDetection(videoPath);
        if(faces != null & faces.size() > 0) {
            List<Movement> movements = movementService.detectMovement(faces);
            for(Movement movement : movements) {
                System.out.println(movement.toString());
            }
        }
    }
}
