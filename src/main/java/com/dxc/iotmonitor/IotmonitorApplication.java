package com.dxc.iotmonitor;

import com.dxc.iotmonitor.user.config.R2StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(R2StorageProperties.class)
public class IotmonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotmonitorApplication.class, args);
	}

}
