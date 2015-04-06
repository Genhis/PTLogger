package sk.genhis.ptlogger;

import sk.genhis.glib.scheduler.GTask;

public final class UpdateDateTask implements GTask {
	public void run() {
		PTLogger.updateDate();
	}

	public String getName() {
		return getClass().getName();
	}
}