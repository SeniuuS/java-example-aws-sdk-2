package org.example.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import ws.schild.jave.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadService {
    private String region;
    private String bucket;

    public UploadService(String region, String bucket) {
        this.region = region;
        this.bucket = bucket;
    }

    public Path upload(Path videoPath) throws IOException {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .withRegion(region)
                .build();

        if(!videoPath.toString().endsWith(".mp4")) {
            System.out.println("Not mp4 video found...");
            System.out.println("Converting...");
            videoPath = convertToMP4(videoPath);
            if(videoPath != null) {
                System.out.println("Conversion finished : " + videoPath.toString());
            } else {
                System.out.println("Conversion failed. Exiting.");
                System.exit(1);
            }
        }

//        createBucket(s3, region);

        boolean created = getFile(s3, videoPath);
        if(!created) {
            System.out.println("Uploading object...");
            uploadVideo(s3, videoPath);
        } else {
            System.out.println("Object already exists...");
        }

        System.out.println("Upload complete");
        System.out.println("Closing the connection to {S3}");
        System.out.println("Connection closed");
        System.out.println("Exiting...");

        return videoPath;
    }

    private Path convertToMP4(Path videoPath) throws IOException {
        File source = new File(videoPath.toUri());
        Path targetPath = Files.createFile(Paths.get(videoPath.getFileName() + ".mp4"));
        File target = new File(targetPath.toUri());

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");

        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");
        video.setX264Profile(VideoAttributes.X264_PROFILE.BASELINE);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp4");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);

        MultimediaObject ob = new MultimediaObject(source);
        try {
            Encoder encoder = new Encoder();
            encoder.encode(ob, target, attrs);
            return Paths.get(target.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createBucket(AmazonS3 s3Client, String region) {
        try {
            System.out.println("Creating bucket : " + this.bucket);
            s3Client.createBucket(bucket);
            System.out.println(this.bucket + " is ready.");
            System.out.printf("%n");
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (AmazonClientException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private boolean getFile(AmazonS3 s3Client, Path path) {
        try {
            s3Client.getObjectMetadata(new GetObjectMetadataRequest(this.bucket, path.getFileName().toString()));
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    private void uploadVideo(AmazonS3 s3Client, Path path) {
        s3Client.putObject(new PutObjectRequest(this.bucket, path.getFileName().toString(), new File(path.toUri())));
    }
}
