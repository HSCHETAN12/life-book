package org.jsp.life_book.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jsp.life_book.dto.Post;
import org.jsp.life_book.dto.User;
import org.jsp.life_book.helper.AES;
import org.jsp.life_book.helper.CloudinaryHelper;
import org.jsp.life_book.helper.EmailSender;
import org.jsp.life_book.repository.PostRepository;
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
	
	@Autowired
	PostRepository postRepository;
	

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

	public  String loadHome(HttpSession session) {
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

	public  String logout(HttpSession session) {
		session.removeAttribute("user");
		session.setAttribute("pass", "Logout sucessfully");
		return "redirect:/login";
	}

	public  String profile(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<Post> posts=postRepository.findByUser(user);
			if(!posts.isEmpty())
			{
				map.put("posts", posts);
			}
			return "profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public  String editProfile(HttpSession session) {
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

	public  String addpost(HttpSession session) {
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
			return "addPost.html";
		}else{
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public  String updatepost(HttpSession session, Post post, MultipartFile image) {
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
			post.setCaption(post.getCaption());
			post.setImageUrl(cloudinaryHelper.savePost(image));
			post.setUser(user);
			postRepository.save(post);
			
			session.setAttribute("pass", "Posted Success");
			return "redirect:/profile";
		}else{
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String deletePost(int id, HttpSession session) {
		
		
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
		Post post=postRepository.findById(id).get();
		postRepository.deleteById(id);
		String publicId=cloudinaryHelper.getPublicIdFromUrl(post.getImageUrl());
		System.out.println(publicId);
		cloudinaryHelper.deletePost(publicId);
		
		session.setAttribute("pass", "Post deleted successfull");
		return "redirect:/profile";
			
		}else{
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
		
	}



	public String viewSuggestions(HttpSession session, ModelMap map) {
		
			User user = (User) session.getAttribute("user");
			if (user != null) {
				List<User> suggestions = repository.findByVerifiedTrue();
				List<User> usersToRemove = new ArrayList<User>();
				for (User suggestion : suggestions) {
					if (suggestion.getId() == user.getId()) {
						usersToRemove.add(suggestion);
					}
					for (User followingUser : user.getFollowing()) {
						if (followingUser.getId() == suggestion.getId()) {
							usersToRemove.add(suggestion);
						}
					}
				}
				if (suggestions.isEmpty()) {
					session.setAttribute("fail", "No Suggestions");
					return "redirect:/profile";
				} else {
					suggestions.removeAll(usersToRemove);
					map.put("suggestions", suggestions);
					return "suggestions.html";
				}
			} else {
				session.setAttribute("fail", "Invalid Session");
				return "redirect:/login";
			}
		}

	public String followUser(int id, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			User folllowedUser = repository.findById(id).get();
			user.getFollowing().add(folllowedUser);
			folllowedUser.getFollowers().add(user);
			repository.save(user);
			repository.save(folllowedUser);
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String updatePost(Post post, HttpSession session, MultipartFile image) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			if(!(image.isEmpty()))
			post.setImageUrl(cloudinaryHelper.saveImage(image));
			else
				post.setImageUrl(postRepository.findById(post.getId()).get().getImageUrl());
			post.setUser(user);
			postRepository.save(post);
			session.setAttribute("pass", "Updated Success");
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	
	public String editpost(int id, HttpSession session,ModelMap map) {
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
			Post post = postRepository.findById(id).get();
			map.put("post", post);
			return "edit-post.html";
		}
		else{
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	}