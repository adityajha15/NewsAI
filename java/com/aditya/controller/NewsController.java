package com.aditya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aditya.model.NewsCheck;
import com.aditya.service.NewsService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;







@Controller
public class NewsController {
	
	@Autowired
	NewsService newsService;
	
	@PostMapping("/analyze")
	public String analyze(@RequestParam String article,HttpSession session,ModelMap model) {
		String email=(String)session.getAttribute("email");
    	NewsCheck item = newsService.analyze(article, email);
        model.addAttribute("summary", item.getSummary());
        model.addAttribute("credibility", item.getCredibility());       

        return "news";
	}
	
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		  String email=(String)session.getAttribute("email");
	      model.addAttribute("history", newsService.findByUserOrderByCreatedAtDesc(email));
	      return "dashboard";
	}
	
	@GetMapping("/allnews")
	public String allnews(ModelMap m) {
		m.addAttribute("allnews",newsService.findAll());
		return "allnews";
	}
	
	@GetMapping("/getDetails")
	public String getDetails(@RequestParam long id,ModelMap m) {
		NewsCheck newsCheck=newsService.getNewsById(id);
		m.addAttribute("newsDetails",newsCheck);
		return "news-details";
	}
	
	@GetMapping("/DeleteNews")
	public String deleteNews(@RequestParam long id,HttpSession session,Model m) {
		newsService.deleteNews(id);
    	String email=(String)session.getAttribute("email");
        m.addAttribute("history", newsService.findByUserOrderByCreatedAtDesc(email));
		return "dashboard";
	}
	
	
	
}
