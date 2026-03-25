package com.SkillConnect.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SkillConnect.demo.entity.Provider;
import org.springframework.data.jpa.repository.Query;

public interface ProviderRepository extends JpaRepository<Provider,Long>{

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

//	@Query(value = "select p.id,p.full_name from chat c join provider p on c.provider_id = p.id where c.user_id = ?1",nativeQuery = true)
//	List<Provider> findMyChattingProviders(Long userId);

	Provider findByEmail(String email);

	List<Provider> getProvidersByCategory(String category);

	List<Provider> findByCityAndCategory(String city, String category);

}
