package com.nimesa.nimesaAssesment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nimesa.nimesaAssesment.entity.S3ObjectDetails;

public interface S3ObjectDetailsRepository extends JpaRepository<S3ObjectDetails, Long> {
    List<S3ObjectDetails> findByBucketNameAndObjectKeyLike(String bucketName, String pattern);

    int countByBucketName(String bucketName);

    Optional<S3ObjectDetails> findByBucketNameAndObjectKey(String bucketName, String fileName);
    
}
