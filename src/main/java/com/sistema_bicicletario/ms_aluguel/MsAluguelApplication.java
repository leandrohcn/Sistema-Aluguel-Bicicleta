package com.sistema_bicicletario.ms_aluguel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
@EntityScan
@EnableFeignClients
public class MsAluguelApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAluguelApplication.class, args);
	}

}
