package org.jsp.life_book.Controller;

import org.eclipse.angus.mail.handlers.multipart_mixed;
import org.jsp.life_book.dto.User;
import org.jsp.life_book.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AppController {
	
	@Autowired
	UserService service;
		//the givnen method is just load or return to the login.html file when the path is locahost:{/,/login}
	//in the browser
	@GetMapping({"/","/login"})
	public String loadLogin()
	{
		return "login.html";
	}
	
	/**
	 * `ModelMap` is used to pass model attributes (e.g., `User` object) from the controller to the view.
	 * It allows us to dynamically add attributes to the model and make them available in the corresponding view (e.g., JSP or Thymeleaf).
	 * `ModelMap` simplifies the process of passing data between the controller and the view without explicitly creating a model object.
	 */
	
	@GetMapping("/register")
	public String loadregister(ModelMap map,User user)
	{
		return service.loadregister(map,user);
		
	}
	
	/**
	 * `BindingResult` holds the results of the validation triggered by the `@Valid` annotation.
	 * It contains all the validation errors that occurred during data binding, such as incorrect field values.
	 * Always place `BindingResult` immediately after the model object in the method signature to correctly bind the validation errors.
	 * If `BindingResult` has errors, it allows us to handle them (e.g., return the form view with error messages).
	 */
	
	/**
	 * The `@Valid` annotation triggers the validation of the model object (e.g., `User`).
	 * It ensures that the fields in the object, such as `@NotNull`, `@Size`, and `@Email`, 
	 * are validated based on the constraints defined in the model class.
	 * This validation occurs before any further processing in the controller method.
	 */
	
	
	@PostMapping("/register")
	public String register(@Valid User user,BindingResult result,HttpSession session)
	{
		return service.register(user,result,session);
	}
	
	@GetMapping("/otp/{id}")
	public String loadotp(@PathVariable int id,ModelMap map)
	{
		map.put("id", id);
		return "verify.html";
	}
	
	@PostMapping("/verify-otp")
	public String verify(@RequestParam int otp,@RequestParam int id,HttpSession session)
	{
		return service.verifyotp(otp, id,session);
	}
	
	@GetMapping("/resend-otp/{id}")
	public String resend(@PathVariable int id,HttpSession session)
	{
		return service.resend(id,session);
	}
	
	@PostMapping("/login")
	public String login(@RequestParam String username,@RequestParam String password,HttpSession session)
	{
		return service.login(username,password,session);
	}
	
	@GetMapping("/home")
	public String profile(HttpSession session)
	{
		return service.loadHome(session);
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session)
	{
		return service.logout(session);
	}
	
	@GetMapping("/profile")
	public String profile1(HttpSession session)
	{
		return service.profile(session);
	}
	
	@GetMapping("/edit-profile")
	public String editProfile(HttpSession session)
	{
		return service.editProfile(session);
	}
	
	@PostMapping("/update-profile")
	public String pdateProfiel(@RequestParam String bio,HttpSession session,@RequestParam MultipartFile image)
	{
		return service.updateProfile(session,image,bio);
	}
	
	
}
