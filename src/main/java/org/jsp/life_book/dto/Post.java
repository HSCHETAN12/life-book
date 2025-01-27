package org.jsp.life_book.dto;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "image_url")
	private String imageUrl;
	private String caption;
	@UpdateTimestamp
	private LocalDateTime PostedTime;
	@ManyToOne
	private User user;
	
	@ManyToMany(fetch = FetchType.EAGER)
	List<User> likedUsers = new ArrayList<User>();
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	List<Comment> comments = new ArrayList<Comment>();
	
	public boolean hasLiked(int id) {
		for (User likedUser : likedUsers) {
			if (likedUser.getId() == id) {
				return true;
			}
		}
		return false;
	}

}
