package org.jsp.life_book.service;

import java.util.Random;


import org.jsp.life_book.dto.User;
import org.jsp.life_book.helper.AES;
import org.jsp.life_book.helper.CloudinaryHelper;
import org.jsp.life_book.helper.EmailSender;
import org.jsp.life_book.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class UserService {
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	CloudinaryHelper cloudinaryHelper;
	

	public String loadregister(ModelMap map, User user) {
		map.put("user", user);
		return "/register.html";
	}

	public String register(@Valid User user, BindingResult result, HttpSession session) {
		
		
		if(!(user.getPassword()).equals(user.getConfirmpassword()))
		{
			result.rejectValue("confirmpassword","error.confirmpassword", "Password Not Matching");
		}
		if(result.hasErrors())
		{
			return "register.html";
		}

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
//			emailSender.sendotp(user.getEmail(),otp , user.getFirstname());
			repository.save(user);
			session.setAttribute("pass", "otp-sent Success");
			return "redirect:/otp/" + user.getId();
		}
		return null;
	}


	public String verifyotp(int otp, int id, HttpSession session) {
		User user=repository.findById(id).get();
		if(user.getOtp()==otp)
		{
			user.setVerified(true);
			user.setOtp(0);
			repository.save(user);
			session.setAttribute("pass", "Account Created Success");
			return "redirect:/";
		}
		else {
			session.setAttribute("fail", "Invaild otp,Try Again!!!");
			return "redirect:/otp/" + id;
		}
	}

	public String resend(int id) {
		User user=repository.findById(id).get();
		System.err.println(user.getOtp());
//		emailSender.sendotp(user.getEmail(),user.getOtp() , user.getFirstname());
		return "redirect:/otp/"+id;
	}

	public String resend(int id, HttpSession session) {
		User user=repository.findById(id).get();
		int otp=new Random().nextInt(10000,100000);
		user.setOtp(otp);
//		emailSender.sendotp(user.getEmail(),user.getOtp() , user.getFirstname());
		System.err.println(otp);
		repository.save(user);
		session.setAttribute("pass", "otp Re-sent Success");
		return "redirect:/otp/"+user.getId();
	}

	public String login(String username, String password, HttpSession session) {
		User user=repository.findByUsername(username);
		if(user==null)
		{
			session.setAttribute("fail", "Invaild Username");
			return "redirect:/login";
		}
		else {
			if(AES.decrypt(user.getPassword()).equals(password))
			{
				if(user.isVerified())
				{
					session.setAttribute("user", user);
					session.setAttribute("pass", "Login Success");
					return "home.html";
				}
				else {
					int otp=new Random().nextInt(100000,1000000);
					user.setOtp(otp);
					System.err.println(otp);
					// emailSender.sendOtp(user.getEmail(), otp, user.getFirstname());
					repository.save(user);
					session.setAttribute("pass", "otp sent to email,First Verify Email to Login");
					return "redirect:/otp/"+user.getId();
				}
			}else {
				session.setAttribute("fail", "Incorrect Password");
				return "redirect:/login";
			}
		}
	}
	
	

	public static String loadHome(HttpSession session) {
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
			return "home.html";
		}
		else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public static String logout(HttpSession session) {
		session.removeAttribute("user");
		session.setAttribute("pass", "Logout sucessfully");
		return "redirect:/login";
	}

	public static String profile(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			return "profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public static String editProfile(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			return "edit-profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}


	public String updateProfile(HttpSession session, MultipartFile image, String bio) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			user.setBio(bio);
			user.setImageUrl(cloudinaryHelper.saveImage(image));
			repository.save(user);
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	
	

}
