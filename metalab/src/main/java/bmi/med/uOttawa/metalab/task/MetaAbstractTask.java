package bmi.med.uOttawa.metalab.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaSources;

/**
 * An abstract class of the tasks in MetaLab, which provides the progress bars, and run the next work in the done() method
 * @author MEDTECHU0188
 *
 */
public abstract class MetaAbstractTask extends SwingWorker<Boolean, Object> {

	protected MetaParameter metaPar;
	protected MetaSources msv;
	protected JProgressBar bar1, bar2;
	protected SwingWorker<Boolean, Object> nextWork;
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	protected ConfigurationBuilder<BuiltConfiguration> builder;

	public MetaAbstractTask(MetaParameter metaPar, MetaSources msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		this.metaPar = metaPar;
		this.msv = msv;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.nextWork = nextWork;

		addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				if ("progress".equals(evt.getPropertyName())) {
					bar1.setValue((Integer) evt.getNewValue());
				}
			}
		});
		initial();
	}
	
	protected abstract void initial();
	
	public void setLogBuilder(ConfigurationBuilder<BuiltConfiguration> builder) {
		this.builder = builder;
	}

	protected abstract String getTaskName();

	protected abstract Logger getLogger();

	protected void done() {

		Logger LOGGER = getLogger();
		String taskName = getTaskName();

		boolean finished = false;
		try {
			finished = get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": task failed");
		}

		if (finished) {
			LOGGER.info(taskName + ": finished");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": task finished");

			if (nextWork != null) {
				nextWork.execute();
			} else {
				setProgress(100);
			}
		} else {
			LOGGER.info(taskName + ": failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": task failed");
		}
	}
}
