package org.jsp.life_book.service;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import org.json.JSONObject;

import org.jsp.life_book.dto.Comment;
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
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

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
					return "redirect:/home";
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

	public  String loadHome(HttpSession session,ModelMap map) {
		User user=(User)session.getAttribute("user");
		if(user!=null)
		{
			List<User> users = user.getFollowing();
			List<Post> posts=postRepository.findByUserIn(users);
			if(!posts.isEmpty())
			map.put("posts", posts);
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

	public String getFollowers(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<User> followers = user.getFollowers();
			if (followers.isEmpty()) {
				session.setAttribute("fail", "No Followers");
				return "redirect:/profile";
			} else {
				map.put("followers", followers);
				return "followers.html";
			}
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String getFollowing(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<User> following = user.getFollowing();
			if (following.isEmpty()) {
				session.setAttribute("fail", "Not Following Anyone");
				return "redirect:/profile";
			} else {
				map.put("following", following);
				return "following.html";
			}
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String unfollow(HttpSession session, int id) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			User user2 = null;
			for (User user3 : user.getFollowing()) {
				if (id == user3.getId()) {
					user2 = user3;
					break;
				}
			}
			user.getFollowing().remove(user2);
			repository.save(user);
			User user3 = null;
			for (User user4 : user2.getFollowers()) {
				if (user.getId() == user4.getId()) {
					user3 = user4;
					break;
				}
			}
			user2.getFollowers().remove(user3);
			repository.save(user2);
			session.setAttribute("user", repository.findById(user.getId()).get());
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String viewProfile(int id, HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			User checkedUser=repository.findById(id).get();
			List<Post> posts = postRepository.findByUser(checkedUser);
			if (!posts.isEmpty())
				map.put("posts", posts);
			map.put("user", checkedUser);
			return "view-profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String likePost(int id, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			Post post = postRepository.findById(id).get();
		
//			boolean flag=true;
//			
//			for (User likedUser : post.getLikedUsers()) {
//				if (likedUser.getId() == user.getId()) {
//					flag=false;
//					break;
//				}
			post.getLikedUsers().add(user);
			postRepository.save(post);
			return "redirect:/home";
			}
//			if(flag) {
//				post.getLikedUsers().add(user);
//			}
//			postRepository.save(post);
//			return "redirect:/home";
//		} 
		else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String dislikePost(int id, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			Post post = postRepository.findById(id).get();
			
			for (User likedUser : post.getLikedUsers()) {
				if (likedUser.getId() == user.getId()) {
					post.getLikedUsers().remove(likedUser);
					break;
				}
			}
			postRepository.save(post);
			return "redirect:/home";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	
	public String loadCommentPage(int id, HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			map.put("id", id);
			return "comment.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String comment(int id, HttpSession session, String comment) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			Post post = postRepository.findById(id).get();
			
			Comment userComment=new Comment();
			userComment.setComment(comment);
			userComment.setUser(user);
			
			post.getComments().add(userComment);
			postRepository.save(post);
			return "redirect:/home";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	
	public String prime(HttpSession session, ModelMap map) throws RazorpayException {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			RazorpayClient client = new RazorpayClient("rzp_test_6Lg2WKKGqBxoM2", "dVaKTcvZ8bMdDAPSuLGBkzUa");
			JSONObject object = new JSONObject();
			object.put("amount", 19900);
			object.put("currency", "INR");
			Order order = client.orders.create(object);
			map.put("key", "rzp_test_6Lg2WKKGqBxoM2");
			map.put("amount", order.get("amount"));
			map.put("currency", order.get("currency"));
			map.put("orderId", order.get("id"));
			map.put("user", user);
			return "payment.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String prime(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			user.setPrime(true);
			repository.save(user);
			
			session.setAttribute("user", user);
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	
	}