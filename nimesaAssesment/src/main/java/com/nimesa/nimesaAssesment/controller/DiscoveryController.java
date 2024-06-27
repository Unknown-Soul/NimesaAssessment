package com.nimesa.nimesaAssesment.controller;

import com.nimesa.nimesaAssesment.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @PostMapping("/discoverServices")
    public ResponseEntity<String> discoverServices(@RequestBody List<String> services) {
        String jobId = discoveryService.discoverServices(services);
        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/getJobResult/{jobId}")
    public ResponseEntity<String> getJobResult(@PathVariable String jobId) {
        String status = discoveryService.getJobStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/getDiscoveryResult/{service}")
    public ResponseEntity<List<String>> getDiscoveryResult(@PathVariable String service) {
        List<String> results = discoveryService.getDiscoveryResult(service);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/getS3BucketObjects")
    public ResponseEntity<String> getS3BucketObjects(@RequestBody String bucketName) {
        String jobId = discoveryService.getS3BucketObjects(bucketName);
        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/getS3BucketObjectCount/{bucketName}")
    public ResponseEntity<Integer> getS3BucketObjectCount(@PathVariable String bucketName) {
        int count = discoveryService.getS3BucketObjectCount(bucketName);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/getS3BucketObjectLike")
    public ResponseEntity<List<String>> getS3BucketObjectLike(@RequestParam String bucketName, @RequestParam String pattern) {
        List<String> files = discoveryService.getS3BucketObjectLike(bucketName, pattern);
        return ResponseEntity.ok(files);
    }


}
