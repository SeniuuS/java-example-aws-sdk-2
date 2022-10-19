package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class AmazonTranscriptionItem {
    private String start_time;
    private String end_time;
    private List<AmazonTranscriptionAlternative> alternatives = new ArrayList<>();
    private String type;

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public List<AmazonTranscriptionAlternative> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<AmazonTranscriptionAlternative> alternatives) {
        this.alternatives = alternatives;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
