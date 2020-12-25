package com.smart.controller;

import java.security.Principal;

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

	@Autowired
	private BCryptPasswordEncoder bcrypt;

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

	// forget password handler
	@RequestMapping("/forget")
	public String forgetPassword() {
		return "forget";
	}

	// reset password
	@RequestMapping("/verify-opt")
	public String verifyOtp() {
		return "verify";
	}

	@PostMapping("/verifyOpt-Process")
	public String verifyOptProcess(@RequestParam("otp") int opt, HttpSession session) {

		int myOtp = (int) session.getAttribute("myOtp");

		String email = (String) session.getAttribute("email");

//		System.out.println("myOtp : " + myOtp);
//		System.out.println("email : " + email);

		if (myOtp == opt) {

			User user = this.userRepository.getUserByUserName(email);

			if (user == null) {
				// send error message if the person is not there in my DB
				session.setAttribute("message", new Message("User does not exits with this emails..", "alert-danger"));

				return "forget";

			} else {

				return "redirect:/reset-password";

			}

		} else {
			session.setAttribute("message", new Message("Something went wrong. try again.", "alert-danger"));
			return "verify";
		}

	}

	@GetMapping("/reset-password")
	public String resetPasswordForm() {
		return "reset_password";
	}

	@PostMapping("/resetPassword-process")
	public String resetPassword(@RequestParam("newpassword") String newpassword,
			@RequestParam("confirm") String confirm, HttpSession session) {

		if (newpassword.equals(confirm)) {

			String email = (String) session.getAttribute("email");

			User user = this.userRepository.getUserByUserName(email);

			user.setPassword(this.bcrypt.encode(newpassword));

			this.userRepository.save(user);

			session.setAttribute("message", new Message("Password reset successfully..", "alert-success"));

			return "login";

		} else {
			session.setAttribute("message", new Message("Something went wrong. try again..", "alert-danger"));
			return "reset_password";
		}
	}

}

/*
 * send link in email
 * 
 * @PostMapping("/resetprocess") public String
 * resetProccess(@RequestParam("newpassword") String newpassword,
 * 
 * @RequestParam("confirm") String confirmP, HttpSession session) {
 * 
 * 
 * String email = (String) session.getAttribute("email");
 * 
 * System.out.println(email);
 * 
 * System.out.println("New password : " + newpassword);
 * System.out.println("Confirm password : " + confirmP);
 * 
 * if (newpassword.equals(confirmP)) {
 * 
 * // String email = (String) session.getAttribute("email"); // // User user =
 * this.userRepository.getUserByUserName(email); // // String password =
 * this.bcrypt.encode(newpassword); // // user.setPassword(password); // //
 * this.userRepository.save(user);
 * 
 * session.setAttribute("message", new
 * Message("New Password save successfully.", "alert-success"));
 * 
 * return "login";
 * 
 * } else {
 * 
 * session.setAttribute("message", new
 * Message("Your password cant match. Try again..", "alert-danger")); return
 * "reset";
 * 
 * }
 */
