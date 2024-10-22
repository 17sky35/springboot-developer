package com.example.demo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>{
	
	//username 값으로 UserEntity를 찾아서 반환한다.
	UserEntity findByUsername(String username);
	//해당 username이 존재하면 true, 아니면 false를 반환한다.
	Boolean existsByUsername(String username);
	
	//username 과 password를 기준으로 UserEntity를 조회하는 메서드
	UserEntity findByUsernameAndPassword(String username, String password);
	//And가 사용되었으므로, 두 필드를 모두 만족하는 데이터를 찾는 조건으로
	//쿼리가 자동 생성된다.
	
	
}
