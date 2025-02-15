package com.enotes.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.enotes.dto.NotesDto;

public interface NoteService {
	
	public Boolean saveNotes(String notes, MultipartFile file)
			throws Exception;
	
	public List<NotesDto> getAllNotes();

}
