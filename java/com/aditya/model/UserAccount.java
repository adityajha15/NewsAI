package com.aditya.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {
	@Id
	private String email;
	private String password;
	private String name;
	private String phone;
    private String role="ROLE_USER";
    private boolean enable=true;
    @Column(columnDefinition = "longblob")
    private byte[] photo;
}
