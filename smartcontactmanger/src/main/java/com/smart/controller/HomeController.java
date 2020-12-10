package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.config.MyConfig;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// Home Handler
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manger");
		return "home";
	}

	// About Handler
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manger");
		return "about";
	}

	// SignUp Handler
	@RequestMapping("/signup")
	public String signUpForm(Model model) {
		model.addAttribute("title", "SignUp - Smart Contact Manger");
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/do_signup")
	public String doSignUp(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, Model model,
			HttpSession session) {

		try {

			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			if (bindingResult.hasErrors()) {
				System.out.println("ERROR" + bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("deafult.png");

			// Here we can BCryptPassword
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			userRepository.save(user);

			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully Register  !! ", "alert-success"));

			return "signup";

		} catch (Exception e) {
			e.printStackTrace();

			model.addAttribute("user", user);

			session.setAttribute("message", new Message("Something Went Wrong  !! " + e.getMessage(), "alert-danger"));

			return "signup";
		}

	}

	// Custom login handler
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manger");
		return "login";
	}

}
