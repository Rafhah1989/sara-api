package com.sara.api.service;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class OciObjectStorageService {

    @Value("${oci.tenant-id}")
    private String tenantId;

    @Value("${oci.user-id}")
    private String userId;

    @Value("${oci.fingerprint}")
    private String fingerprint;

    @Value("${oci.region}")
    private String region;

    @Value("${oci.private-key-file-path}")
    private String privateKeyFilePath;

    @Value("${oci.bucket.name}")
    private String bucketName;

    @Value("${oci.namespace.name}")
    private String namespaceName;

    @Autowired
    private ResourceLoader resourceLoader;

    private ObjectStorage client;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes;
            try (InputStream keyStream = resourceLoader.getResource(privateKeyFilePath).getInputStream()) {
                keyBytes = keyStream.readAllBytes();
            }

            AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .fingerprint(fingerprint)
                    .region(Region.fromRegionId(region))
                    .privateKeySupplier(() -> new ByteArrayInputStream(keyBytes))
                    .build();

            client = ObjectStorageClient.builder()
                    .build(provider);
            
            log.info("OCI Object Storage client initialized in region: {}", region);
        } catch (Exception e) {
            log.error("Failed to initialize OCI Object Storage client. Check your properties and key file.", e);
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, long contentLength, String contentType) {
        uploadFile(bucketName, objectName, inputStream, contentLength, contentType);
    }

    public void uploadFile(String targetBucketName, String objectName, InputStream inputStream, long contentLength, String contentType) {
        if (client == null) throw new IllegalStateException("OCI Client not initialized");

        PutObjectRequest request = PutObjectRequest.builder()
                .bucketName(targetBucketName)
                .namespaceName(namespaceName)
                .objectName(objectName)
                .putObjectBody(inputStream)
                .contentLength(contentLength)
                .contentType(contentType)
                .build();

        client.putObject(request);
        log.info("File uploaded to OCI bucket {}: {}", targetBucketName, objectName);
    }

    public InputStream downloadFile(String objectName) {
        return downloadFile(bucketName, objectName);
    }

    public InputStream downloadFile(String targetBucketName, String objectName) {
        if (client == null) throw new IllegalStateException("OCI Client not initialized");

        GetObjectRequest request = GetObjectRequest.builder()
                .namespaceName(namespaceName)
                .bucketName(targetBucketName)
                .objectName(objectName)
                .build();

        GetObjectResponse response = client.getObject(request);
        return response.getInputStream();
    }

    public void deleteFile(String objectName) {
        deleteFile(bucketName, objectName);
    }

    public void deleteFile(String targetBucketName, String objectName) {
        if (client == null) throw new IllegalStateException("OCI Client not initialized");

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespaceName)
                .bucketName(targetBucketName)
                .objectName(objectName)
                .build();

        client.deleteObject(request);
        log.info("File deleted from OCI bucket {}: {}", targetBucketName, objectName);
    }

    public String getPublicUrl(String targetBucketName, String objectName) {
        return String.format("https://%s.objectstorage.%s.oci.customer-oci.com/n/%s/b/%s/o/%s",
                namespaceName, region, namespaceName, targetBucketName, objectName);
    }
}
