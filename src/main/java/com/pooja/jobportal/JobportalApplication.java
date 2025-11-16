package com.pooja.jobportal;

import com.pooja.jobportal.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobportalApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(JobportalApplication.class);
		app.addInitializers(new EnvConfig());
		app.run(args);
	}

}
