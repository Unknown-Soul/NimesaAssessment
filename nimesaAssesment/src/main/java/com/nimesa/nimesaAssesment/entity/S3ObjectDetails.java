package com.nimesa.nimesaAssesment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class S3ObjectDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucketName;
    private String objectKey;

    @OneToOne
    @JoinColumn(name = "discovery_result_id")
    private DiscoveryResult discoveryResult;

}
