package com.SkillConnect.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDto
{
    @NotNull
    @Size(min = 2, max = 100)
    private String fullName;

    @Email
    @NotNull
    private String email;

    @NotNull
    @Size(min = 8, max = 16)
    private String password;

    @NotNull
    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @NotNull
    private String state;

    @NotNull
    private String city;

    @NotNull
    private String category;

    @DecimalMin(value = "0.0")
    private Double chargePerService;

    private MultipartFile profileImageUrl;
}
