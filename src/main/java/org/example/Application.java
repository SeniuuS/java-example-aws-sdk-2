package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.AnalysisService;
import org.example.service.DownloadService;
import org.example.service.UploadService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Application {

    public static final String REKO_BUCKET_NAME = "seniuus-test-rekognition-uploaded-video";
    public static final String MOVEMENT_VIDEO = "C:\\Temp\\video.mp4";
    public static final String TRAN_BUCKET_NAME = "seniuus-test-transcribe-uploaded-video";
    public static final String VOICE_VIDEO = "C:\\Temp\\video.mp4";
    public static Region REGION = Region.EU_WEST_2;

    public static void main(String[] args) {
//        runForReko();
//        runForTranscribe();
    }

    private static void runForTranscribe() {
        Path videoPath = Paths.get(VOICE_VIDEO);
        String bucket = TRAN_BUCKET_NAME;

        UploadService uploadService = new UploadService(REGION, bucket);
        DownloadService downloadService = new DownloadService();
        AnalysisService analysisService = new AnalysisService(REGION, bucket);

//        try {
//            uploadService.upload(videoPath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        String resultUri = analysisService.startVoiceDetection(videoPath);
        if(!StringUtils.isEmpty(resultUri)) {
            String value = downloadService.downloadFile(resultUri);
            System.out.println(value);
        }
    }

    private static void runForReko() {
        Path videoPath = Paths.get(MOVEMENT_VIDEO);
        String bucket = REKO_BUCKET_NAME;

        UploadService uploadService = new UploadService(REGION, bucket);
        AnalysisService analysisService = new AnalysisService(REGION, bucket);

//        try {
//            uploadService.upload(videoPath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        analysisService.startFaceDetection(videoPath);
    }
}
