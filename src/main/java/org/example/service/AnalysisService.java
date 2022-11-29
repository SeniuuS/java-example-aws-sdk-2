package org.example.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalysisService {

    private String region;
    private String bucket;

    private String startJobId = "";

    public AnalysisService(String region, String bucket) {
        this.region = region;
        this.bucket = bucket;
    }

    public List<FaceDetection> startFaceDetection(Path videoPath) {
        List<FaceDetection> result = null;

        AmazonRekognition rekClient = AmazonRekognitionClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .withRegion(region)
                .build();

        try {
            S3Object s3Obj = new S3Object();
            s3Obj.setBucket(this.bucket);
            s3Obj.setName(videoPath.getFileName().toString());

            Video vidOb = new Video();
            vidOb.setS3Object(s3Obj);

            StartFaceDetectionRequest faceDetectionRequest = new StartFaceDetectionRequest();
            faceDetectionRequest.setFaceAttributes(String.valueOf(FaceAttributes.ALL));
            faceDetectionRequest.setJobTag("Faces");
            faceDetectionRequest.setVideo(vidOb);

            StartFaceDetectionResult startLabelDetectionResult = rekClient.startFaceDetection(faceDetectionRequest);
            startJobId = startLabelDetectionResult.getJobId();

            result = getFaceResults(rekClient);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return result;
    }

    private List<FaceDetection> getFaceResults(AmazonRekognition rekClient) {
        List<FaceDetection> result = new ArrayList<>();

        try {
            String paginationToken = null;
            GetFaceDetectionResult faceDetectionResponse = null;
            boolean finished = false;
            String status;
            int yy = 0;

            do {
                if (faceDetectionResponse != null)
                    paginationToken = faceDetectionResponse.getNextToken();

                GetFaceDetectionRequest recognitionRequest = new GetFaceDetectionRequest();
                recognitionRequest.setJobId(startJobId);
                recognitionRequest.setNextToken(paginationToken);
                recognitionRequest.setMaxResults(10);

                // Wait until the job succeeds
                while (!finished) {
                    faceDetectionResponse = rekClient.getFaceDetection(recognitionRequest);
                    status = faceDetectionResponse.getJobStatus();

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
                VideoMetadata videoMetaData = faceDetectionResponse.getVideoMetadata();
                System.out.println("Format: " + videoMetaData.getFormat());
                System.out.println("Codec: " + videoMetaData.getCodec());
                System.out.println("Duration: " + videoMetaData.getDurationMillis());
                System.out.println("FrameRate: " + videoMetaData.getFrameRate());
                System.out.println("Job");

                // Show face information
                for (FaceDetection face : faceDetectionResponse.getFaces()) {
                    result.add(face);
                }

            } while (faceDetectionResponse != null && faceDetectionResponse.getNextToken() != null);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return result;
    }

    public String startVoiceDetection(Path videoPath) {
        String resultUri = "";

        AmazonTranscribe trClient = AmazonTranscribeClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .withRegion(region)
                .build();

        try {
            Media media = new Media();
            media.setMediaFileUri(String.format("s3://%s/%s", bucket, videoPath.getFileName().toString()));

            StartTranscriptionJobRequest transcriptionJobRequest = new StartTranscriptionJobRequest();
            transcriptionJobRequest.setTranscriptionJobName(String.format("%s-%s-%s", bucket, videoPath.getFileName().toString(), new Date().getTime()));
//          transcriptionJobRequest.setSubtitles(Subtitles.builder().formats(SubtitleFormat.SRT).build())
            transcriptionJobRequest.setLanguageCode(LanguageCode.EnUS.toString());
            transcriptionJobRequest.setMediaFormat(MediaFormat.Mp4.toString());
            transcriptionJobRequest.setMedia(media);

            StartTranscriptionJobResult response = trClient.startTranscriptionJob(transcriptionJobRequest);
            resultUri = getTransriptResults(trClient, response.getTranscriptionJob().getTranscriptionJobName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return resultUri;
    }

    private String getTransriptResults(AmazonTranscribe trClient, String jobName) {
        String transcriptFileUri = "";
        try {
            String paginationToken = null;
            GetTranscriptionJobResult transcriptionJobResponse = null;
            boolean finished = false;
            String status;
            int yy = 0;
            GetTranscriptionJobRequest transcriptionJobRequest = new GetTranscriptionJobRequest();
            transcriptionJobRequest.setTranscriptionJobName(jobName);

            // Wait until the job succeeds
            while (!finished) {
                transcriptionJobResponse = trClient.getTranscriptionJob(transcriptionJobRequest);
                status = transcriptionJobResponse.getTranscriptionJob().getTranscriptionJobStatus();

                if (status.equals(TranscriptionJobStatus.COMPLETED.toString()) || status.equals(TranscriptionJobStatus.FAILED.toString()))
                    finished = true;
                else {
                    System.out.println(yy + " status is: " + status.toString());
                    Thread.sleep(1000);
                }
                yy++;
            }
            finished = false;

            transcriptFileUri = transcriptionJobResponse.getTranscriptionJob().getTranscript().getTranscriptFileUri();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return transcriptFileUri;
    }

}
