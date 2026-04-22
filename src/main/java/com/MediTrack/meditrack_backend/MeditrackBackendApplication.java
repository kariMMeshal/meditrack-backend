package com.MediTrack.meditrack_backend;

import com.MediTrack.meditrack_backend.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MeditrackBackendApplication {

	public static void main(String[] args) {
        
		SpringApplication.run(MeditrackBackendApplication.class, args);
	}

}
