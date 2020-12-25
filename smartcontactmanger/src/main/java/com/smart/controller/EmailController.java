package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.EmailRequest;
import com.smart.helper.Message;
import com.smart.services.EmailService;

@Controller
public class EmailController {
	Random random = new Random(10000);

	@Autowired
	private EmailService emailService;

	@RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
	public String sendEmailProccess(@RequestParam("to") String to, HttpSession session) {

		// generating otp of 4 digit
		int otp = random.nextInt(99999);

		System.out.println("OTP : " + otp);

		// send email

		String subject = "Forget Password ";
		String message = "<div style='border:1px solid #e2e2e2; padding:20px; width:30%;' >" + "<h1 style=' text-align: center;'> OTP : " + otp + "</h1>"
				+ "</div>";

		boolean flag = this.emailService.sendEmail(to, subject, message);

		if (flag) {

			// now store otp in session
			session.setAttribute("myOtp", otp);
			
			// the which email is given by user save it on session
			session.setAttribute("email", to);

			return "redirect:/verify-opt";
		} else {

			session.setAttribute("message", new Message("Check your eamil id !!", "alert-danger"));
			return "forget";

		}

	}

}

/*
 * send reset password link
 * 
 * @RequestMapping(value = "/sendemail", method = RequestMethod.POST) public
 * String sendEmail(@ModelAttribute EmailRequest emailRequest, HttpSession
 * session) { String url = "http://localhost:8282/reset-password";
 * 
 * emailRequest.setMessage("Password Reset\n" +
 * "Seem like you forget your password" + "\n" + url + "\n" +
 * "if you did not forget your password you can safely ignore this email.");
 * emailRequest.setSubject("Forget Password ");
 * System.out.println(emailRequest);
 * 
 * boolean result = this.emailService.sendEmail(emailRequest.getTo(),
 * emailRequest.getSubject(), emailRequest.getMessage());
 * 
 * if (result) {
 * 
 * session.setAttribute("message", new
 * Message("Reset password link is send successfully. Pls check your email..",
 * "alert-success"));
 * 
 * return "redirect:/forget"; } else {
 * 
 * session.setAttribute("message", new
 * Message("Something went wrong. Pls try again..", "alert-danger"));
 * 
 * return "redirect:/forget"; } }
 * 
 * 
 * 
 */
