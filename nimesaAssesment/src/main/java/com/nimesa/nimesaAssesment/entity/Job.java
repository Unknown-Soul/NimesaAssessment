package com.nimesa.nimesaAssesment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class Job {

    @Id
    private String jobId;
    private String status;

    // Default constructor
    public Job() {}

    // Constructor with fields
    public Job(String jobId, String status) {
        this.jobId = jobId;
        this.status = status;
    }
}

