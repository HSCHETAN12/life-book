package org.jsp.life_book.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailSender {

	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	TemplateEngine engine;
	
	public void sendotp(String to,int otp,String name)
	{
		MimeMessage message= mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		try {
				helper.setFrom("chetanyash5@gmail.com","lifebook Application");
				helper.setTo(to);
				helper.setSubject("Verfiy your mail through OTP");
				
				Context context=new Context();
				context.setVariable("name", name);
				context.setVariable("otp", otp);
				
				String body=engine.process("otp-template.html", context);
				helper.setText(body,true);
				
		}catch (Exception e) {
		
		}
		mailSender.send(message);
	}
}
