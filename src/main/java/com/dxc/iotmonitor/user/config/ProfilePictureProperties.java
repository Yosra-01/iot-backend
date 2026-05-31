package com.dxc.iotmonitor.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "iot.profile-pictures")
public class ProfilePictureProperties {

    /**
     * Root directory for profile images on the server filesystem.
     * Files are named {@code {userId}.{ext}}; nothing is stored in the database.
     */
    private String directory = "uploads/profile-pictures";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Path resolvedRoot() {
        return Paths.get(directory).toAbsolutePath().normalize();
    }
}
