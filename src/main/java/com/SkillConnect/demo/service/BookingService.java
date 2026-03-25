package com.SkillConnect.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkillConnect.demo.entity.Bookings;
import com.SkillConnect.demo.repository.BookingRepository;

@Service
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	public void updateBookingStatus(Long bookingId, String status) {
		Bookings booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid booking ID: " + bookingId));
		booking.setStatus(status); // Assuming there's a `setStatus` method in the Booking entity
		bookingRepository.save(booking); // Save the updated booking entity
	}

}
