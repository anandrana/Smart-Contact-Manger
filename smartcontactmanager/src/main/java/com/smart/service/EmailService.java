package com.smart.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Service;

@Service
public class EmailService {


	public boolean sendEmail(String subject,String message,String to) {
		
		boolean f=false;
		String from="naammaikiyahai@gmail.com";
    	String host="smtp.gmail.com";
		
		//get system properties
		Properties properties=System.getProperties();
		
		//setting important information to properties 
		//set host
		properties.put("mail.smtp.host",host);
		properties.put("mail.smtp.port","465");
		properties.put("mail.smtp.ssl.enable","true");
		properties.put("mail.smtp.auth","true");
		
		//step 1.Get session 
		
		Session session=Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("naammaikiyahai@gmail.com","Rana1162@anandguts");
			}
			
		});
		
		session.setDebug(true);
		
		//step 2: compose the message [text,media]
		MimeMessage mimeMessage=new MimeMessage(session);
		try {
			
			// from email
			mimeMessage.setFrom(from);
			
			//adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message 
			mimeMessage.setSubject(subject);
			
			
			//adding adding attachment
			
			String path="/home/anand/Pictures/marks.jpg";
			MimeMultipart mimeMultipart=new MimeMultipart();
			
			MimeBodyPart textMime=new MimeBodyPart();
			MimeBodyPart attachMime=new MimeBodyPart();
			
			
			try {
				textMime.setText(message);
				File file=new File(path);
				attachMime.attachFile(file);
				
				mimeMultipart.addBodyPart(textMime);
				mimeMultipart.addBodyPart(attachMime);
				
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			mimeMessage.setContent(mimeMultipart);
			
			//step 3: send the message 
			Transport.send(mimeMessage);
			
			System.out.println("sent successfully ........");
			f=true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return f;
	}
	
}
