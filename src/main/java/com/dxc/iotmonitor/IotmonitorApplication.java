package com.dxc.iotmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IotmonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotmonitorApplication.class, args);
	}

}
