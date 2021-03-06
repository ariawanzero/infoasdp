package com.asdp.service;

import java.io.ByteArrayOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.asdp.entity.QuestionEntity;
import com.asdp.entity.QuizEntity;
import com.asdp.request.QuestionRequest;
import com.asdp.request.QuizSearchRequest;
import com.asdp.request.ResultQuizSearchRequest;

public interface QuizService {
	Resource download(String nameFile) throws Exception;
	String searchMateriQuiz(QuizSearchRequest request) throws Exception;
	String searchResultQuiz(ResultQuizSearchRequest request) throws Exception;
	String searchMateriQuestion(QuestionRequest request) throws Exception;
	String findOneById(String id) throws Exception;
	String saveHeader(QuizEntity request) throws Exception;
	String save(MultipartFile file, String id) throws Exception;
	String saveQuizWithQuestion(QuestionEntity request) throws Exception;
	String startQuiz(String id) throws Exception;
	String answerQuiz(QuestionEntity question) throws Exception;
	String publishQuiz(QuizEntity request) throws Exception;
	String detailResultQuiz(ResultQuizSearchRequest request) throws Exception;
	String resultQuiz(QuizSearchRequest request) throws Exception;
	ByteArrayOutputStream downloadResulQuiz(String request) throws Exception;
}
