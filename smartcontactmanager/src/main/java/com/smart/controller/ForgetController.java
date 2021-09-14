package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.helper.Message;
import com.smart.service.EmailService;


@Controller
public class ForgetController {
	
	@Autowired 
	private EmailService emailService;
	
	Random random=new Random(100000);
	
	@RequestMapping("/forget")
	public String openEmailForm() {
		return "forget_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email,HttpSession session) {
		

		
		int otp=random.nextInt(999999);
		System.out.println("otp "+otp);
		
		String subject="OTP";
		String message=" OTP ="+otp+"";
		
		String to=email;
		boolean result=emailService.sendEmail(subject, message, to);
		
		if(result) {
			return "verify_otp";
		}
		else {
			session.setAttribute(message, new Message("your have enter wrong otp","alert-danger"));
			return "forget_email_form";
		}
	}
}
