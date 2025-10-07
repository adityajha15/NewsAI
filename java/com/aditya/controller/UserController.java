package com.aditya.controller;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.aditya.mail.MailSend;
import com.aditya.model.UserAccount;
import com.aditya.service.UserService;
import com.aditya.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class UserController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	UserService userService;
	
	@Autowired
	MailSend mailSend;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@GetMapping(value = {"/","index"})
	public String home() {
		return "index";
	}
	
	@PostMapping("/login")
	public String login(Model model) {
		model.addAttribute("showLoginModal",true);
		return "index";
	}
	
	@PostMapping("/Register")
	public String register(@ModelAttribute UserAccount userAccount,HttpSession session,Model model) {
		String password=userAccount.getPassword();
		if(userService.save(userAccount)) {
			//mail send code
			mailSend(userAccount);
			
			//add code for authorization and role
			// Step 1: Create Authentication object
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(
	                        userAccount.getEmail(),password
	                );
	        // Step 2: Authenticate using AuthenticationManager
	        Authentication authentication = authenticationManager.authenticate(authToken);
	        
	        // Step 3: Put authentication into SecurityContext
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        // Step 4: Store in session (so Spring Security knows user is logged in)
	        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
	                             SecurityContextHolder.getContext());
			
			session.setAttribute("name", userAccount.getName());
			session.setAttribute("email", userAccount.getEmail());
			session.setAttribute("phone", userAccount.getPhone());
			session.setAttribute("passwordFlag", true);
//			model.addAttribute("msg","Registered Successfully!"); //does not work for redirect url
			return "redirect:/user-home";
		}else {
			model.addAttribute("msg","Email ID Already Registered!");
			return "index";
		}
	}
	
	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestBody String email,ModelMap model) {
		UserAccount userAccount= userService.getUserByEmail(email);
		if(userAccount==null) {
			model.addAttribute("showForgotModal",true);
			model.addAttribute("error2",true);
			return "index";
		}else {
			String token = jwtUtil.generateToken(email);
	        String resetLink = "http://localhost:2222/reset-password?token=" + token;
			
	        // Send email
	        String sub="Password Reset Request";
	        String body="Click the link to reset your password: " + resetLink;
	        mailSend.doMailSend(email, sub, body);
			model.addAttribute("msg","Reset link sent. Check your mail box!");
			return "index";
		}
	}
	
	@GetMapping("/reset-password")
	public String resetPasswordForm(@RequestParam(defaultValue = "") String token, Model model) {
        String email = jwtUtil.validateToken(token);
        if (email == null) {
            model.addAttribute("msg", "Invalid or expired token.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
	}
	
	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam String token,@RequestParam String password,Model model) {
		 String email = jwtUtil.validateToken(token);
	        if (email == null) {
	            model.addAttribute("msg", "Invalid or expired token.");
	            return "reset-password";
	        }
	        userService.updatePassword(email, password);
	        model.addAttribute("msg", "Password updated successfully. Please login.");
	        model.addAttribute("showLoginModal",true);
	        return "index";
	}
	
	
	@GetMapping("/user-home")
	public String userHome(HttpSession session) {
		return "user-home";
	}
	
	@GetMapping("/403")
	public String accessDenied() {
		return "403";
	}
	
	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "index";
	}
	
	@PostMapping("/UpdatePhoto")
	public String updatePhoto(@RequestPart MultipartFile photo,HttpSession session, Model model)throws IOException {
		String email=(String)session.getAttribute("email");
		userService.updatePhoto(email,photo.getBytes());
		model.addAttribute("msg","Success!");
		return "user-home";
	}
	
	@PostMapping("/UpdateProfile")
	public String updateProfile(@RequestParam String name,@RequestParam String phone,HttpSession session,Model model) {
		String email=(String)session.getAttribute("email");
		userService.updateProfile(email,phone,name);
		session.setAttribute("name", name);
		session.setAttribute("phone", phone);
		model.addAttribute("msg","Success!");
		return "user-home";
	}
	
	@PostMapping("/UpdatePassword")
	public String updatePassword(@RequestParam(defaultValue = "") String oldpassword,@RequestParam String newpassword,HttpSession session, Model model) {
		String email=(String)session.getAttribute("email");
		if(oldpassword.equals("") || userService.checkOldPassword(email, oldpassword)) {
			userService.updatePassword(email, newpassword);
			session.setAttribute("passwordFlag", true);
			model.addAttribute("msg","Success!");
		}else {
			model.addAttribute("msg","Old Password is Wrong!");
		}
		return "user-home";
	}
	
	private void mailSend(UserAccount userAccount) {
		String sub="Registered Successfully!";
		
//		String body="Congrats! "+userAccount.getName()+" have Registered Successfully!";
//		mailSend.doMailSend(userAccount.getEmail(), sub, body);
		
//		String body="<h1 style='background-color:blue;color:white;padding:20px;'>Congrats!</h1> "
//				+ "<p style='background-color:yellow;padding:20px;'>"+userAccount.getName()+" have Registered Successfully!</p>";
		
		String body="<!doctype html>"
				+ "<html lang='en'>"
				+ "<head>"
				+ "<meta charset='utf-8'>"
				+ "<title>Welcome to newsAi</title>\r\n"
				+ "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\r\n"
				+ "<style>\r\n"
				+ "/* Simple, safe inline-style-friendly defaults */\r\n"
				+ "body { margin:0; padding:0; background-color:#f4f6f8; font-family: \"Helvetica Neue\", Arial, sans-serif; color:#111; }\r\n"
				+ ".container { width:100%; max-width:600px; margin:24px auto; background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 4px 18px rgba(0,0,0,0.05); }\r\n"
				+ ".header { padding:20px; text-align:center; background: #0f1724; color:#fff; }\r\n"
				+ ".logo { max-height:48px; display:inline-block; vertical-align:middle; }\r\n"
				+ ".preheader { display:none !important; visibility:hidden; opacity:0; color:transparent; height:0; width:0; }\r\n"
				+ ".content { padding:28px; }\r\n"
				+ "h1 { margin:0 0 12px 0; font-size:20px; color:#0f1724; }\r\n"
				+ "p { margin:0 0 16px 0; line-height:1.5; color:#333; }\r\n"
				+ ".btn { display:inline-block; padding:12px 20px; border-radius:6px; text-decoration:none; font-weight:600; }\r\n"
				+ ".btn-primary { background:#0066ff; color:#fff; }\r\n"
				+ ".muted { color:#7a8594; font-size:13px; }\r\n"
				+ ".footer { padding:18px; background:#fbfdff; text-align:center; font-size:13px; color:#7a8594; }\r\n"
				+ ".small { font-size:12px; color:#9aa3b2; }\r\n"
				+ ".social { margin-top:10px; }\r\n"
				+ "@media (max-width:420px){ .content{padding:20px} h1{font-size:18px} }\r\n"
				+ "</style>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "<!-- Preheader (shows in inbox preview) -->\r\n"
				+ "<span class=\"preheader\">Welcome to newsAi — confirm your email and start personalising your news feed.</span>\r\n"
				+ "\r\n"
				+ "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8; width:100%;\">\r\n"
				+ "<tr>\r\n"
				+ "<td align=\"center\">\r\n"
				+ "<table role=\"presentation\" class=\"container\" cellpadding=\"0\" cellspacing=\"0\">\r\n"
				+ "<!-- Header -->\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"header\">\r\n"
				+ "<!-- Replace src with your logo URL -->\r\n"
				+ "<img src=\"https://example.com/logo.png\" alt=\"newsAi logo\" class=\"logo\" />\r\n"
				+ "<div style=\"margin-top:8px; font-size:14px; opacity:0.9;\">Smarter news, tailored for you</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "\r\n"
				+ "<!-- Body -->\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"content\">\r\n"
				+ "<h1>Welcome, "+userAccount.getName()+" 👋</h1>\r\n"
				+ "<p>Thanks for registering at <strong>newsAi</strong> — we’re excited to have you. newsAi helps you discover the stories that matter, faster.</p>\r\n"
				+ "\r\n"
				+ "<p class=\"muted\">Before you get started, please confirm your email address so we know it's really you.</p>\r\n"
				+ "\r\n"
				+ "<!-- CTA -->\r\n"
				+ "<p style=\"text-align:center; margin:22px 0;\">\r\n"
				+ "<a href=\"{{verification_link}}\" class=\"btn btn-primary\" style=\"background:#0066ff; color:#fff; border-radius:6px; padding:12px 18px; text-decoration:none; display:inline-block;\">Confirm my email</a>\r\n"
				+ "</p>\r\n"
				+ "\r\n"
				+ "<p>If the button above doesn't work, copy and paste this link into your browser:</p>\r\n"
				+ "<p class=\"small\"><a href=\"{{verification_link}}\" style=\"color:#0066ff; text-decoration:underline;\">{{verification_link}}</a></p>\r\n"
				+ "\r\n"
				+ "<hr style=\"border:none; border-top:1px solid #eef2f6; margin:22px 0;\" />\r\n"
				+ "\r\n"
				+ "<p><strong>What you can do next</strong></p>\r\n"
				+ "<ul style=\"margin:8px 0 16px 18px; color:#333;\">\r\n"
				+ "<li>Set your interests to personalize your news feed</li>\r\n"
				+ "<li>Enable push notifications for breaking news</li>\r\n"
				+ "<li>Explore curated topic lists</li>\r\n"
				+ "</ul>\r\n"
				+ "\r\n"
				+ "<p class=\"muted\">If you didn’t create an account with this email, you can safely ignore this message.</p>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "\r\n"
				+ "<!-- Footer -->\r\n"
				+ "<tr>\r\n"
				+ "<td class=\"footer\">\r\n"
				+ "<div style=\"font-weight:600; color:#0f1724;\">Need help?</div>\r\n"
				+ "<div class=\"small\" style=\"margin-top:6px;\">Email us at <a href=\"mailto:support@newsai.example\" style=\"color:#0066ff; text-decoration:none;\">support@newsai.example</a></div>\r\n"
				+ "\r\n"
				+ "<div class=\"social\" style=\"margin-top:12px;\">\r\n"
				+ "<!-- Replace with your social links -->\r\n"
				+ "<a href=\"#\" style=\"margin:0 6px; text-decoration:none;\">Twitter</a> |\r\n"
				+ "<a href=\"#\" style=\"margin:0 6px; text-decoration:none;\">LinkedIn</a> |\r\n"
				+ "<a href=\"#\" style=\"margin:0 6px; text-decoration:none;\">Instagram</a>\r\n"
				+ "</div>\r\n"
				+ "\r\n"
				+ "<div style=\"margin-top:12px;\" class=\"small\">© {{current_year}} newsAi. All rights reserved.</div>\r\n"
				+ "<div class=\"small\" style=\"margin-top:6px;\">If you don't want to receive emails from us, <a href=\"{{unsubscribe_link}}\" style=\"color:#0066ff; text-decoration:underline;\">unsubscribe</a>.</div>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "\r\n"
				+ "</table>\r\n"
				+ "</td>\r\n"
				+ "</tr>\r\n"
				+ "</table>\r\n"
				+ "</body>\r\n"
				+ "</html>";
		
		mailSend.doMailSendHTML(userAccount.getEmail(), sub, body);
	}
	
	@GetMapping("/loginsucess")
	public String userHome(Authentication authentication,HttpSession session) {
		String email=authentication.getName();
		String name= userService.getName(email);
		String phone= userService.getPhone(email);
		session.setAttribute("name", name);
		session.setAttribute("email", email);
		session.setAttribute("phone", phone);
		session.setAttribute("passwordFlag", true);
		return "redirect:/user-home";
	}
	@GetMapping("/oauth2success")
	public String oauth2(@AuthenticationPrincipal OAuth2User principal,HttpSession session) {
		String email = principal.getAttribute("email");
		UserAccount user=userService.getUserByEmail(email);
		session.setAttribute("name", user.getName());
		session.setAttribute("email", email);
		session.setAttribute("phone", user.getPhone());
		session.setAttribute("passwordFlag", userService.checkPasswordExist(email));
		
		return "redirect:/user-home";
	}
	
	@GetMapping("/getPhoto")
	public void getPhoto(@RequestParam String email,HttpServletResponse response)  throws IOException{
		byte photo[]=userService.getPhoto(email);
		if(photo==null || photo.length==0)
		{
			InputStream is=this.getClass().getClassLoader().getResourceAsStream("static/person.jpg");
			photo=is.readAllBytes();
		}
		response.getOutputStream().write(photo);
	}
	
}
