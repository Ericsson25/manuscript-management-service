package manuscript.module.manuscript.management;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import manuscript.module.manuscript.management.bean.Author;
import manuscript.module.manuscript.management.bean.CheckSubmissionExistence;
import manuscript.module.manuscript.management.bean.SubmissionStatus;
import manuscript.module.manuscript.management.bean.SubmitLifecycle;
import manuscript.module.manuscript.management.exception.FileUploadException;
import manuscript.module.manuscript.management.exception.SaveSubmissionException;
import manuscript.module.manuscript.management.fileupload.FileManager;
import manuscript.module.manuscript.management.lifecycle.ManuscriptLifecycle;
import manuscript.module.manuscript.management.preload.reply.ManuscriptPreloadReply;
import manuscript.module.manuscript.management.request.RemoveSubmissionRequest;
import manuscript.module.manuscript.management.request.SaveSubmissionDataRequest;
import manuscript.module.manuscript.management.request.SaveSubmissionRequest;
import manuscript.module.manuscript.management.request.SearchAuthorRequest;
import manuscript.module.manuscript.management.request.SubmitSubmissionRequest;
import manuscript.module.manuscript.management.response.AuthorPreloadResponse;
import manuscript.module.manuscript.management.response.EditorPreloadResponse;
import manuscript.module.manuscript.management.response.FileUploadResponse;
import manuscript.module.manuscript.management.response.RemoveSubmissionResponse;
import manuscript.module.manuscript.management.response.ReviewerPreloadResponse;
import manuscript.module.manuscript.management.response.SaveSubmissionDataResponse;
import manuscript.module.manuscript.management.response.SearchAuthorResponse;
import manuscript.module.manuscript.management.response.SubmitSubmissionResponse;
import manuscript.module.user.management.bean.Roles;
import manuscript.module.user.management.bean.SearchUser;
import manuscript.module.user.management.bean.User;
import manuscript.module.user.management.searchuser.SearchUserService;
import manuscript.system.security.bean.AuthenticatedUser;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

	private final static Logger LOGGER = LoggerFactory.getLogger(ManuscriptServiceImpl.class);

	private FileManager fileManager;
	private ManuscriptDao manuscriptDao;
	private SearchUserService searchUserService;
	private ManuscriptLifecycle manuscriptLifecycle;

	public ManuscriptServiceImpl(FileManager fileManager, ManuscriptDao manuscriptDao, SearchUserService searchUserService,
			ManuscriptLifecycle manuscriptLifecycle) {
		this.fileManager = fileManager;
		this.manuscriptDao = manuscriptDao;
		this.searchUserService = searchUserService;
		this.manuscriptLifecycle = manuscriptLifecycle;
	}

	@Override
	public ManuscriptPreloadReply<?> preload(Roles role) {
		if (role.equals(Roles.AUTHOR_ROLE)) {

			ManuscriptPreloadReply<AuthorPreloadResponse> reply = new ManuscriptPreloadReply<AuthorPreloadResponse>();
			reply.setPreloadReply(authorPreload());
			return reply;

		} else if (role.equals(Roles.REVIEWER_ROLE)) {

			ManuscriptPreloadReply<ReviewerPreloadResponse> reply = new ManuscriptPreloadReply<ReviewerPreloadResponse>();
			reply.setPreloadReply(reviewerPreload());
			return reply;

		} else if (role.equals(Roles.EDITOR_ROLE)) {

			ManuscriptPreloadReply<EditorPreloadResponse> reply = new ManuscriptPreloadReply<EditorPreloadResponse>();
			reply.setPreloadReply(editorPreload());
			return reply;

		} else {
			return null;
		}
	}

	private EditorPreloadResponse editorPreload() {
		// TODO Auto-generated method stub
		return null;
	}

	private ReviewerPreloadResponse reviewerPreload() {
		// TODO Auto-generated method stub
		return null;
	}

	private AuthorPreloadResponse authorPreload() {
		AuthorPreloadResponse authorPreloadResponse = new AuthorPreloadResponse();
		authorPreloadResponse.setSuccessMessage("Author success preload reply.");

		return authorPreloadResponse;
	}

	@Override
	public FileUploadResponse upload(MultipartFile file) {
		FileUploadResponse response = new FileUploadResponse();
		SaveSubmissionRequest saveSubmissionRequest = new SaveSubmissionRequest();
		File fileToSave = null;
		try {
			fileToSave = fileManager.saveFile(file, null);

			saveSubmissionRequest.setSubmissionId(generateSubmissionId(fileToSave.getAbsolutePath()));
			saveSubmissionRequest
					.setAuthorId(((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId());
			saveSubmissionRequest.setFilePath(fileToSave.getAbsolutePath());
			saveSubmissionRequest.setSubmissionStatus(SubmissionStatus.STARTED);
			saveSubmissionRequest.setVersion(1);

			manuscriptDao.saveBasicSubmissionData(saveSubmissionRequest);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			fileManager.deleteFile(fileToSave);
			throw new FileUploadException("File upload was not success. Please try again later.");
		}

		response.setSubmission((manuscriptDao.getSubmissionData(saveSubmissionRequest.getSubmissionId())).getSubmission());
		LOGGER.debug("New submission has created with date: {}", response);
		response.setSuccessMessage("File upload was success");
		return response;
	}

	private String generateSubmissionId(String path) {
		return ((path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."))));
	}

	@Override
	public SaveSubmissionDataResponse save(SaveSubmissionDataRequest submission) {
		checkSubmissionExistence(submission);

		try {
			manuscriptDao.saveSubmissionData(submission);
		} catch (Exception e) {
			LOGGER.debug("Exception occurred durin save submission. Exception {}", e);
			throw new SaveSubmissionException("Can't save submission. Please try again later.");
		}
		SaveSubmissionDataResponse response = new SaveSubmissionDataResponse();

		response.setSuccessMessage("Your modifications have been saved succesfully.");

		// Kell hogy visszaadja az �j, lementett manuscriptet???
		response.setSubmission((manuscriptDao.getSubmissionData(submission.getSubmission().getSubmissionId())).getSubmission());
		return response;
	}

	private void checkSubmissionExistence(SaveSubmissionDataRequest submission) {
		CheckSubmissionExistence checkSubmissionExistence = new CheckSubmissionExistence();
		checkSubmissionExistence.setSubmissionId(submission.getSubmission().getSubmissionId());
		checkSubmissionExistence.setSubmitterId(submission.getSubmission().getSubmitter());

		String path = manuscriptDao.checkSubmissionExistence(checkSubmissionExistence);

		fileManager.checkFileExistenceOnFileSystem(path);
		LOGGER.debug("File is existing on path: ", path);

	}

	@Override
	public RemoveSubmissionResponse remove(RemoveSubmissionRequest request) {
		RemoveSubmissionResponse response = new RemoveSubmissionResponse();

		try {
			String submissionFilePath = manuscriptDao.removeSubmissionData(request);

			if (submissionFilePath != null || !submissionFilePath.isEmpty()) {
				fileManager.deleteFile(new File(submissionFilePath));
			}
		} catch (Exception e) {
			response.setExceptionMessage("Can't remove submission. Please try again later");
		}
		response.setSuccessMessage("Your submission has been deleted succesfully.");
		return response;
	}

	@Override
	public SubmitSubmissionResponse submit(SubmitSubmissionRequest request) {

		SubmitLifecycle lifecycle = new SubmitLifecycle();
		lifecycle.setSubmission(request.getSubmission());
		lifecycle.setNewStatus(SubmissionStatus.SUBMITTED);

		manuscriptLifecycle.lifecycle(lifecycle);

		return null;
	}

	@Override
	public SearchAuthorResponse searchAuthor(SearchAuthorRequest request) {
		SearchAuthorResponse response = new SearchAuthorResponse();

		SearchUser searchUser = new SearchUser();
		searchUser.setEmail(request.getEmail());
		searchUser.setFirstName(request.getFirstName());
		searchUser.setLastName(request.getLastName());
		searchUser.setRole(Roles.AUTHOR_ROLE);
		List<User> users = searchUserService.searchUsers(searchUser);

		if (!users.isEmpty()) {
			for (User user : users) {
				Author author = new Author();
				author.setEmail(user.getEmail());
				author.setFirstName(user.getFirstName());
				author.setLastName(user.getLastName());
				author.setUserId(user.getUserId());
				response.getAuthors().add(author);
			}
		}

		return response;
	}
}
