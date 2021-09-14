package com.smart.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("")
	public String home(Model model) {
		model.addAttribute("title","Home-smart contact manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About-smart contact manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register-smart contact manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user , BindingResult bindingResult,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model model , HttpSession session) {
		

		try {
			
			if(!agreement) {
				System.out.println("you are not accept the terms and condition");
				throw new Exception("you  are not accept the terms and condition");
			}
			
			if(bindingResult.hasErrors()) {
				System.out.println("enter into signup");
				model.addAttribute("user",user);
				return "signup";
			}			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
//			this is will save in database
			User result=userRepository.save(user);
			
			model.addAttribute("user",new User());
			
			session.setAttribute("message", new Message("Successfully Registered ","alert-success"));
			
			return "signup";
			//System.out.println("Agreement"+agreement);
			//System.out.println("User"+user);

		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong !! "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	@RequestMapping("/signin")
	public String customlogin(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
	}
}
