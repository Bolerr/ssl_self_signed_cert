package org.example.ssl

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication
class SSLExampleApplication extends SpringBootServletInitializer {

	static void main(String[] args) {
		SpringApplication.run(SSLExampleApplication.class, args)
	}

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(SSLExampleApplication.class)
	}
}
