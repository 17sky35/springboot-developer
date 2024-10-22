package com.example.demo.di4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.demo.qualifier.Computer1;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//코딩을 하려면 컴퓨터가 필요하다

@Component
@RequiredArgsConstructor //생성자 생성
@Getter
public class Coding {
	
	//의존성 주입 final이나 @NonNull 붙히고 
	private final Computer1 computer;
	
	
	//생성자 주입(Constructor injection) (이 사용법을 권장하고 가장 많이씀)
	//생성자가 호출되는 시점 -> 객체가 만들어질 때 매개변수에 객체가 주입이된다.
//	public Coding(Computer computer) {
//		this.computer = computer;
//	}
	
	//setter 주입(setter injection)
	//@Autowired
	//public void setComputer(Computer computer) {
	//	this.computer = computer;
	//}
	
	
//	public Computer getComputer() {
//		return computer;
//	}

	
}
