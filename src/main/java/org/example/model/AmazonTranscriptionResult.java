package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class AmazonTranscriptionResult {
    private List<AmazonTranscriptionTranscript> transcripts = new ArrayList<>();
    private List<AmazonTranscriptionItem> items = new ArrayList<>();

    public List<AmazonTranscriptionTranscript> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(List<AmazonTranscriptionTranscript> transcripts) {
        this.transcripts = transcripts;
    }

    public List<AmazonTranscriptionItem> getItems() {
        return items;
    }

    public void setItems(List<AmazonTranscriptionItem> items) {
        this.items = items;
    }
}
