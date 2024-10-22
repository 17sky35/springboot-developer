package com.example.demo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Data
public class DemoModel {
	
	@NonNull
	String id;
}
