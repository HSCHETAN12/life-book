package org.jsp.life_book.repository;

import org.jsp.life_book.dto.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {

}
