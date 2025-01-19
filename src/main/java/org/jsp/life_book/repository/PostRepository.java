package org.jsp.life_book.repository;

import java.util.List;

import org.jsp.life_book.dto.Post;
import org.jsp.life_book.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.transaction.Transactional;

public interface PostRepository extends JpaRepository<Post, Integer> {

	List<Post> findByUser(User user);
	
	 @Transactional
	void deleteByImageUrl(String imageUrl);

//	void deleteByImageUrl(String imageUrl);


}
