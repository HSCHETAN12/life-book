package org.jsp.life_book.repository;

import org.jsp.life_book.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer>{
	
	boolean existsByEmail(String email);
	
	boolean existsByMobile(long mobile);
	
	boolean existsByUsername(String username);
	
	
	
}
