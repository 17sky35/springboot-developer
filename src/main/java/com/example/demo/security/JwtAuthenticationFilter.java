package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
//OncePerRequestFilter 상속받기
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	@Autowired
	private TokenProvider tokenProvider;
	
	//doFilterInternal을 오버라이딩해야 클래스에 에러가 사라짐
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		//필터에서 뭘 하고 싶은지 작성 (토큰을 가지고 유효한지 검증)
		try {
			//읽어오는것을 먼저 해야한다
			//1. 발급된 토큰 읽어오기
			//request에 담긴 토큰을 꺼내기
			String token = parseBearerToken(request);
			log.info("Filter is running...");
			
			//2. 토큰 검사하기
			//token.equalsIgnoreCase -> 문자열비교 null이라는 단어가 들어있는지 비교
			if(token != null && !token.equalsIgnoreCase("null")) {
				//토큰을 통해 userId를 반환받는다.
				//validateAndeGetUserId(token)
				//검증을하고 userId를 반환
				String userId = tokenProvider.validateAndeGetUserId(token);
				log.info("Authenticated user ID : " + userId);
				
				//사용자 인증 완료후, SecurityContext에 인증 정보를 등록
				//AbstractAuthenticationToken
				//스프링 시큐리티에서 제공하는 인증된 사용자 정보를 표현하는 추상클래스
				//인증된 사용자와 그 사용자의 권한 정보(Authorities)를 담는 역할을 한다.
				AbstractAuthenticationToken authentication = 
						new UsernamePasswordAuthenticationToken(
								userId,//id
								null,//password (굳이 저장안해도되서 null)
								AuthorityUtils.NO_AUTHORITIES //현재 권한 정보는 제공하지 않는다.
								);
				//WebAuthenticationDetailsSource
				//request로 부터 인증 세부 정보를 생성하는 역할을 한다.
				//.buildDetails(request)
				//reques 객체에서 인증과 관련된 추가적인 정보를 추출한다.
				//사용자의 세션 ID, 클라이언트의 IP주소 등의 메타데이터를 포함한다.
				//
				authentication.setDetails(
						new WebAuthenticationDetailsSource()
						.buildDetails(request)
				);
				
				//SecurityContext (createEmptyContext의 반환형)
				//인증된 정보를 저장
				//SecurityContextHolder
				//스프링 시큐리티에서 사용자의 인증 정보와 보안 컨텍스트를 관리하는 중심 클래스다.
				//애플리케이션 내에서 현재 인증된 사용자의 정보를 저장하고 제공하는 역할을 한다.
				SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); 
				//setAuthentication(authentication)
				//현재 요청에 대한 인증 정보를 securityContext에 저장하여
				//스프링 시큐리티가 해당 사용자를 인증된 사용자로 인식하게 해주는 메서드
				securityContext.setAuthentication(authentication);
				
				//setContext
				//인증을 완료한 후, 이 메서드를 사용하여 인증된 사용자 정보를 저장할 수 있다.
				SecurityContextHolder.setContext(securityContext);
			}
		} catch (Exception e) {
			logger.error("Could not set user authentication in security context", e);
		}
		//다음필터가 있으면 호출해라
		filterChain.doFilter(request, response);
	}//doFilterInternal
	//HttpsServletRequest request
	//클라이언트가 하는 요청은 request 객체에 담긴다.
	private String parseBearerToken(HttpServletRequest request) {
		//Http 요청의 헤더를 파싱해 Bearer 토큰을 반환한다.
		String bearerToken = request.getHeader("Authorization");//Authorization부분만 잘라낸다  
		
		//Bearer 토큰 형식일 경우 토큰값만 반환
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
  
}
