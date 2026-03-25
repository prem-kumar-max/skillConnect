package com.SkillConnect.demo.repository;

import com.SkillConnect.demo.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAchievementRepository extends JpaRepository<Achievement,Long>
{
    List<Achievement> findByProviderId(Long providerId);
}
