package com.wonwire.wonwire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    /**
     * JWT token returned after successful authentication.
     * The frontend must store this token and send it in every subsequent request
     * via the Authorization header: "Bearer {token}"
     */
    private String token;
    private String email;
    private String fullName;
}