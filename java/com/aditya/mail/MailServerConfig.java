package com.aditya.mail;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class MailServerConfig {
	
	private String adminGmailEmail;
    private String adminGmailPasscode;
	
	@Bean
	public JavaMailSender getMailConfig() {
		JavaMailSenderImpl mailSender=new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		
		
		mailSender.setUsername(adminGmailEmail);
		mailSender.setPassword(adminGmailPasscode);
		Properties prop=mailSender.getJavaMailProperties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		//prop.put("mail.debug", "true");//optional
		return mailSender;
	}
}
