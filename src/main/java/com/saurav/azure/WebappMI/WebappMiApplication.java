package com.saurav.azure.WebappMI;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class WebappMiApplication {

	public static void main(String[] args) {
        String ACTIVE_PROFILE = System.getenv("ACTIVE_PROFILE");
        System.setProperty("spring.profiles.active",ACTIVE_PROFILE);
		SpringApplication.run(WebappMiApplication.class, args);
	}


}
