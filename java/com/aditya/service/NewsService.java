package com.aditya.service;

import java.time.Instant;
import java.util.List;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aditya.model.NewsCheck;
import com.aditya.model.UserAccount;
import com.aditya.repo.NewsRepo;
import com.aditya.repo.UserRepo;
import com.google.genai.Client;

@Service
public class NewsService {
//	@Autowired
//    private OpenAiChatModel chatModel;
	@Autowired
	NewsRepo newsRepo;
	@Autowired
	UserRepo userRepo;
	
	
	public NewsCheck analyze(String articleText, String email) {
UserAccount user=userRepo.findById(email).orElse(null);
		
        String summaryPrompt = "Summarize the following news into exactly 3 concise sentences:\n\n" + articleText;
        String credibilityPrompt = "Classify the credibility of the following news as one of: Credible, Suspicious, Fake. "
                + "Use indicators like source, specificity, sensationalism, verifiability. "
                + "Respond with just one word: Credible or Suspicious or Fake.\nNews:\n" + articleText;

//        String summary = chatModel.call(summaryPrompt);
//        String credibility = chatModel.call(credibilityPrompt); 
        
//        Client client = Client.builder().apiKey("YOUR-KEY").build();
        Client client = Client.builder().apiKey("AIzaSyC-9nGKekPBAmiAy5vY3Y8UZ_rp28zszJY").build();

        String summary = client.models.generateContent("gemini-2.5-flash",summaryPrompt,null).text();
        String credibility = client.models.generateContent("gemini-2.5-flash",credibilityPrompt,null).text();
        
        System.out.println(credibility);
        if (credibility != null) credibility = credibility.trim().split("\\s+")[0];

        NewsCheck n = NewsCheck.builder()
                .articleText(articleText)
                .summary(summary)
                .credibility(credibility)
                .createdAt(Instant.now())
                .user(user)
                .build();
        return newsRepo.save(n);
    }


	public List<NewsCheck> findByUserOrderByCreatedAtDesc(String email) {
		UserAccount user=userRepo.findById(email).orElse(null);
		return newsRepo.findByUserOrderByCreatedAtDesc(user);
	}


	public List<NewsCheck> findAll() {
		return newsRepo.findAll();
	}


	public NewsCheck getNewsById(long id) {
		return newsRepo.findById(id).orElse(null);
	}


	public void deleteNews(long id) {
		newsRepo.deleteById(id);
	}
}
