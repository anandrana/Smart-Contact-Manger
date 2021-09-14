package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;




@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	//method for Adding common data for response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		
		String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		
		System.out.println("user"+user);
		model.addAttribute("user",user);
	}
	
	//handler for the dashboard
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User DashBoard");
		return "normal/user_dashboard";
	}
	
	//handler for adding new contact
	@GetMapping("/addContact")
	public String addContact(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/addContact";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
				@RequestParam("profileImage") MultipartFile multipartFile ,
				Principal principal,
				HttpSession session) {
		
		try {
			String userName=principal.getName();
			User user=userRepository.getUserByUserName(userName);
			
			if(multipartFile.isEmpty()) {
				//if multipartfile is empty
				contact.setImage("contact.png");
				System.out.println("image is empty");
			}
			else {
				
				contact.setImage(multipartFile.getOriginalFilename());
				
				File file=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(file.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is successfully upload");
				
			}
			contact.setUser(user);
			
			user.getContacts().add(contact);
			userRepository.save(user);
			
			System.out.println(contact);
			session.setAttribute("message", new Message("Added Successfully !! add more contact ","alert-success"));
		}catch(Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went Wrong!! ","alert-danger"));
		}
		return "normal/addContact";
	}
	
	//show contacts handler 
	//current page=(0) page
	//per page=(n=5)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal) {
		
		model.addAttribute("title","show contacts");
		
		String UserName=principal.getName();
		
		User user=userRepository.getUserByUserName(UserName);
		
		Pageable pageable=PageRequest.of(page, 3);
		
		Page<Contact> contacts=contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/showContact";
	}
	
	//showing particular contacts details
	
	@RequestMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
	

		try {
			Optional<Contact> optional=contactRepository.findById(cId);
			Contact contact=optional.get();	
			
			String UserName=principal.getName();
			
			User user=userRepository.getUserByUserName(UserName);
			
			System.out.println("helllo");
			System.out.println(contact);
			if(user.getId()==contact.getUser().getId()) {
				model.addAttribute("title","Contact Detial");
				model.addAttribute("contact",contact);
				
			}			
		}catch(Exception e) {
			e.printStackTrace();
		}


		return "normal/contact_detail";
	}
	
	//handling delete the contact 
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Principal principal , HttpSession session) {
		
		Optional<Contact> optional=contactRepository.findById(cId);
		
		Contact contact=optional.get();
		
		String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			
			user.getContacts().remove(contact);
			userRepository.save(user);
			
			session.setAttribute("message", new Message(" Successfully contact deleted of CID"+contact.getcId(),"alert-success"));

		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update the contact details
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model model) {
		model.addAttribute("title","update contact");
		
		Contact contact=contactRepository.findById(cId).get();
		
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}

	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile,
			Model model,HttpSession session,Principal principal) {
		
		try {
			Contact oldContactDetail=contactRepository.findById(contact.getcId()).get();
			
			if(!multipartFile.isEmpty()) {
				
				//delete the old image 
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				//update the new image 
				File file=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(file.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
				
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
				contact.setImage(multipartFile.getOriginalFilename());
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user=userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			
			session.setAttribute("message", new Message("successfully updated of CID"+contact.getcId(),"alert-success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("contact name"+contact.getName());
		System.out.println("contact id"+contact.getcId());
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	// handler for your profile
	
	@GetMapping("/profile")
	public String yourProfile(@ModelAttribute User user, Model model) {
		System.out.println(user);
		
		model.addAttribute("title","Profile Page");
		model.addAttribute("user",user);
		return "normal/profile";
	}
	
	//handler for settings
	@GetMapping("/setting")
	public String openSettings() {
		return "normal/setting";
	}
	
	//handler for change password
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,
			Principal principal,HttpSession session) {
		
		String userName=principal.getName();
		
		User currentUser=userRepository.getUserByUserName(userName);
		
		if(bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())) {
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepository.save(currentUser);
			session.setAttribute("message", new Message("password will successfully changed","alert-success"));
		}else {
			session.setAttribute("message", new Message("Old passward is not matched with given old password","alert-danger"));
			return "redirect:/user/setting";
		}
		return "redirect:/user/index";
	}
	
}
