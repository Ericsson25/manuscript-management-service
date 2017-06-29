package manuscript.module.manuscript.management.fileupload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import manuscript.module.manuscript.management.exception.CheckSubmissionExistenceException;
import manuscript.system.security.bean.AuthenticatedUser;

@Service
public class FileManagerImpl implements FileManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(FileManagerImpl.class);

	private static final String PATH_TO_SAVE = "D:\\tmpFiles\\";
	private static final String SPECIAL_CHARACTER = "@";
	private static final String DATE_FORMAT = "yyyyMMdd_HHmm";
	private static final String START_SEQUENCE = "_1";

	private static final Collection<String> ACCEPTABLE_FILE_TYPES = Collections.unmodifiableList(Arrays.asList("json", "zip", "pdf", "doc", "latex"));

	@Override
	public File saveFile(MultipartFile file, String existingFilePath) {

		File fileToSave = new File(makePathAndFileName(file, existingFilePath));

		LOGGER.debug("Saving file... {}", fileToSave);

		try {
			file.transferTo(fileToSave);
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return fileToSave;
	}

	private String makePathAndFileName(MultipartFile file, String existingFilePath) {
		StringBuilder builder = new StringBuilder();

		String fileType = validateFileType(file.getOriginalFilename());

		String userName = ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

		Path filePath = Paths.get(PATH_TO_SAVE + userName + File.separator);

		if (!Files.exists(filePath)) {
			LOGGER.debug("{} is not exist. Make directory...", filePath);
			new File(filePath.toString()).mkdirs();
		}

		builder.append(filePath.toString());
		builder.append(File.separator);
		builder.append(userName);
		builder.append(SPECIAL_CHARACTER);
		builder.append(existingFilePath != null ? dateFromPath(existingFilePath) : generateDate());
		builder.append(existingFilePath != null ? generateNewSequence(existingFilePath) : START_SEQUENCE);
		builder.append(fileType);

		if (Files.exists(Paths.get(builder.toString()))) {
			LOGGER.debug("{} file already exist. Generating new sequence..", builder.toString());
			generateNewSequence(builder.toString());
		}

		return builder.toString();
	}

	/**
	 * A fájl eredeti fájlnevébõl kivágja a fájltípust. Ezután megvizsgálja, hogy az {@link ACCEPTABLE_FILE_TYPES} tartalmazza-e a típust. Ha igen,
	 * visszaadja egyéb esetben Exception.
	 * 
	 * @param originalFilename
	 * @return
	 */
	private String validateFileType(String originalFilename) {
		String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		if (!ACCEPTABLE_FILE_TYPES.contains(type)) {
			throw new RuntimeException("invalid type exception...");
		}
		return "." + type;
	}

	private String dateFromPath(String existingFilePath) {
		return existingFilePath.substring(existingFilePath.lastIndexOf("@") + 1, existingFilePath.lastIndexOf("_"));
	}

	private String generateDate() {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		Date date = new Date();

		return dateFormat.format(date);
	}

	private String generateNewSequence(String path) {
		Integer newSequence = ((Integer.parseInt(path.substring(path.lastIndexOf("_") + 1, path.lastIndexOf(".")))) + 1);
		return "_" + newSequence.toString();
	}

	@Override
	public void deleteFile(File file) {
		if (file.delete()) {
			LOGGER.debug("File from {} path has been deleted", file);
		}
	}

	@Override
	public void checkFileExistenceOnFileSystem(String path) {
		LOGGER.debug("Check file existence on path: ", path);
		if (!Files.exists(Paths.get(path))) {
			throw new CheckSubmissionExistenceException("File is not existing on the file system.");
		}

	}
}
