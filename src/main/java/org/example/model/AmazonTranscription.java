package org.example.model;

public class AmazonTranscription {
    private String jobName;
    private String accountId;
    private AmazonTranscriptionResult results;
    private String status;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AmazonTranscriptionResult getResults() {
        return results;
    }

    public void setResults(AmazonTranscriptionResult results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
