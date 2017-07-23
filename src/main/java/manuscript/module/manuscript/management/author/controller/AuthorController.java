package manuscript.module.manuscript.management.author.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import manuscript.module.manuscript.management.ManuscriptService;
import manuscript.module.manuscript.management.exception.FileValidationException;
import manuscript.module.manuscript.management.preload.reply.ManuscriptPreloadReply;
import manuscript.module.manuscript.management.request.RemoveSubmissionRequest;
import manuscript.module.manuscript.management.request.SaveSubmissionDataRequest;
import manuscript.module.manuscript.management.request.SearchAuthorRequest;
import manuscript.module.manuscript.management.request.SubmitSubmissionRequest;
import manuscript.module.manuscript.management.response.AuthorPreloadResponse;
import manuscript.module.manuscript.management.response.FileUploadResponse;
import manuscript.module.manuscript.management.response.RemoveSubmissionResponse;
import manuscript.module.manuscript.management.response.SaveSubmissionDataResponse;
import manuscript.module.manuscript.management.response.SearchAuthorResponse;
import manuscript.module.manuscript.management.response.SubmitSubmissionResponse;
import manuscript.module.user.management.bean.Roles;

@RestController
@RequestMapping("/author")
public class AuthorController {

	private ManuscriptService manuscriptService;

	public AuthorController(ManuscriptService manuscriptService) {
		this.manuscriptService = manuscriptService;
	}

	@RequestMapping("/submission/preload")
	public AuthorPreloadResponse preload() {
		ManuscriptPreloadReply<?> response = manuscriptService.preload(Roles.AUTHOR_ROLE);

		if (response.getPreloadReply() instanceof AuthorPreloadResponse) {
			return (AuthorPreloadResponse) response.getPreloadReply();
		}

		return null;
	}

	@RequestMapping("/submission/upload")
	public FileUploadResponse upload(@RequestParam("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new FileValidationException("File must not be null");
		}
		return manuscriptService.upload(file);
	}

	@RequestMapping("/submission/save")
	public SaveSubmissionDataResponse save(@RequestBody SaveSubmissionDataRequest submission) {
		return manuscriptService.save(submission);
	}

	@RequestMapping("/submission/remove")
	public RemoveSubmissionResponse save(@RequestBody RemoveSubmissionRequest request) {
		return manuscriptService.remove(request);
	}

	@RequestMapping("/submission/searchauthor")
	public SearchAuthorResponse searchAuthor(SearchAuthorRequest request) {
		return manuscriptService.searchAuthor(request);
	}

	@RequestMapping("/submission/submit")
	public SubmitSubmissionResponse submit(@RequestBody SubmitSubmissionRequest request) {
		return manuscriptService.submit(request);
	}

}
