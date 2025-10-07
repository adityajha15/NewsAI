package com.aditya.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCheck {
	@Id	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Lob
    @Column(nullable = false, length = 20000)
    private String articleText;
	
	 @Lob
	 @Column(length = 5000)
	 private String summary;
	 
	 @Column(length = 32)
	 private String credibility; // Credible / Suspicious / Fake

	 private Instant createdAt;

	 @ManyToOne(fetch = FetchType.LAZY)
	 private UserAccount user;
}
