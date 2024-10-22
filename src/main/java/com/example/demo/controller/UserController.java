package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.UserEntity;
import com.example.demo.security.TokenProvider;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/auth")
public class UserController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private TokenProvider tokenProvider;
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	
	//회원가입 -> 데이터베이스에 데이터를 추가
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO){
		//request body 에 포함된 UserDTO 객체에 수신하여 처리한다.
		try {
			//UserDTO를 기반으로 UserEntity 객체를 생성한다.
			UserEntity user = UserEntity.builder()
					.username(userDTO.getUsername())
					//사용자에게 입력받은 비밀번호 암호화
					.password(passwordEncoder.encode(userDTO.getPassword()))
					.build();
			
			//UserService를 이용해 새로 만든 UserEntity를 데이터베이스에 저장한다.
			UserEntity registeredUser = userService.create(user);
			
			
			//등록된 UserEntity 정보를 UserDTO로 변환하여 응답에 사용한다.
			UserDTO responseUserDTO = UserDTO.builder()
					.id(registeredUser.getId())
					.username(registeredUser.getUsername())
					.build();
			// 성공적으로 저장된 유저 정보를 포함한 HTTP 200 응답을 반환한다.
            return ResponseEntity.ok(responseUserDTO);
		} catch (Exception e) {
			//예외가 발생한 경우, 에러 메시지를 포함한 ResponseDTO 객체를 만들어 응답한다.
			ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();
			
			//HTTP 400상태 코드를 반환하고, 에러메시지를 ResponseBody에 포함시킨다.
			return ResponseEntity.badRequest().body(responseDTO);// HTTP 400 응답을 생성한다.
		}
	}
	
	//아이디와 비밀번호를 입력받아 로그인처리
	@PostMapping("/signin")
	public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO){
		// 요청 본문으로 전달된 UserDTO의 username과 password를 기반으로 유저를 조회한다.
		//getByCredentials : Service에 있는 id와 password를 전달받아 조회하는 메서드
		UserEntity user = userService.getByCredentials(
				userDTO.getUsername(),
				userDTO.getPassword(),
				passwordEncoder);
		
		 
		
		//사용자가 존재한다면
		if(user != null) {
			
			final String token = tokenProvider.create(user);
			//인증에 성공한 경유 유저 정보를 UserDTO로 변환하여 응답에 사용한다.
			final UserDTO responseUserDTO = UserDTO.builder()
					.id(user.getId())
					.username(user.getUsername())
					.token(token)
					.build();
			//성공적으로 인증된 유저 정보를 포함한 HTTP 200 응답을 반환한다.
			return ResponseEntity.ok().body(responseUserDTO);
		}else {
			//유저가 존재하지 않거나 인증 실패시 에러 메시지를 포함한 ResponseDTO를 반환한다.
			ResponseDTO responseDTO = ResponseDTO.builder()
					.error("로그인 실패")//에러 메세지 설정
					.build();//ResponseDTO 객체를 빌드한다.
			
			//HTTP 400 상태 코드를 반환하고, 에러 메시지를 응답 본문에 포함시킨다.
			return ResponseEntity.badRequest().body(responseDTO);
			
		}
				
	}
}
