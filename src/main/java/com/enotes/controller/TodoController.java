package com.enotes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enotes.dto.ToDoDto;
import com.enotes.service.TodoService;
import com.enotes.util.CommonUtil;

@RestController
@RequestMapping("/api/v1/todo")
public class TodoController {
	
	@Autowired
	private TodoService todoService;
	
	@PostMapping("/")
	public ResponseEntity<?> saveTodo(@RequestBody ToDoDto todo) throws Exception{
		Boolean saveTodo = todoService.saveTodo(todo);
		if(saveTodo) {
			return CommonUtil.createBuildResponseMessage("Todo Saved Successfully!", HttpStatus.CREATED);
		}else {
			return CommonUtil.createErrorResponseMessage("Todo save failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getTodo(@PathVariable Integer id) throws Exception{
		ToDoDto todo = todoService.getTodoById(id);
		return CommonUtil.createBuildResponse(todo, HttpStatus.OK);
	}
	
	@GetMapping("/list")
	public ResponseEntity<?> getAllTodoByUser(@PathVariable Integer id) throws Exception{
		List<ToDoDto> todoList = todoService.getTodoByUser();
		if(CollectionUtils.isEmpty(todoList))
		{
			return ResponseEntity.noContent().build();
		}
		return CommonUtil.createBuildResponse(todoList, HttpStatus.OK);
	}

}
