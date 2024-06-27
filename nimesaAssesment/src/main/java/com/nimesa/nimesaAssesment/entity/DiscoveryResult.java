package com.nimesa.nimesaAssesment.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class DiscoveryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobId;
    private String serviceName;
    private String result;

    // Additional fields for EC2 instances
    private String instanceId;

    // Relationship with S3ObjectDetails
    @OneToOne(mappedBy = "discoveryResult", cascade = CascadeType.ALL)
    private S3ObjectDetails s3ObjectDetails;

    // Default constructor
    public DiscoveryResult() {}

    // Constructor with fields
    public DiscoveryResult(String jobId, String serviceName, String result) {
        this.jobId = jobId;
        this.serviceName = serviceName;
        this.result = result;
    }

   
}
