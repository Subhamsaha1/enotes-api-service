package com.enotes.service.impl;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.enotes.config.security.CustomUserDetails;
import com.enotes.dto.EmailRequest;
import com.enotes.dto.LoginRequest;
import com.enotes.dto.LoginResponse;
import com.enotes.dto.UserDto;
import com.enotes.entity.AccountStatus;
import com.enotes.entity.Role;
import com.enotes.entity.User;
import com.enotes.repository.RoleRepository;
import com.enotes.repository.UserRepository;
import com.enotes.service.JwtService;
import com.enotes.service.UserService;
import com.enotes.util.Validation;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private Validation validation;
	
	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	private EmailSendService emailService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;
	
	@Override
	public Boolean register(UserDto userDto, String url) throws Exception {

		validation.userValidation(userDto);
		User user = mapper.map(userDto, User.class);
		
		setRole(userDto, user);
		
		AccountStatus status = AccountStatus.builder()
				.isActive(false)
				.verificationCode(UUID.randomUUID().toString())
				.build();
		
		user.setStatus(status);
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User saveUser = userRepo.save(user);
		if(!ObjectUtils.isEmpty(saveUser)) {
			// send email
			emailSend(saveUser, url);
			return true;
		}
		return false;
	}

	private void emailSend(User saveUser, String url) throws Exception {
		
		String message = "Hi, <b>[[username]]</b> "
				+ "<br> Your account registered successfully <br>"
				+ "<br> Click the below link and verify and activate your account<br>"
				+ "<a href = '[[url]]'> Click Here</a><br><br>"
				+"Thanks, <br>Enotes.com";

		message = message.replace("[[username]]", saveUser.getFirstName());
		message = message.replace("[[url]]", url+"/api/v1/home/verify?uid="+saveUser.getId()+"&&code="+saveUser.getStatus().getVerificationCode());
		
		EmailRequest emailRequest = EmailRequest.builder()
				.to(saveUser.getEmail())
				.title("Account Creating confirmation")
				.subject("Account Registration Status")
				.message(message)
				.build();
		emailService.sendEmail(emailRequest);
	}

	private void setRole(UserDto userDto, User user) {

		List<Integer> reqRoleId = userDto.getRoles().stream().map(r->r.getId()).toList();
		List<Role> roles = roleRepo.findAllById(reqRoleId);
		user.setRoles(roles);
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), 
				loginRequest.getPassword()));
		if(authenticate.isAuthenticated()) {
			CustomUserDetails customUserDetails = (CustomUserDetails)authenticate.getPrincipal();
			
			String token = jwtService.generateToken(customUserDetails.getUser());
			
			LoginResponse loginResponse = LoginResponse.builder()
			.user(mapper.map(customUserDetails.getUser(), UserDto.class))
			.token(token)
			.build();
			return loginResponse;
		}
		return null;
	}

}
