package org.example.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import ws.schild.jave.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class UploadService {
    private Region region;
    private String bucket;

    public UploadService(Region region, String bucket) {
        this.region = region;
        this.bucket = bucket;
    }

    public Path upload(Path videoPath) throws IOException {
        S3Client s3 = S3Client.builder().region(region).build();

        if(!videoPath.endsWith(".mp4")) {
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
        s3.close();
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

    private void createBucket(S3Client s3Client, Region region) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(this.bucket)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(region.id())
                                    .build())
                    .build());
            System.out.println("Creating bucket : " + this.bucket);
            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(this.bucket)
                    .build());
            System.out.println(this.bucket + " is ready.");
            System.out.printf("%n");
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    private boolean getFile(S3Client s3Client, Path path) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(this.bucket)
                            .key(path.getFileName().toString())
                            .build());
            return true;
        }catch(NoSuchKeyException e) {
            return false;
        }
    }

    private void uploadVideo(S3Client s3Client, Path path) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.bucket)
                        .key(path.getFileName().toString()).build(),
                path);
    }
}
