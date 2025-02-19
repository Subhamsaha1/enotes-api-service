package com.enotes.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.enotes.dto.NotesDto;
import com.enotes.dto.NotesResponse;
import com.enotes.entity.FileDetails;
import com.enotes.exception.ResourceNotFoundException;

public interface NoteService {
	
	public Boolean saveNotes(String notes, MultipartFile file)
			throws Exception;
	
	public List<NotesDto> getAllNotes();

	public byte[] downloadFile(FileDetails fileDetails) throws Exception;

	public FileDetails getFileDetails(Integer id) throws Exception;

	public NotesResponse getAllNotesByUser(Integer userId, Integer pageNo, Integer pageSize);

	public void softDeleteNotes(Integer id) throws ResourceNotFoundException;

	public void restoreNotes(Integer id) throws ResourceNotFoundException;

	public List<NotesDto> getUserRecycleBinNotes(Integer id);

	public void hardDeleteNotes(Integer id) throws Exception;

	public void emptyRecycleBin(int userId);
}
