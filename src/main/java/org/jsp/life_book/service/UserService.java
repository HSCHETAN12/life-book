package org.jsp.life_book.service;

import java.util.Random;

import org.jsp.life_book.dto.User;
import org.jsp.life_book.helper.AES;
import org.jsp.life_book.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;



import jakarta.validation.Valid;

@Service
public class UserService {
	
	@Autowired
	UserRepository repository;
	

	public String loadregister(ModelMap map, User user) {
		map.put("user", user);
		return "/register.html";
	}

	public String register(@Valid User user, BindingResult result) {
		
		
		if(!(user.getPassword()).equals(user.getConfirmpassword()))
		{
			result.rejectValue("confirmpassword","error.confirmpassword", "Password Not Matching");
		}
		if(result.hasErrors())
		{
			return "register.html";
		}
//		if(!(user.isVerified()))
//		{
//			user.setOtp(0);
//			int otp=new Random().nextInt(10000,100000);
//			System.out.println(otp);
//			return "verify.html";
//	}
		if(repository.existsByEmail(user.getEmail()))
			result.rejectValue("email", "error.email", "Email is alreday exist");
		if(repository.existsByMobile(user.getMobile()))
			result.rejectValue("mobile", "error.mobile", "Number is alreday exist");
		if(repository.existsByUsername(user.getUsername()))
			result.rejectValue("username", "error.username", "username is alreday taken");
		
		else {
			user.setPassword(AES.encrypt(user.getPassword()));
			int otp=new Random().nextInt(10000,100000);
			user.setOtp(otp);
			System.err.println(otp);
			repository.save(user);
			return "redirect:/otp/" + user.getId();
		}
		return null;
	}


	public String verifyotp(int otp, int id) {
		User user=repository.findById(id).get();
		if(user.getOtp()==otp)
		{
			user.setVerified(true);
			user.setOtp(0);
			repository.save(user);
			return "redirect:/";
		}
		else {
			return "redirect:/otp/" + id;
		}
	}

}
