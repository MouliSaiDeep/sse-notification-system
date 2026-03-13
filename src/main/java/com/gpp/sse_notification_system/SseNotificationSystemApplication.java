package com.gpp.sse_notification_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SseNotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SseNotificationSystemApplication.class, args);
	}

}
