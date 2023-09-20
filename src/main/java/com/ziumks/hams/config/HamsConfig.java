package com.ziumks.hams.config;

import com.ziumks.hams.dto.HamsInfoDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource
(
	value = "file:./config/hams.properties",
	ignoreResourceNotFound = false,
	encoding = "UTF-8"
)
public class HamsConfig
{
	@Bean(name = "HamsInfo")
	@ConfigurationProperties(prefix = "com.ziumks.hams")
	HamsInfoDto hamsInfo() {
		return new HamsInfoDto();
	}
}
