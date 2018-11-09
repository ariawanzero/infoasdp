package com.asdp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.asdp.entity.EmailEntity;
import com.asdp.entity.QuestionEntity;
import com.asdp.entity.QuizEntity;
import com.asdp.entity.ResultQuizEntity;
import com.asdp.entity.UserEntity;
import com.asdp.repository.EmailRepository;
import com.asdp.repository.QuestionRepository;
import com.asdp.repository.QuizRepository;
import com.asdp.repository.ResultQuizRepository;
import com.asdp.request.QuizSearchRequest;
import com.asdp.util.CommonPageUtil;
import com.asdp.util.CommonPaging;
import com.asdp.util.CommonResponse;
import com.asdp.util.CommonResponseGenerator;
import com.asdp.util.CommonResponsePaging;
import com.asdp.util.DateTimeFunction;
import com.asdp.util.EmailUtils;
import com.asdp.util.JsonFilter;
import com.asdp.util.JsonUtil;
import com.asdp.util.StringFunction;
import com.asdp.util.SystemConstant;
import com.asdp.util.SystemConstant.UploadConstants;
import com.asdp.util.SystemConstant.ValidFlag;
import com.asdp.util.SystemRestConstant.OpenFileConstant;
import com.asdp.util.SystemRestConstant.QuizConstant;
import com.asdp.util.UserException;
import com.fasterxml.jackson.databind.ObjectWriter;

import liquibase.util.file.FilenameUtils;

public class QuizServiceImpl implements QuizService{

	Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private CommonResponseGenerator comGen;
	
	@Autowired
	private QuizRepository quizRepo;
	
	@Autowired
	private QuestionRepository questionRepo;
	
	@Autowired
	private ResultQuizRepository resultQuizRepo;

	@Autowired
	private CommonPageUtil pageUtil;
	
	@Autowired
	StorageService storageService;
	
	@Autowired
	private EmailRepository emailRepo;
	
	@PersistenceContext
	EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public String save(MultipartFile file, String id) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		UserEntity users = new UserEntity();
		BeanUtils.copyProperties(principal, users);
		
		QuizEntity en = quizRepo.findById(id).orElseThrow(() -> new UserException("400", "Data not Exists"));
		List<String> fileName = new ArrayList<>();
		
		if(en.getNameFileJson() != null) {
			fileName = JsonUtil.parseJson(en.getNameFileJson(), ArrayList.class);
		}
		
		try {
			String name = en.getName()
							.concat("-")
							.concat(String.valueOf(fileName.size() + 1))
							.concat(".")
							.concat(FilenameUtils.getExtension(file.getOriginalFilename()));
			
			fileName.add(name);
			storageService.store(file, name);
		} catch (Exception e) {
			throw new UserException("400", "Fail Transfer File to Server !");
		}
		
		QuizEntity materi = en;
		materi.setNameFileJson(JsonUtil.generateJson(fileName));
		materi.setModifiedBy(users.getUsername());
		materi.setModifiedDate(new Date());
		quizRepo.save(materi);
		
		CommonResponse<String> response = comGen.generateCommonResponse(SystemConstant.SUCCESS);
		return JsonUtil.generateDefaultJsonWriter().writeValueAsString(response);
	}

	@Override
	public Resource download(String nameFile) throws Exception {
		return storageService.loadFile(nameFile);
	}

	@Override
	public String searchMateriQuiz(QuizSearchRequest request) throws Exception {
		//List<QuizEntity> listExpired = new ArrayList<>();
		
		Pageable pageable = pageUtil.generateDefaultPageRequest(request.getPage(),
				new Sort(Sort.Direction.ASC, QuizEntity.Constant.NAME_FIELD));
		
		Specification<QuizEntity> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> list = new ArrayList<>();
			list.add(criteriaBuilder.equal(root.<String>get(QuizEntity.Constant.VALID_FIELD),
						ValidFlag.VALID));
			
			if (!StringFunction.isEmpty(request.getName())) {
				list.add(criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(QuizEntity.Constant.NAME_FIELD)),
						SystemConstant.WILDCARD + request.getName().toLowerCase() + SystemConstant.WILDCARD));
			}
			
			if (!StringFunction.isEmpty(request.getDivisi())) {
				list.add(criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(QuizEntity.Constant.DIVISI_FIELD)),
						SystemConstant.WILDCARD + request.getDivisi().toLowerCase() + SystemConstant.WILDCARD));
			}
			
			return criteriaBuilder.and(list.toArray(new Predicate[] {}));
		};
		
		Page<QuizEntity> paging = quizRepo.findAll(spec, pageable);
		
		CommonResponsePaging<QuizEntity> restResponse = comGen
				.generateCommonResponsePaging(new CommonPaging<>(paging));
		
		ObjectWriter writer = JsonUtil.generateJsonWriterWithFilter(
				new JsonFilter(QuizEntity.Constant.JSON_FILTER),
				new JsonFilter(QuizEntity.Constant.JSON_FILTER, QuizEntity.Constant.QUESTION_FIELD));
		
		return writer.writeValueAsString(restResponse);
	}
	
	@Async
	private void updateStatusInvalid(List<QuizEntity> listQuiz){
		quizRepo.saveAll(listQuiz);
	}
	
	private boolean isExistMateriByMateriName(String name, String id) {
		Specification<QuizEntity> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> list = new ArrayList<>();
			list.add(criteriaBuilder.equal(root.<Integer>get(QuizEntity.Constant.VALID_FIELD), SystemConstant.ValidFlag.VALID));
			list.add(criteriaBuilder.equal(criteriaBuilder.lower(root.<String>get(QuizEntity.Constant.NAME_FIELD)),
					name.toLowerCase()));
			
			if(id != null) {
				list.add(criteriaBuilder.notEqual(root.<String>get(QuizEntity.Constant.ID_FIELD),id.toLowerCase()));
			}
			return criteriaBuilder.and(list.toArray(new Predicate[] {}));
		};
		
		Long rowCount = quizRepo.count(spec);
		return (rowCount != null && rowCount > 0 ? true : false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String findOneById(String id) throws Exception {
		QuizEntity materi = quizRepo.findById(id).orElseThrow(() -> new UserException("400", "Quiz not found"));
		
		if(materi.getNameFileJson() != null) {
			materi.setNameFile(JsonUtil.parseJson(materi.getNameFileJson(), ArrayList.class));
		}
		materi.setUrlPreview(UploadConstants.URL_PREVIEW.concat(OpenFileConstant.OPEN_CONTROLLER)
				.concat(QuizConstant.PREVIEW_FILE_ADDR).concat("?name="));
		materi.getQuestionList();
		
		CommonResponse<QuizEntity> response = new CommonResponse<>(materi);
		ObjectWriter writter = JsonUtil.generateJsonWriterWithFilter(
				new JsonFilter(QuizEntity.Constant.JSON_FILTER),
				new JsonFilter(QuizEntity.Constant.JSON_FILTER, QuizEntity.Constant.NAME_FILE_JSON_FIELD, QuizEntity.Constant.QUESTION_FIELD));

		return writter.writeValueAsString(response);
	}

	@Override
	public String saveHeader(QuizEntity request) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		UserEntity users = new UserEntity();
		BeanUtils.copyProperties(principal, users);
		
		QuizEntity toUpdate = request;
		if (request == null || StringFunction.isEmpty(request.getName())) {
			throw new UserException("400", "Quiz Name is mandatory !");
		}
		if (isExistMateriByMateriName(request.getName(), request.getId())) {
			throw new UserException("400", "Quiz with that Name already exists !");
		}
		
		if (StringFunction.isNotEmpty(request.getId())) {
			Optional<QuizEntity> existUser = quizRepo.findById(request.getId());
			if (existUser == null) {
				throw new UserException("400", "Quiz not found !");
			} else {
				toUpdate = existUser.get();
			}
			
			BeanUtils.copyProperties(request, toUpdate);

			toUpdate.setModifiedBy(users.getUsername());
			toUpdate.setModifiedDate(new Date());
		}else {
			toUpdate.setCreatedBy(users.getUsername());
			toUpdate.setCreatedDate(new Date());
		}
		
		quizRepo.save(toUpdate);
		
		CommonResponse<String> response = comGen.generateCommonResponse(SystemConstant.SUCCESS);
		return JsonUtil.generateDefaultJsonWriter().writeValueAsString(response);
	}
	
	@Override
	public String saveQuizWithQuestion(QuizEntity request) throws Exception {
		Optional<QuizEntity> quiz = null;
		
		if (StringFunction.isEmpty(request.getId())) {
			throw new UserException("400", "Quiz is Mandatory!");
		}
		
		quiz = quizRepo.findById(request.getId());
		if (quiz == null) {
			throw new UserException("400", "Quiz not Found!");
		}
		List<QuestionEntity> questions = new ArrayList<>();
		if(request.getQuestion() != null && !request.getQuestion().isEmpty()) {
			List<QuestionEntity> temp = new ArrayList<>(request.getQuestion());

			for (QuestionEntity e : temp) {
				e.setQuiz(quiz.get());
				questions.add(e);
			}
		}
		
		if (!questions.isEmpty()) {
			questionRepo.saveAll(questions);
		}
		
		CommonResponse<String> response = comGen.generateCommonResponse(SystemConstant.SUCCESS);
		return JsonUtil.generateDefaultJsonWriter().writeValueAsString(response);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String startQuiz(String id) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		UserEntity users = new UserEntity();
		BeanUtils.copyProperties(principal, users);
		Random rand = new Random();

		QuizEntity quiz = quizRepo.findById(id).orElseThrow(() -> new UserException("400", "Quiz not found"));
		
		/*if(quiz.getPassQuiz() != 1 || !DateTimeFunction.getTimeExpired(quiz.getStartDate())) {
			throw new UserException("400", "the quiz hasn't started yet !");
		}*/
		
		ResultQuizEntity resultQuiz = resultQuizRepo.findByUsernameAndQuiz(users.getUsername(), quiz.getId());
		List<QuestionEntity> questions;
		List<QuestionEntity> listQuestionFinal = new ArrayList<>();
		Map<String, String> mapQuestion = new HashMap<>();
		if(resultQuiz == null) {
			questions = questionRepo.findByQuiz(quiz);
			for(int i=0; i<quiz.getTotalQuiz(); i++) {
				int randomIndex = rand.nextInt(questions.size());

				mapQuestion.put(questions.get(randomIndex).getId(), " ");
				listQuestionFinal.add(questions.get(randomIndex));
				
				questions.remove(randomIndex);
			}
			resultQuiz = new ResultQuizEntity();
			String mapQuesions = JsonUtil.generateJson(mapQuestion);
			resultQuiz.setQuestionAnswerJson(mapQuesions);
			resultQuiz.setQuestionAnswer(mapQuestion);
			resultQuiz.setQuiz(quiz.getId());
			resultQuiz.setUsername(users.getUsername());
			resultQuiz.setQuestions(listQuestionFinal);
			
			resultQuizRepo.save(resultQuiz);
		}else {
			mapQuestion = JsonUtil.parseJson(resultQuiz.getQuestionAnswerJson(), HashMap.class);
			ArrayList<String> listId = new ArrayList<String>(mapQuestion.keySet());
			
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<QuestionEntity> query = cb.createQuery(QuestionEntity.class);
			Root<QuestionEntity> root = query.from(QuestionEntity.class);
			Expression<String> parentExpression = root.get(QuestionEntity.Constant.ID_FIELD);			
			Predicate parentPredicate = parentExpression.in(listId);
			query.select(root).where(parentPredicate);

			questions = em.createQuery(query).getResultList();
			resultQuiz.setQuestions(questions);
		}
		
		CommonResponse<ResultQuizEntity> response = new CommonResponse<>(resultQuiz);
		ObjectWriter writter = JsonUtil.generateJsonWriterWithFilter(
				new JsonFilter(ResultQuizEntity.Constant.JSON_FILTER),
				new JsonFilter(ResultQuizEntity.Constant.JSON_FILTER, ResultQuizEntity.Constant.QUESTION_ANSWER_JSON_FIELD),
				new JsonFilter(QuestionEntity.Constant.JSON_FILTER, QuestionEntity.Constant.QUIZ_FIELD));

		return writter.writeValueAsString(response);
	}

	@Override
	public String publishQuiz(QuizEntity request) throws Exception {
		QuizEntity quiz = quizRepo.findById(request.getId()).orElseThrow(() -> new UserException("400", "Quiz not found"));
		if(DateTimeFunction.getTimeExpired(quiz.getStartDate())){
			throw new UserException("400", "Start date has passed, please edit start date !");
		}
		quiz.setPassQuiz(ValidFlag.VALID);
		quizRepo.save(quiz);
		
		sendNotificationQuiz(quiz);

		CommonResponse<String> response = comGen.generateCommonResponse(SystemConstant.SUCCESS);
		return JsonUtil.generateDefaultJsonWriter().writeValueAsString(response);
	}
	
	@Async
	public void sendNotificationQuiz(QuizEntity quiz) throws Exception{
		CriteriaBuilder critBuilder = em.getCriteriaBuilder();
		
		CriteriaQuery<UserEntity> query = critBuilder.createQuery(UserEntity.class);
		Root<UserEntity> root = query.from(UserEntity.class);
		List<Predicate> lstWhere = new ArrayList<Predicate>();
		lstWhere.add(critBuilder.like(root.get(UserEntity.Constant.DIVISI_FIELD), 
				SystemConstant.WILDCARD + quiz.getDivisi().toLowerCase() + SystemConstant.WILDCARD));
		query.select(root).where(lstWhere.toArray(new Predicate[] {}));
		List<UserEntity> users = em.createQuery(query).getResultList();
		users.stream().filter(user -> user.getUserRole().getUserRoleCode() != "0" && user.getUserRole().getUserRoleCode() != "1");
		
		String dateFormat = "HH:mm dd-MMM-yyyy";
		String date = DateTimeFunction.date2String(quiz.getStartDate(), dateFormat).concat(" - ").concat(DateTimeFunction.date2String(quiz.getEndDate(), dateFormat));
		Optional<EmailEntity> email = emailRepo.findById("QUIZNOTIF");
		for(UserEntity user : users) {
			EmailUtils.sendEmail(user.getUsername(), String.format(email.get().getBodyMessage(), user.getName(), quiz.getName(), date), email.get().getSubject());
		}
		
	}
}
