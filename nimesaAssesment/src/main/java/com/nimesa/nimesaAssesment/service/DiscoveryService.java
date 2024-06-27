package com.nimesa.nimesaAssesment.service;

import com.nimesa.nimesaAssesment.entity.DiscoveryResult;
import com.nimesa.nimesaAssesment.entity.Job;
import com.nimesa.nimesaAssesment.entity.S3ObjectDetails;
import com.nimesa.nimesaAssesment.helper.Constants;
import com.nimesa.nimesaAssesment.repository.DiscoveryResultRepository;
import com.nimesa.nimesaAssesment.repository.JobRepository;
import com.nimesa.nimesaAssesment.repository.S3ObjectDetailsRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class DiscoveryService {


    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DiscoveryResultRepository discoveryResultRepository;

    @Autowired
    private S3ObjectDetailsRepository s3ObjectDetailsRepository;

     @Autowired
    private EntityManager entityManager; 

    private final Ec2Client ec2Client;
    private final S3Client s3Client;


    public DiscoveryService() {

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Constants.accessKeyId, Constants.secretKey);

        // Create EC2 client
        this.ec2Client = Ec2Client.builder()
                .region(Region.AP_SOUTH_1) // Mumbai region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        // Create S3 client
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1) // Mumbai region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public String discoverServices(List<String> services) {
        // Generate JobId
        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, "IN_PROGRESS");
        jobRepository.save(job);

        // Asynchronously discover services
        if (services.contains("EC2")) {
            CompletableFuture.runAsync(() -> discoverEC2Instances(jobId));
        }
        if (services.contains("S3")) {
            CompletableFuture.runAsync(() -> discoverS3Buckets(jobId));
        }

        return jobId;
    }


    public String getJobStatus(String jobId) {
        Optional<Job> job = jobRepository.findById(jobId);
        return job.map(Job::getStatus).orElse("Job not found");
    }

    private void discoverEC2Instances(String jobId) {
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            List<String> instanceIds = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(Instance::instanceId)
                    .collect(Collectors.toList());

            instanceIds.forEach(instanceId -> {
                DiscoveryResult result = new DiscoveryResult(jobId, "EC2", instanceId);
                discoveryResultRepository.save(result);
            });

            // Update the job status to SUCCESS
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("SUCCESS");
            jobRepository.save(job);
        } catch (Exception e) {
            // Handle the exception and update job status to FAILED
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("FAILED");
            jobRepository.save(job);
            e.printStackTrace();
        }
    }

    private void discoverS3Buckets(String jobId) {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<String> bucketNames = response.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

            bucketNames.forEach(bucketName -> {
                DiscoveryResult result = new DiscoveryResult(jobId, "S3", bucketName);
                discoveryResultRepository.save(result);
            });

            // Update the job status to SUCCESS
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("SUCCESS");
            jobRepository.save(job);
        } catch (Exception e) {
            // Handle the exception and update job status to FAILED
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("FAILED");
            jobRepository.save(job);
            e.printStackTrace();
        }
    }

    public List<String> getDiscoveryResult(String serviceName) {
        List<DiscoveryResult> results = discoveryResultRepository.findByServiceName(serviceName);
        return results.stream()
                .map(DiscoveryResult::getResult)
                .collect(Collectors.toList());
    }

    public String getS3BucketObjects(String bucketName) {
        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, "IN_PROGRESS");
        jobRepository.save(job);

        CompletableFuture.runAsync(() -> discoverS3BucketObjects(bucketName, jobId));

        return jobId;
    }



    @Transactional
    public String discoverS3BucketObjects(String bucketName, String jobId) {

        CompletableFuture.runAsync(() -> {
            try {
                Set<String> uniqueFileNames = new HashSet<>(); // Use Set to avoid duplicates

                ListObjectsV2Request request = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .build();

                ListObjectsV2Response response;
                do {
                    response = s3Client.listObjectsV2(request);

                    for (S3Object object : response.contents()) {
                        String fileName = object.key();
                        if (uniqueFileNames.add(fileName)) { // Check for duplicates before saving
                            // Create or reattach S3ObjectDetails entity
                            S3ObjectDetails newS3ObjectDetails = new S3ObjectDetails();
                            newS3ObjectDetails.setBucketName(bucketName);
                            newS3ObjectDetails.setObjectKey(object.key());
                            s3ObjectDetailsRepository.saveAndFlush(newS3ObjectDetails);
                        }
                    }


                    // Continue listing if there are more objects
                    request = ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .continuationToken(response.nextContinuationToken())
                            .build();
                } while (response.isTruncated());

                // Update job status to SUCCESS after discovery completes
                Job job = jobRepository.findById(jobId).orElseThrow();
                job.setStatus("SUCCESS");
                jobRepository.saveAndFlush(job);

            } catch (Exception e) {
                // Handle exceptions and update job status to FAILED
                Job job = jobRepository.findById(jobId).orElseThrow();
                job.setStatus("FAILED");
                jobRepository.save(job);
                e.printStackTrace();
            }
        });

        return jobId;
    }

    public int getS3BucketObjectCount(String bucketName) {
        return s3ObjectDetailsRepository.countByBucketName(bucketName);

    }

    public List<String> getS3BucketObjectLike(String bucketName, String pattern) {
        String queryString = "SELECT s.objectKey FROM S3ObjectDetails s WHERE s.bucketName = :bucketName AND s.objectKey LIKE :pattern";
        jakarta.persistence.Query query = entityManager.createQuery(queryString);
        query.setParameter("bucketName", bucketName);
        query.setParameter("pattern", "%" + pattern + "%");

        List<String> results = query.getResultList();
        return results.stream().map(String::valueOf).collect(Collectors.toList());
    }

}
