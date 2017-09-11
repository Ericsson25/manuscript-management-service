package manuscript.module.manuscript.management.lifecycle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import manuscript.module.manuscript.management.bean.ManuscriptLifecycleRequest;
import manuscript.module.manuscript.management.bean.SubmissionStatus;
import manuscript.module.manuscript.management.bean.SubmitLifecycle;

@Service
public class ManuscriptLifecycleImpl<T extends ManuscriptLifecycleRequest> implements ManuscriptLifecycle<T> {

	private static final Map<SubmissionStatus, List<SubmissionStatus>> stateHolder = new HashMap<SubmissionStatus, List<SubmissionStatus>>();

	static {
		stateHolder.put(SubmissionStatus.STARTED, Arrays.asList(SubmissionStatus.SUBMITTED));
		stateHolder.put(SubmissionStatus.SUBMITTED, Arrays.asList(SubmissionStatus.ASSIGN_TO_REVIEWER, SubmissionStatus.DELETED));
		stateHolder.put(SubmissionStatus.ASSIGN_TO_REVIEWER, Arrays.asList(SubmissionStatus.UNDER_REVIEW));
		stateHolder.put(SubmissionStatus.UNDER_REVIEW, Arrays.asList(SubmissionStatus.UNDER_VERDICT));
		stateHolder.put(SubmissionStatus.UNDER_VERDICT,
				Arrays.asList(SubmissionStatus.NEED_PRECISION, SubmissionStatus.ACCEPTED, SubmissionStatus.REJECTED));
		stateHolder.put(SubmissionStatus.NEED_PRECISION, Arrays.asList(SubmissionStatus.STARTED));
		stateHolder.put(SubmissionStatus.ACCEPTED, Arrays.asList());
		stateHolder.put(SubmissionStatus.REJECTED, Arrays.asList(SubmissionStatus.DELETED));
		stateHolder.put(SubmissionStatus.DELETED, Arrays.asList());
	}

	@Override
	public void lifecycle(T t) {
		SubmissionStatus oldStatus = t.getSubmission().getStatus();
		SubmissionStatus newStatus = t.getNewStatus();

		if (stateHolder.get(oldStatus).contains(newStatus)) {
			changeStatus(t);
		} else {
			// TODO THROW EXCEPTION
		}
	}

	private void changeStatus(T t) {
		SubmissionStatus newStatus = t.getNewStatus();

		if (newStatus == SubmissionStatus.STARTED) {

		} else if (newStatus == SubmissionStatus.SUBMITTED) {
			if (t instanceof SubmitLifecycle) {

			}
		} else if (newStatus == SubmissionStatus.ASSIGN_TO_REVIEWER) {

		} else if (newStatus == SubmissionStatus.UNDER_REVIEW) {

		} else if (newStatus == SubmissionStatus.UNDER_VERDICT) {

		} else if (newStatus == SubmissionStatus.NEED_PRECISION) {

		} else if (newStatus == SubmissionStatus.ACCEPTED) {

		} else if (newStatus == SubmissionStatus.REJECTED) {

		} else {
			// DELETED
		}
	}
}
