package com.enotes.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
public class ProjectConfig {

    @Bean
    ModelMapper mapper() {
		return new ModelMapper();
	}
    
    @Bean
    AuditorAware<Integer> auditAware(){
    	return new AuditAwareConfig();
    }

}
