package com.aditya.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aditya.model.NewsCheck;
import com.aditya.model.UserAccount;

@Repository
public interface NewsRepo extends JpaRepository<NewsCheck, Long>{
	List<NewsCheck> findByUserOrderByCreatedAtDesc(UserAccount user);
}
