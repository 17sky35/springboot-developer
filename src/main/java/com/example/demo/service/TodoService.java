package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.example.demo.model.TodoEntity;
import com.example.demo.persistence.TodoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;


//@Component 어노테이션이 붙은 클래스는 bean으로 만들어준다.
// < @Component의 자식 컴포넌트의 종류 >
//@Repository
//@Controller
//@RestController
//컴포넌트를 구분함으로써 클래스의 용도를 구체적으로 구분할 수 있다.


//@Service 컴포넌트는 @Component의 자식 컴포넌트기 때문에
//스프링이 @Service도 찾아서 Bean으로 만들어준다.

@Slf4j
@Service //서비스임을 명시하는 어노테이션을 달아줘야함
@RequiredArgsConstructor

public class TodoService {
	
	//TodoRepository 주입하기
	private final TodoRepository repository;
	
	//메서드형태로 비즈니스로직을 구현
	public String testService() {
		//TodoEntity생성
		TodoEntity entity = TodoEntity.builder().title("My first todo item").build();
		//TodoEntity 저장
		//save() : insert
		repository.save(entity);
		//TodoEntity 검색
		//findById(entity.getId()) 기본키를 이용해 db에 저장된 TodoEntity찾고 getId으로 가져옴
		TodoEntity savedEntity = repository.findById(entity.getId()).get();
		return savedEntity.getTitle();
	}

	//Controller에서 넘어온 데이터를 검증하고 Todo테이블에 저장
	//데이터를 추가하고, 추가가 잘됐는지 확인하는 로직
	public List<TodoEntity> create(TodoEntity entity){
		
		validate(entity);
		//넘어온 Entity에 문제가 없을 때 DB에 추가한다.
		repository.save(entity);
		
		//{}는 Slf4j에서 제공하는 플레이스 홀더로, 두번째 매개변수로 전달된
		//entity.getId() 값이 대입되어 출력된다.
		log.info("Entity Id : {} is saved", entity.getId());
		
		
		//넘어온 entity로부터 userId를 가지고 DB에서 조회된 내용을
		//List에 묶어서 반환을 한다.
		//SELECT * FROM TodoEntity WHERE userId = 'temporary-user'
		//실행해서 조회된 결과를 list에 묶어서 반환하겠다.
		return repository.findByUserId(entity.getUserId());
	
	}
	
	//userId를 넘겨받아 해당 유저가 추가한 Todo를 모두 조회하는 메서드
	public List<TodoEntity> retrieve(String userId){
		return repository.findByUserId(userId);
	} 
	
	
	//업데이트 메서드를 만들것
	//entity를 수정하고 수정한 entity를 확인할 수 있는
	//TodoEntity entity 매개변수는 수정된 값이 들어있다.
	public List<TodoEntity> update(TodoEntity entity){
		//1. 수정할 entity가 유효한지 확인
		validate(entity);
		
		//존재하지 않는 entity는 업데이트 할 수 없음
		//기존에 저장되어있는 데이터를 조회
		Optional<TodoEntity> original = repository.findById(entity.getId());
		
		//자바 람다식으로 표현
		//1번째 방식
//		original.ifPresent(todo -> {
//			//반환된 TodoEntity가 존재하면(isPresent) 그 값을 새 Entity로 덮어쓴다.
//			todo.setTitle(entity.getTitle());
//			todo.setDone(entity.isDone());
//			
//			//데이터베이스에 새 값을 저장한다.
//			repository.save(todo);
//		});
		//2번째 방식
		if(original.isPresent()) {
			
			TodoEntity todo = original.get();
			todo.setTitle(entity.getTitle());
			todo.setDone(entity.isDone());
			
			repository.save(todo);
		}
		
		
		//수정이 잘 됐는지 확인하기 위해 retrieve 메서드를 실행한 결과를 반환
		return retrieve(entity.getUserId());
	}
	
	public List<TodoEntity> delete(TodoEntity entity){
		//1. 엔티티가 유효한지 확인
		validate(entity);
		
		try {
			//2. 엔티티를 삭제
			repository.delete(entity);
			
		} catch (Exception e) {
			//3. 예외 발생시 id와 exception을 로그로 찍는다.
			log.error("error deleting entity ", entity.getId(),e);
			//컨트롤러로 exception을 날린다.
			//데이터베이스 내부 로직을 캡슐화 하기 위해 e를 반환하지 않고
			//새 exception 객체를 반환한다.
			throw new RuntimeException("error deleting entity" + entity.getId());
			
		}
		//새 Todo 리스트를 가져와 반환한다.
		return retrieve(entity.getUserId());
	}
	
	
	
	private void validate(TodoEntity entity) {
		//검증
		//매개변수로 넘오온 Entity가 유효한지 검사
		if(entity == null) {
			log.warn("Entity cannot be null");
			throw new RuntimeException("Entity cannot be null");
		}
		
		//userId가 안넘어왔을때
		if(entity.getUserId() == null) {
			log.warn("Unknown user");
			throw new RuntimeException("Unknown user");
		}
	}
	
	
	
	
}
