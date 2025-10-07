package com.aditya.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aditya.model.UserAccount;

@Repository
public interface UserRepo extends JpaRepository<UserAccount, String>{

}
