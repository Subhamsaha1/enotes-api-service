package com.enotes.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.enotes.dto.NotesDto;
import com.enotes.dto.NotesDto.CategoryDto;
import com.enotes.dto.NotesDto.FilesDto;
import com.enotes.dto.NotesResponse;
import com.enotes.entity.Category;
import com.enotes.entity.FileDetails;
import com.enotes.entity.Notes;
import com.enotes.exception.ResourceNotFoundException;
import com.enotes.repository.CategoryRepository;
import com.enotes.repository.FileRepository;
import com.enotes.repository.NotesRepository;
import com.enotes.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotesServiceImpl implements NoteService{

	@Autowired
	private NotesRepository notesRepository;
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	@Autowired
	private FileRepository fileRepo;
	
	@Override
	public Boolean saveNotes(String notes, MultipartFile file) 
			throws Exception {
		
		ObjectMapper ob = new ObjectMapper();
		NotesDto notesDto = ob.readValue(notes, NotesDto.class);
		notesDto.setIsDeleted(false);
		notesDto.setDeletedOn(null);		
		
		// update notes if id is given in request
		if(!ObjectUtils.isEmpty(notesDto.getId())){
			updateNotes(notesDto, file);
		}
		
		
		//category validation
		checkCategoryExist(notesDto.getCategory());
		
		Notes notesMap = mapper.map(notesDto, Notes.class);
		
		FileDetails fileDtls = saveFileDetails(file);
		
		if(!ObjectUtils.isEmpty(fileDtls)) {
			notesMap.setFileDetails(fileDtls);
		}else {
			if(ObjectUtils.isEmpty(notesDto.getId())){
				notesMap.setFileDetails(null);
			}
		}
		
		Notes saveNotes = notesRepository.save(notesMap);
		if(!ObjectUtils.isEmpty(saveNotes)) {
			return true;
		}
		return false;
	}

	private void updateNotes(NotesDto notesDto, MultipartFile file) throws Exception{

		Notes existNotes = notesRepository.findById(notesDto.getId())
				.orElseThrow(()-> new ResourceNotFoundException("Invalid Notes id"));
		
		// user not choose any file at update time
		if(ObjectUtils.isEmpty(file)) {
			notesDto.setFileDetails(mapper.map(existNotes.getFileDetails(), FilesDto.class));
		}
		
	}

	private FileDetails saveFileDetails(MultipartFile file) throws IOException {
		if( !ObjectUtils.isEmpty(file) && !file.isEmpty()) {
			
			String originalFilename = file.getOriginalFilename();
			String extension = FilenameUtils.getExtension(originalFilename);
			
			List<String> extensionAllow = Arrays.asList("pdf","xlsx","jpg", "png");
			if(!extensionAllow.contains(extension)) {
				throw new IllegalArgumentException("Invalid file format! Upload only .pdf, .xlsx, .jpg, .png file");
			}
			
			String rndString = UUID.randomUUID().toString();
			String uploadFileName = rndString + "." + extension;
			
			File saveFile = new File(uploadPath);
			if(!saveFile.exists()) {
				saveFile.mkdir();
			}
			
			// path : enotesapiservice/notes/java.pdf
			String storePath = uploadPath.concat(uploadFileName);
			
			
			//upload file
			long upload = Files.copy(file.getInputStream(), Paths.get(storePath));
			if(upload != 0) {
				FileDetails fileDtls = new FileDetails();
				fileDtls.setOriginalFileName(originalFilename);
				fileDtls.setDisplayFileName(getDisplayName(originalFilename));
				fileDtls.setUploadFileName(uploadFileName);
				fileDtls.setPath(storePath);
				fileDtls.setFileSize(file.getSize());
				FileDetails saveFileDtls = fileRepo.save(fileDtls);
				return saveFileDtls;
			}
		}
		return null;
	}

	private String getDisplayName(String originalFilename) {
		//java_programming_tutorials.pdf
		String extension = FilenameUtils.getExtension(originalFilename);
		String fileName = FilenameUtils.removeExtension(originalFilename);
		
		if(fileName.length()>8) {
			fileName = fileName.substring(0, 7);
		}
		fileName = fileName + "." + extension;
		return fileName;
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

	@Override
	public byte[] downloadFile(FileDetails fileDetails) throws Exception {

		InputStream io = new FileInputStream(fileDetails.getPath());
		return StreamUtils.copyToByteArray(io);
	}

	@Override
	public FileDetails getFileDetails(Integer id) throws Exception {
		FileDetails fileDtls = fileRepo.findById(id).orElseThrow(()-> 
		new ResourceNotFoundException("File is not available"));
		return fileDtls;
	}

	@Override
	public NotesResponse getAllNotesByUser(Integer userId, Integer pageNo, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Notes> pageNotes = notesRepository.findByCreatedByAndIsDeletedFalse(userId, pageable);
		
		List<NotesDto> notesDto = pageNotes.get().map(n-> mapper.map(n, NotesDto.class)).toList();
		
		NotesResponse notes = NotesResponse.builder()
				.notes(notesDto)
				.pageNo(pageNotes.getNumber())
				.pageSize(pageNotes.getSize())
				.totalElements(pageNotes.getTotalElements())
				.totalPages(pageNotes.getTotalPages())
				.isFirst(pageNotes.isFirst())
				.isLast(pageNotes.isLast())
				.build();
		return notes;
	}

	@Override
	public void softDeleteNotes(Integer id) throws ResourceNotFoundException {
		
		Notes notes = notesRepository.findById(id)
		.orElseThrow(()-> new ResourceNotFoundException("Notes id Invalid!"));
		notes.setIsDeleted(true);
		notes.setDeletedOn(new Date());
		notesRepository.save(notes);
		
	}

	@Override
	public void restoreNotes(Integer id) throws ResourceNotFoundException {

		Notes notes = notesRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Notes id Invalid!"));
				notes.setIsDeleted(false);
				notes.setDeletedOn(null);
				notesRepository.save(notes);
	}

	@Override
	public List<NotesDto> getUserRecycleBinNotes(Integer userId) {

		List<Notes> recycleNotes = notesRepository.findByCreatedByAndIsDeletedTrue(userId);
		List<NotesDto> notesDtoList = recycleNotes.stream()
				.map(note->mapper.map(note, NotesDto.class))
				.toList();
		return notesDtoList;
	}
	
}
