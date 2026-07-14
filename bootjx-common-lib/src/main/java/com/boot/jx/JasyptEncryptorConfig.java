package com.boot.jx;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Split out of {@link AppConfig} because {@code AppConfig} itself has
 * {@code @Value}-injected properties (e.g. {@code encrypted.app.property})
 * that jasypt-spring-boot decrypts using the {@code encryptorBean} defined
 * here. Defining that bean inside {@code AppConfig} would make {@code
 * AppConfig} depend on itself (needs to exist to produce the bean that
 * decrypts its own fields) - a circular reference that Spring silently
 * tolerated via early bean-reference exposure before Boot 2.6, but rejects
 * by default from Boot 2.6 onwards ({@code spring.main.allow-circular-references=false}).
 * Keeping this bean in its own configuration class with no encrypted
 * {@code @Value} fields of its own avoids the cycle entirely.
 */
@Configuration
public class JasyptEncryptorConfig {

	@Value("${jasypt.encryptor.password}")
	String jasyptEncryptorPassword;

	@Value("${jasypt.encryptor.algorithm}")
	String jasyptEncryptorAlgorithm;

	@Bean(name = "encryptorBean")
	public StringEncryptor stringEncryptor() {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(jasyptEncryptorPassword);
		config.setAlgorithm(jasyptEncryptorAlgorithm);
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setStringOutputType("base64");
		encryptor.setConfig(config);
		return encryptor;
	}
}
