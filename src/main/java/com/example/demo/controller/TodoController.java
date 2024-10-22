package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.TodoDTO;
import com.example.demo.model.TodoEntity;
import com.example.demo.service.TodoService;

//@Controller 컨트롤러를 쓰면 view(html,jsp,thtmelef)를 반환
@RestController //이 클래스가 컨트롤러임을 명시하는 어노테이션
//@Controller + @ResponseBody -> 직렬화 해서 반환
@RequestMapping("todo")
public class TodoController {
	
	//TodoService 의존성 주입하기
	private final TodoService service;
	
	public TodoController(TodoService service) {
		this.service = service;
	}
	//다른 클래스에 있는 메서드를 사용하기위해 객체를 우선 만들어야 한다.
	//스프링에서는 객체를 이미 만들어놨다. 주입만 하면 된다.
	
	//Get /todo/test로 요청을 했을때 testTodo()메서드 호출되도록 정의하기
	//testTodo 안에서는  TodoService의 메서드를 호출하여 결과를 ResponseDTO의 리스트에 추가한다.
	//ResponseEntity.ok().body(response todoService의 문자열)
	
	//아래의 메서드가 실행되기 위한조건
	//브라우저에 주소가 호출되어야 한다.
	//localhost:9090/todo/test
	
	//ResPonseEntity
	//HTTP응답을 보다 세밀하게 제어할 수 있다.
	//HTTP 상태코드, 헤더, 바디를 구성할 수 있다.
	@GetMapping("/test")
	public ResponseEntity<?> testTodo(){
		String str = service.testService();
		//리스트의 생성
		//인덱스를 가지고 크기에 제한이 없는 자료구조
		List<String> list = new ArrayList<>();
		//리스트에 값 추가하기 list.add(value);
		list.add(str);
		//http 정보 응답을 위해 ResponseDTO 객체 생성 빌드패턴(builder(), build())으로 만듬
		ResponseDTO<String> response = ResponseDTO.<String>builder().data(list).build();
		//ResonpseEntity에 body에 response를 실어서 응답을 보낸다.
		return ResponseEntity.ok().body(response);
	}


	@PostMapping //포스트로 요청시 실행
	public ResponseEntity<?> createTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
		try {
			
			//TodoDTO 객체를 TodoEntity로 변환한다.
			TodoEntity entity = TodoDTO.toEntity(dto);
			
			//id에 null이 이미 들어가있지만 명시적으로 null로 한번 더 설정한다.
			//우린 자동적으로 id에 빈값을 넣어 null로 만들어뒀는데
			//사용자가 실수로 json형태로 id를 줘서 요청하게 되면 안되기 때문에 설정하는것
			//해당 엔티티가 새로운 데이터임을 보장하게 된다.
			entity.setId(null);
			
			//임시 userId 설정
			//유저아이디에 위에 변수로 지정해둔 내용으로 설정
			//지금은 인증과 인가 기능이 없으므로 임시유저(temporary-user)만 로그인없이 사용 가능한
			//애플리케이션인 셈이다.
			entity.setUserId(userId);
			
			//서비스 레이어의 create 메서드를 호출하여, TodoEntity를 데이터베이스에 저장하는 작업을 수행한다.
			//이 메서드는 저장된 TodoEntity 객체를 저장한 리스트를 반환한다.
			//entities라는 변수명으로 코드 작성해보기
			
			//TodoService에 create 메서드 반환형에 따라 타입을 맞춘다.
			List<TodoEntity> entities = service.create(entity);
			//추가
			
			
			//-----------------------------------------------
			//조회
			
			//자바 스트림을 이용해 반환된 엔티티리스트를 TodoDTO객체를 담은 리스트로 반환한다.
			//response 내보낼때 TodoDTO로 내보내기로 했으니 TodoDTO
			//entities.stream().map(TodoDTO::new) : TodoEntity 객체들을 TodoDTO 객체들로 변환하는 과정
			//.collect(Collectors.toList()): 스트림으로 변환된 객체들을 리스트로 다시 수집한다.
			List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
			
			//변환된 TodoDTO 객체를 담고있는 리스트를 이용해 ResponseDTO의 data필드에 대입한다.
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
			
			return ResponseEntity.ok().body(response); //responseDTO 객체를 돌려줘야함
			
		} catch (Exception e) {
			//혹시 예외가 발생하는 경우 responseDTO객체에 대신 error 메시지를 넣어 반환한다.
			String error = e.getMessage();
			
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
			return ResponseEntity.badRequest().body(response);
			
		}
	}
	
	
	//조회 기능만 있음
	@GetMapping
	public ResponseEntity<?> retrieveTodoList(@AuthenticationPrincipal String userId){
		
		//서비스 레이어의 retrieve 메서드를 이용해 TodoEntity가 담겨있는 리스트를 반환받아 entities에 저장한다.
		List<TodoEntity> entities = service.retrieve(userId);
		
		//자바 스트림을 이용해 반환된 리스트를 TodoDTO 객체로 변환하고 리스트로 변환하여 dtos에 저장한다.
		//map(TodoDTO::new) -> .map(entity -> new TodoDTO(entity))
		List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
		
		 //변환된 dtos리스트를 이용해 responseDTO에 담고 ResponseEntity를 이용해 응답을 반환한다.
		ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
		return  ResponseEntity.ok().body(response);
	} 
	
	//수정 기능
	//외부로부터 수정하려고 하는 entity를 요청을 통해 받는다.
	@PutMapping //수정이니까 put매핑을 사용
	public ResponseEntity<?> updateTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto){
		
		//dto -> Entity로 변환
		TodoEntity entity = TodoDTO.toEntity(dto);
		//dto에는 userId에 대한 정보가 없기 때문에 임시유저를 묶어서 보내야 한다.
		entity.setUserId(userId);
		
		List<TodoEntity> entities = service.update(entity);
		
		//응답으로 돌려주려면 Entity -> DTO 변환
		List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
		
		ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
		return ResponseEntity.ok().body(response);
	}
	
	
	//삭제하기
	@DeleteMapping
	public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal String userId,@RequestBody TodoDTO dto){
		
		try {
			
			//1. 엔티티로 변경 //DTO -> Entity로 변경해주는 메서드 toEntity
			TodoEntity entity = TodoDTO.toEntity(dto);
			//2.임시 유저 아이디 설정
			entity.setUserId(userId);
			//3.서비스를 이용해 entity를 삭제
			List<TodoEntity> entities = service.delete(entity);
			//4. 자바 스트림을 이용해 반환된 엔티티 리스트들을 TodoDTO리스트로 변환한다.
			List<TodoDTO> dtos = entities.stream().map(TodoDTO::new).collect(Collectors.toList());
			
			//5. ResponseDTO에 담아서 반환한다.
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
			return ResponseEntity.ok().body(response);
			
		} catch (Exception e) {
			
			//6. 예외가 발생하는 경우 ResponseDTO에 error를 실어서 반환한다
			String error = e.getMessage();
			ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
			return ResponseEntity.badRequest().body(response);
		
		}
	}
	
	
	
	
}
