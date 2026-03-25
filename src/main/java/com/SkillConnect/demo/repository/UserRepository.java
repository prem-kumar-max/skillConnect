package com.SkillConnect.demo.repository;

import com.SkillConnect.demo.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import com.SkillConnect.demo.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
	
	User findByEmail(String email);

}
