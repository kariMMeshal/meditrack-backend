package com.MediTrack.meditrack_backend;

import com.MediTrack.meditrack_backend.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class MeditrackBackendApplication {

	public static void main(String[] args) {
        
		SpringApplication.run(MeditrackBackendApplication.class, args);
	}

}
