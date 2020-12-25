package com.smart.services;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String to, String subject, String message) {

		boolean f = false;

		String from = "gourav.srishti@gmail.com";

		// variable for gmail host
		String host = "smtp.gmail.com";

		// get the system properties
		Properties properties = System.getProperties();

		System.out.println("SMTP is running......");

		// setting important information to properties object

		// host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step 1: to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			String userName = "gourav.srishti@gmail.com";
			String password = "*******";

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication(userName, password);
			}
		});

//		session.setDebug(true);

		// step 2: compose the message

		MimeMessage mimeMessage = new MimeMessage(session);

		try {

			// form
			mimeMessage.setFrom(from);

			// adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			mimeMessage.setSubject(subject);

			// adding text to message
//			mimeMessage.setText(message);

			// now my message goes on html fomate
			mimeMessage.setContent(message, "text/html");

			// send//
			// step 3: send message using transport class
			Transport.send(mimeMessage);

			System.out.println("Email Send Successfully...");

			f = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return f;
	}

}
