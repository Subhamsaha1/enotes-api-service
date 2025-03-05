package com.enotes.service.impl;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.enotes.dto.ToDoDto;
import com.enotes.dto.ToDoDto.StatusDto;
import com.enotes.entity.Todo;
import com.enotes.enums.TodoStatus;
import com.enotes.exception.ResourceNotFoundException;
import com.enotes.repository.TodoRepository;
import com.enotes.service.TodoService;
import com.enotes.util.Validation;

@Service
public class TodoServiceImpl implements TodoService{

	@Autowired
	private TodoRepository todoRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@Autowired
	private Validation validation;
	
	@Override
	public Boolean saveTodo(ToDoDto todoDto) throws Exception{
		//validate todo status
		validation.todoValidation(todoDto);
		
		Todo todo = mapper.map(todoDto, Todo.class);
		todo.setStatusId(todoDto.getStatus().getId());
		Todo saveTodo = todoRepo.save(todo);
		if(!ObjectUtils.isEmpty(saveTodo)) {
			return true;
		}
		return false;
	}

	@Override
	public ToDoDto getTodoById(Integer id) throws Exception {
		Todo todo = todoRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Todo Not Found!! Id invalid"));
		ToDoDto todoDto = mapper.map(todo, ToDoDto.class);
		setStatus(todoDto, todo);
		return todoDto;
	}

	private void setStatus(ToDoDto todoDto, Todo todo) {
		for(TodoStatus st:TodoStatus.values()) {
			if(st.getId().equals(todo.getStatusId())) {
				StatusDto statusDto = StatusDto.builder()
						.id(st.getId())
						.name(st.getName())
						.build();
				todoDto.setStatus(statusDto);
			}
		}
		
	}

	@Override
	public List<ToDoDto> getTodoByUser() {
		Integer userId=1;
		List<Todo> todos = todoRepo.findByCreatedBy(userId);
		return todos.stream().map(td-> mapper.map(td, ToDoDto.class)).toList();
		
	}

}
