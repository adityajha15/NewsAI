package com.aditya.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.aditya.model.UserAccount;
import com.aditya.repo.UserRepo;
import com.aditya.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
		
	@Autowired
	CustomOAuth2UserService  customOAuth2UserService;
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/", "/index", "/Register","/forgotPassword").permitAll()
				.requestMatchers("/user-home").hasRole("USER")
				.anyRequest().authenticated())
				.formLogin((form) -> form.loginPage("/login")
				.defaultSuccessUrl("/loginsucess")
				.failureUrl("/login?error=true").permitAll())

				// Google login
				.oauth2Login(oauth -> oauth
						.loginPage("/oauth2/authorization/google") // Separate login page for Google OAuth2
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.defaultSuccessUrl("/oauth2success", true))

				.logout((logout) -> logout.logoutSuccessUrl("/index?logout=true").permitAll())

				.exceptionHandling(handling -> handling.accessDeniedPage("/403"));

		return http.build();	
	}
	
	@Autowired
	private UserRepo userRepo;
	
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		UserDetailsService customUserDetailsService = new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
				System.out.println(email);
				UserAccount userAccount = userRepo.findById(email).orElse(null);
				if (userAccount == null) {
					throw new UsernameNotFoundException("User not found");
				}
				return new User(userAccount.getEmail(), userAccount.getPassword(), userAccount.isEnable(), true, // accountNonExpired
						true, // credentialsNonExpired
						true, // accountNonLocked
						List.of(new SimpleGrantedAuthority(userAccount.getRole())));
			}

		};
		provider.setUserDetailsService(customUserDetailsService);
		provider.setPasswordEncoder(getEncoder());
		return provider;	}
	
	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
	    return config.getAuthenticationManager();
	}
}
