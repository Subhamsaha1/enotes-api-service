package com.enotes.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.enotes.dto.CategoryDto;
import com.enotes.dto.ToDoDto;
import com.enotes.dto.ToDoDto.StatusDto;
import com.enotes.enums.TodoStatus;
import com.enotes.exception.ResourceNotFoundException;
import com.enotes.exception.ValidationException;

@Component
public class Validation {

	public void categoryValidation(CategoryDto categoryDto) {

		Map<String, Object> error = new LinkedHashMap<>();
		if (ObjectUtils.isEmpty(categoryDto)) {
			throw new IllegalArgumentException("Category Object/JSON should not be null or empty");
		} else {

			// Validation for Name Field
			if (ObjectUtils.isEmpty(categoryDto.getName())) {
				error.put("name", "name field is empty or null");
			} else {
				if (categoryDto.getName().length() < 3) {
					error.put("name", "name length min. 3");
				}
				if (categoryDto.getName().length() >= 100) {
					error.put("name", "name length max. 100");
				}
			}

			// validation for isActive field
			if (ObjectUtils.isEmpty(categoryDto.getDescription())) {
				error.put("description", "Description field is empty or null");
			}

			// Validation for Name Field
			if (ObjectUtils.isEmpty(categoryDto.getIsActive())) {
				error.put("isActive", "isActive field is empty or null");
			} else {
				if (categoryDto.getIsActive() != Boolean.TRUE.booleanValue()
						&& categoryDto.getIsActive() != Boolean.FALSE.booleanValue()) {
					error.put("isActive", "Invalid value for isActive field");

				}
			}
		}

		if (!error.isEmpty()) {
			throw new ValidationException(error);
		}
	}

	public void todoValidation(ToDoDto todo) throws Exception {
		StatusDto reqStatus = todo.getStatus();
		Boolean statusFound = false;
		for (TodoStatus st : TodoStatus.values()) {
			if (st.getId().equals(reqStatus.getId())) {
				statusFound = true;
			}
		}
		if (!statusFound) {
			throw new ResourceNotFoundException("Invalid status!");
		}
	}

}
