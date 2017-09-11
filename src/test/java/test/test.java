package test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import manuscript.module.manuscript.management.bean.SubmissionStatus;
import manuscript.module.user.management.bean.User;

public class test {

	private String path = "Jonsnow@20170625_1137_1.json";

	private static final Map<SubmissionStatus, List<SubmissionStatus>> stateHolder = new HashMap<SubmissionStatus, List<SubmissionStatus>>();

	static {
		stateHolder.put(SubmissionStatus.STARTED, Arrays.asList(SubmissionStatus.SUBMITTED));
		stateHolder.put(SubmissionStatus.SUBMITTED, Arrays.asList(SubmissionStatus.ASSIGN_TO_REVIEWER, SubmissionStatus.DELETED));
	}

	@Test
	public void test() {
		String ta = path.substring(path.lastIndexOf("_") + 1, path.lastIndexOf(".")) + 1;
		System.out.println("newSequence: " + ta);
		System.out.println(ta.toString());

	}

	@Test
	public void test2() {
		if (stateHolder.get(SubmissionStatus.SUBMITTED).contains(SubmissionStatus.ASSIGN_TO_REVIEWER)) {
			System.out.println("lol");
		} else {
			System.out.println("nopee");
		}
		// for (SubmissionStatus item : stateHolder.get(SubmissionStatus.STARTED)) {
		// if (it) {
		//
		// }
		// }
	}

	@Test
	public void test3() {
		User user = new User();
		// user.setTitle("dr");
		user.setFirstName("firstname");
		user.setLastName("lastname");

		System.out.println(buildFullName(user));
	}

	private String buildFullName(User user) {
		StringBuilder builder = new StringBuilder();
		builder.append(user.getTitle() != null ? user.getTitle() : "");
		builder.append(user.getTitle() != null ? " " : "");
		builder.append(user.getFirstName() != null ? user.getFirstName() : "");
		builder.append(user.getFirstName() != null ? " " : "");
		builder.append(user.getLastName() != null ? user.getLastName() : "");
		return builder.toString();
	}

}
