package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalysisService {

    private Region region;
    private String bucket;

    private String startJobId = "";

    public AnalysisService(Region region, String bucket) {
        this.region = region;
        this.bucket = bucket;
    }

    public List<FaceDetection> startFaceDetection(Path videoPath) {
        List<FaceDetection> result = null;

        RekognitionClient rekClient = RekognitionClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        try {
            S3Object s3Obj = S3Object.builder()
                    .bucket(this.bucket)
                    .name(videoPath.getFileName().toString())
                    .build();

            Video vidOb = Video.builder()
                    .s3Object(s3Obj)
                    .build();

            StartFaceDetectionRequest faceDetectionRequest = StartFaceDetectionRequest.builder()
                    .jobTag("Faces")
                    .faceAttributes(FaceAttributes.ALL)
                    .video(vidOb)
                    .build();

            StartFaceDetectionResponse startLabelDetectionResult = rekClient.startFaceDetection(faceDetectionRequest);
            startJobId = startLabelDetectionResult.jobId();

            result = getFaceResults(rekClient);

            rekClient.close();
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return result;
    }

    private List<FaceDetection> getFaceResults(RekognitionClient rekClient) {
        List<FaceDetection> result = new ArrayList<>();

        try {
            String paginationToken = null;
            GetFaceDetectionResponse faceDetectionResponse = null;
            boolean finished = false;
            String status;
            int yy = 0;

            do {
                if (faceDetectionResponse != null)
                    paginationToken = faceDetectionResponse.nextToken();

                GetFaceDetectionRequest recognitionRequest = GetFaceDetectionRequest.builder()
                        .jobId(startJobId)
                        .nextToken(paginationToken)
                        .maxResults(10)
                        .build();

                // Wait until the job succeeds
                while (!finished) {
                    faceDetectionResponse = rekClient.getFaceDetection(recognitionRequest);
                    status = faceDetectionResponse.jobStatusAsString();

                    if (status.compareTo("SUCCEEDED") == 0 || status.compareTo("FAILED") == 0)
                        finished = true;
                    else {
                        System.out.println(yy + " status is: " + status);
                        Thread.sleep(1000);
                    }
                    yy++;
                }

                finished = false;

                // Proceed when the job is done - otherwise VideoMetadata is null
                VideoMetadata videoMetaData = faceDetectionResponse.videoMetadata();
                System.out.println("Format: " + videoMetaData.format());
                System.out.println("Codec: " + videoMetaData.codec());
                System.out.println("Duration: " + videoMetaData.durationMillis());
                System.out.println("FrameRate: " + videoMetaData.frameRate());
                System.out.println("Job");

                // Show face information
                for (FaceDetection face : faceDetectionResponse.faces()) {
                    result.add(face);
                }

            } while (faceDetectionResponse != null && faceDetectionResponse.nextToken() != null);

        } catch (RekognitionException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return result;
    }

    public String startVoiceDetection(Path videoPath) {
        String resultUri = "";

        TranscribeClient trClient = TranscribeClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        try {
            Media media = Media.builder()
                    .mediaFileUri(String.format("s3://%s/%s", bucket, videoPath.getFileName().toString()))
                    .build();

            StartTranscriptionJobRequest transcriptionJobRequest = StartTranscriptionJobRequest.builder()
                    .transcriptionJobName(String.format("%s-%s-%s", bucket, videoPath.getFileName().toString(), new Date().getTime()))
//                    .subtitles(Subtitles.builder().formats(SubtitleFormat.SRT).build())
                    .languageCode(LanguageCode.EN_US)
                    .mediaFormat(MediaFormat.MP4)
                    .media(media)
                    .build();

            StartTranscriptionJobResponse response = trClient.startTranscriptionJob(transcriptionJobRequest);
            resultUri = getTransriptResults(trClient, response.transcriptionJob().transcriptionJobName());
            trClient.close();
        } catch (TranscribeException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return resultUri;
    }

    private String getTransriptResults(TranscribeClient trClient, String jobName) {
        String transcriptFileUri = "";
        try {
            String paginationToken = null;
            GetTranscriptionJobResponse transcriptionJobResponse = null;
            boolean finished = false;
            TranscriptionJobStatus status;
            int yy = 0;
            GetTranscriptionJobRequest transcriptionJobRequest = GetTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .build();

            // Wait until the job succeeds
            while (!finished) {
                transcriptionJobResponse = trClient.getTranscriptionJob(transcriptionJobRequest);
                status = transcriptionJobResponse.transcriptionJob().transcriptionJobStatus();

                if (status == TranscriptionJobStatus.COMPLETED || status == TranscriptionJobStatus.FAILED)
                    finished = true;
                else {
                    System.out.println(yy + " status is: " + status.toString());
                    Thread.sleep(1000);
                }
                yy++;
            }
            finished = false;

            transcriptFileUri = transcriptionJobResponse.transcriptionJob().transcript().transcriptFileUri();
        } catch (TranscribeException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return transcriptFileUri;
    }

}
