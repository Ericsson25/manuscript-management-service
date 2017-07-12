package manuscript.module.manuscript.management;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import manuscript.module.manuscript.management.bean.CheckSubmissionExistence;
import manuscript.module.manuscript.management.bean.Role;
import manuscript.module.manuscript.management.bean.SubmissionStatus;
import manuscript.module.manuscript.management.exception.FileUploadException;
import manuscript.module.manuscript.management.exception.SaveSubmissionException;
import manuscript.module.manuscript.management.fileupload.FileManager;
import manuscript.module.manuscript.management.preload.reply.ManuscriptPreloadReply;
import manuscript.module.manuscript.management.request.RemoveSubmissionRequest;
import manuscript.module.manuscript.management.request.SaveSubmissionDataRequest;
import manuscript.module.manuscript.management.request.SaveSubmissionRequest;
import manuscript.module.manuscript.management.response.AuthorPreloadResponse;
import manuscript.module.manuscript.management.response.EditorPreloadResponse;
import manuscript.module.manuscript.management.response.FileUploadResponse;
import manuscript.module.manuscript.management.response.RemoveSubmissionResponse;
import manuscript.module.manuscript.management.response.ReviewerPreloadResponse;
import manuscript.module.manuscript.management.response.SaveSubmissionDataResponse;
import manuscript.system.security.bean.AuthenticatedUser;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

	private final static Logger LOGGER = LoggerFactory.getLogger(ManuscriptServiceImpl.class);

	private FileManager fileManager;
	private ManuscriptDao manuscriptDao;

	public ManuscriptServiceImpl(FileManager fileManager, ManuscriptDao manuscriptDao) {
		this.fileManager = fileManager;
		this.manuscriptDao = manuscriptDao;
	}

	@Override
	public ManuscriptPreloadReply<?> preload(Role role) {
		if (role.equals(Role.AUTHOR_ROLE)) {

			ManuscriptPreloadReply<AuthorPreloadResponse> reply = new ManuscriptPreloadReply<AuthorPreloadResponse>();
			reply.setPreloadReply(authorPreload());
			return reply;

		} else if (role.equals(Role.REVIEWER_ROLE)) {

			ManuscriptPreloadReply<ReviewerPreloadResponse> reply = new ManuscriptPreloadReply<ReviewerPreloadResponse>();
			reply.setPreloadReply(reviewerPreload());
			return reply;

		} else if (role.equals(Role.EDITOR_ROLE)) {

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

		// Kell hogy visszaadja az új, lementett manuscriptet???
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

}
