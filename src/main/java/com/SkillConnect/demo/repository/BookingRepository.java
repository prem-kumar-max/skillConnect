package com.SkillConnect.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SkillConnect.demo.entity.Bookings;

public interface BookingRepository extends JpaRepository<Bookings,Long> {


	List<Bookings> findByBookedBy(String userEmail);
	List<Bookings> findByProviderIdAndStatus(Long providerId, String string);
	List<Bookings> findByProviderId(Long providerId);
	List<Bookings> findByStatus(String string);
	



}
