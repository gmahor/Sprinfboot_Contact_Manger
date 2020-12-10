package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// logged in user
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		m.addAttribute("user", user);
	}

	// dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/addcontact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/addcontact";
	}

	// processing add contact from
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,
			@RequestParam("profileimage") MultipartFile file, BindingResult bindingResult, Principal principal,
			Model model, HttpSession session) {

		try {

			if (bindingResult.hasErrors()) {
				System.out.println("ERROR" + bindingResult.toString());
				model.addAttribute("contact", contact);
				return "normal/addcontact";
			}

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file
			if (file.isEmpty()) {
				System.out.println("file is empty !!");
				contact.setImage("contacts.png");
			} else {

				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image Uploaded Successfully");
			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);

//			System.out.println("Name : " + name);
//			System.out.println("User : " + user);
//			System.out.println("Contact : " + contact);

			System.out.println("Contact Added to database..");

			model.addAttribute("contact", new Contact());

			session.setAttribute("message", new Message("Contact Added Successfully !! ", "alert-success"));

			return "normal/addcontact";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something Went Wrong !!", "alert-danger"));
			return "normal/addcontact";
		}

	}

	// show contacts handler
	// per page = 5
	// current page = 0 [page]
	@GetMapping("/viewcontacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model m, Principal principal) {
		m.addAttribute("title", "View All Contacts");

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		int id = user.getId();

		// Pageable
		// 1) current page
		// 2) contact per page - 2

		Pageable pageable = PageRequest.of(page, 2);

		// contacts list specific user
		// List<Contact> contacts = this.contactRepository.findContactByUserId(id);
		Page<Contact> contacts = this.contactRepository.findContactByUserId(id, pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/view_contacts";
	}

	// showing specific contact details
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") int cid, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid, Principal principal, HttpSession session) {
		try {
			Optional<Contact> contactOptional = this.contactRepository.findById(cid);
			Contact contact = contactOptional.get();

			// if Your contact is not delete
			// contact.setUser(null);

			File file = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(file.getAbsolutePath() + File.separator + contact.getImage());

			Files.deleteIfExists(path);

			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);

			// check...
			if (user.getId() == contact.getUser().getId()) {
				this.contactRepository.delete(contact);
			}

			session.setAttribute("message", new Message("Contact Deleted Successfully !!!", "alert-warning"));

			return "redirect:/user/viewcontacts/0";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/viewcontacts/0";
		}
	}

	// open update contact handler
	@PostMapping("/update/{cid}")
	public String updateContact(@PathVariable("cid") int cid, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";

	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileimage") MultipartFile file,
			Principal principal,HttpSession session) {

		try {
			// old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();

			// image.....
			if (!file.isEmpty()) {

				// delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
			

				// Update new photo
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);

			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact Updated Successfully !!", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Contact Name : " + contact.getName());
		System.out.println("Contact Id : " + contact.getCid());
		return "redirect:/user/" + contact.getCid() + "/contact";
	}
	
	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Your profile" );
		
		return "normal/profile";
	}
	

}
