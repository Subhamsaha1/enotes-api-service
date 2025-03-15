package com.enotes.service;

import com.enotes.dto.LoginRequest;
import com.enotes.dto.LoginResponse;
import com.enotes.dto.UserDto;

public interface UserService {
	
	public Boolean register (UserDto userDto, String url) throws Exception;

	public LoginResponse login(LoginRequest loginRequest);

}
