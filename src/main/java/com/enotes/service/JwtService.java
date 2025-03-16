package com.enotes.service;

import com.enotes.entity.User;

public interface JwtService {
	
	public String generateToken(User user);

}
