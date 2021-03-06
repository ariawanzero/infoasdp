package com.asdp.util;

public class SystemRestConstant {
	public static final class UserConstant {
		private UserConstant() {
		}

		public static final String USER_CONTROLLER = "/user";
		public static final String SAVE_USER_ADDR = "/saveUser";
		public static final String FIND_USER_DETAIL_ADDR = "/findUserDetail";
		public static final String SEARCH_USER_DETAIL_ADDR = "/searchUser";
		public static final String SEARCH_HISTORY_LOGIN_ADDR = "/searchHistoryLogin";
		public static final String CHANGE_PASSWORD_ADDR = "/changePassword";
		public static final String SAVE_USER_AUTH = "SAVE_PRE_SCREENING";
		public static final String COUNT_HISTORY_LOGIN_ADDR = "/countTodayLogin";
	}
	
	public static final class QuizConstant {
		private QuizConstant() {
		}

		public static final String QUIZ_CONTROLLER = "/quiz";
		public static final String SAVE_QUIZ_ADDR = "/saveQuiz";
		public static final String FIND_QUIZ_DETAIL_ADDR = "/findQuizDetail";
		public static final String SEARCH_QUIZ_ADDR = "/searchQuiz";
		public static final String PREVIEW_FILE_ADDR = "/previewFile";
		
		public static final String MATERI_QUIZ_CONTROLLER = "/materi";
		public static final String SAVE_MATERI_QUIZ_HEADER_ADDR = "/saveMateriHeader";
		public static final String UPLOAD_MATERI_QUIZ_ADDR = "/uploadMateri";
		public static final String FIND_MATERI_QUIZ_DETAIL_ADDR = "/findMateriDetail";
		public static final String SEARCH_MATERI_QUIZ_ADDR = "/searchMateri";
		public static final String SEARCH_QUESTION_ADDR = "/searchQuestion";
		public static final String SAVE_QUESTION_ADDR = "/saveQuestion";
		public static final String DOWNLOAD_FILE_ADDR = "/downloadFile";
		public static final String START_QUIZ_ADDR = "/startQuiz";
		public static final String ANSWER_QUIZ_ADDR = "/answerQuiz";
		public static final String FINISH_QUIZ_ADDR = "/finishQuiz";
		public static final String PUBLISH_QUIZ_ADDR = "/publishQuiz";
		public static final String RESULT_QUIZ_ADDR = "/resultQuiz";

		public static final String RESULT_QUIZ_CONTROLLER = "/result";
		public static final String SEARCH_RESULT_QUIZ_CONTROLLER = "/searchResultQuiz";
		public static final String DETAIL_RESULT_QUIZ_CONTROLLER = "/detailResultQuiz";
		public static final String DOWNLOAD_RESULT_QUIZ_CONTROLLER = "/downloadResultQuiz";
	}
	public static final class DocumentConstant {
		private DocumentConstant() {
		}
		
		public static final String DOCUMENT_CONTROLLER = "/document";
		public static final String SAVE_DOCUMENT_ADDR = "/saveDocument";
		public static final String FIND_DOCUMENT_DETAIL_ADDR = "/findDocumentDetail";
		public static final String SEARCH_DOCUMENT_ADDR = "/searchDocument";
		public static final String SEARCH_DOCUMENT_PENDING_ADDR = "/searchDocumentPending";
		public static final String APPROVE_DOCUMENT_ADDR = "/approveDocument";
		public static final String REJECTED_DOCUMENT_ADDR = "/rejectedDocument";
		public static final String SEARCH_DOCUMENT_HISTORY_ADDR = "/searchDocumentHistory";
		public static final String SEARCH_DETAIL_DOCUMENT_HISTORY_ADDR = "/searchDetailDocumentHistory";
		public static final String UPLOAD_DOCUMENT_ADDR = "/uploadDocument";
		public static final String READ_DOCUMENT_DETAIL_ADDR = "/readDocumentDetail";
		public static final String DASHBOAR_SEARCH_ADDR = "/dashboardSearch";
	}
	
	public static final class OpenFileConstant {
		private OpenFileConstant() {
		}

		public static final String OPEN_CONTROLLER = "/open";
		public static final String PREVIEW_FILE_ADDR = "/previewFile";
	}
	public static final class SysParamConstant {
		private SysParamConstant() {
		}
		
		public static final String SYS_PARAM_CONTROLLER = "/sysparam";
		public static final String FIND_PARAM_BY_TYPE_CONTROLLER = "/findParamByType";
		public static final String SEARCH_PARAM_CONTROLLER = "/searchParam";
		public static final String SAVE_PARAM_ADDR = "/saveParam";
		public static final String FIND_PARAM_DETAIL_ADDR = "/findParamDetail";
	}
}
