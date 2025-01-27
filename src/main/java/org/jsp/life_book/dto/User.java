package org.jsp.life_book.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Size(min = 3,max = 10,message = "Enter minimum 3 charecters")
	private String firstname;
	@Size(min = 1,max = 15,message = "It should be between 1 and 15 charecters")
	private String lastname;
	@Size(min = 5,max = 15,message = "It should be between 5 and 15 charecters")
	private String username;
	@Email(message = "It should me proper Email format")
	@NotEmpty(message = "Email is requried")
	private String email;
	@DecimalMin(value = "6000000000",message = "It should be proper numbere format")
	@DecimalMax(value = "9999999999",message = "It should be proper numbere format")
	private Long mobile;
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",message = "It should contain atleast 8 charecter, one uppercase, one lowercase, one number and one speacial charecter")
	private String password;
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",message = "It should contain atleast 8 charecter, one uppercase, one lowercase, one number and one speacial charecter")
	@Transient
	private String confirmpassword;
	@NotNull(message = "It is required Field")
	private String gender;
	private int otp;
	private boolean verified;
	private String bio;
	private String imageUrl;
	
	private boolean prime;
	
	@ManyToMany(fetch = FetchType.EAGER)
	 List<User> followers=new ArrayList<User>();
	@ManyToMany(fetch = FetchType.EAGER)
	 List<User> following=new ArrayList<User>();
	
	public boolean isFollowing() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpSession session = attributes.getRequest().getSession();
		User user=(User) session.getAttribute("user");
		if(user!=null) {
			for(User user2:user.following) {
				if(this.id==user2.id) {
					return true;
				}
			}
			return false;
		}else {
			return false;
		}
	}
}
