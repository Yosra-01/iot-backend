package com.dxc.iotmonitor.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iot.r2")
public class R2StorageProperties {

    private String accountId;
    private String bucket;
    private String accessKeyId;
    private String secretAccessKey;
    private String publicBaseUrl;
    private String profilePicturePrefix = "profile-pictures";

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getProfilePicturePrefix() {
        return profilePicturePrefix;
    }

    public void setProfilePicturePrefix(String profilePicturePrefix) {
        this.profilePicturePrefix = profilePicturePrefix;
    }
}
