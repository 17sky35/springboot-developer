package com.example.demo.di4;

import org.springframework.stereotype.Component;

//import org.springframework.stereotype.Component;

//코딩을 하기위해서는 컴퓨타가 필요하다.
//컴퓨터에는 ram이라는 부품이 있다.
@Component
public class Computer {
	
	int ram = 32;
	
	public int getRam() {
		return ram;
	}
}
