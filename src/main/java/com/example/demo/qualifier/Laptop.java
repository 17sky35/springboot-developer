package com.example.demo.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

//노트북
@Component
@Qualifier("Laptop")
public class Laptop implements Computer1{

	@Override
	public int getScreenWidth() {
		return 1600;
	}
}
