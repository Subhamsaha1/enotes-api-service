package com.enotes.service;

import java.util.List;

import com.enotes.dto.ToDoDto;

public interface TodoService {

	public Boolean saveTodo(ToDoDto todo) throws Exception;
	
	public ToDoDto getTodoById(Integer id) throws Exception;
	
	public List<ToDoDto> getTodoByUser();

}
