package com.SkillConnect.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;
    private String state;
    private String city;
    private String category;
    private Double chargePerService;
    private String profileImageUrl;
    private String password;

    @OneToMany
    private List<FeedBack> feedBacks;

    @OneToMany
    private List<Chat> chats;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Achievement> achievements;


}
