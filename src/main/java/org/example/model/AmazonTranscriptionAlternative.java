package org.example.model;

public class AmazonTranscriptionAlternative {
    private float confidence;
    private String content;

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "AmazonTranscriptionAlternative{" +
                "confidence=" + confidence +
                ", content='" + content + '\'' +
                '}';
    }
}
