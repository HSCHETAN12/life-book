package org.jsp.life_book.Controller;

import org.jsp.life_book.dto.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class AppController {
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
		map.put("user", user);
		return "register.html";
		
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
	public String register(@Valid User user,BindingResult result)
	{
		if(!(user.getPassword()).equals(user.getConfirmpassword()))
		{
			result.rejectValue("confirmpassword","error.confirmpassword", "Password Not Matching");
		}
		if(result.hasErrors())
		{
			return "register.html";
		}
		else {
		return "redirect:https://www.youtube.com";
	}
}
}
