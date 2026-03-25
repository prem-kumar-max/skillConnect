package com.SkillConnect.demo.repository;

import com.SkillConnect.demo.entity.FeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedBack,Long>
{
    List<FeedBack> getFeedbacksByUserId(Long userId);

    @Query(value ="SELECT * FROM feed_back WHERE provider_id = :providerId" ,nativeQuery = true)
    List<FeedBack> getFeedbacksByProviderId(@Param("providerId") Long providerId);


}
