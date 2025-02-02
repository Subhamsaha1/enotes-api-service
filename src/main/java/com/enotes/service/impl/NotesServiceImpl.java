package com.enotes.service.impl;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.enotes.dto.NotesDto;
import com.enotes.dto.NotesDto.CategoryDto;
import com.enotes.entity.Category;
import com.enotes.entity.Notes;
import com.enotes.exception.ResourceNotFoundException;
import com.enotes.repository.CategoryRepository;
import com.enotes.repository.NotesRepository;
import com.enotes.service.NoteService;

@Service
public class NotesServiceImpl implements NoteService{

	@Autowired
	private NotesRepository notesRepository;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@Override
	public Boolean saveNotes(NotesDto notesDto) throws Exception {
		
		//category validation
		checkCategoryExist(notesDto.getCategory());
		
		Notes notes = mapper.map(notesDto, Notes.class);
		
		Notes saveNotes = notesRepository.save(notes);
		if(!ObjectUtils.isEmpty(saveNotes)) {
			return true;
		}
		return false;
	}

	private void checkCategoryExist(CategoryDto category) throws Exception{

		categoryRepo.findById(category.getId())
		.orElseThrow(()->new ResourceNotFoundException("Category Id is Invalid"));
		
	}

	@Override
	public List<NotesDto> getAllNotes() {
		
		return notesRepository.findAll().stream()
				.map(note -> mapper.map(note, NotesDto.class))
				.toList();
		
	}

}
