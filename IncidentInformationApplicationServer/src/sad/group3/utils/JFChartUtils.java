package sad.group3.utils;

import java.io.File;

import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.servlet.ServletUtilities;

public class JFChartUtils extends ServletUtilities {
	private static String tempFilePrefix = "jfreechart-";
	private static String tempOneTimeFilePrefix = "jfreechart-onetime-";

	public static String saveChartAsPNG(String path, JFreeChart chart,
			int width, int height, ChartRenderingInfo info, HttpSession session)
			throws java.io.IOException {
		try {
			if (chart == null)
				throw new IllegalArgumentException("Null 'chart' argument.");
			createTempDir(path);
			String prefix = tempFilePrefix;
			if (session == null)
				prefix = tempOneTimeFilePrefix;

			File tempFile = File.createTempFile(prefix, ".png", new File(path));
			ChartUtilities.saveChartAsPNG(tempFile, chart, width, height, info);
			if (session != null)
				registerChartForDeletion(tempFile, session);
			return tempFile.getName();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("null!");
			return "";
		}
	}

	protected static void createTempDir(String path) {
		if (path == null)
			throw new RuntimeException(
					"Temporary directory system property (java.io.tmpdir) is null.");
		File tempDir = new File(path);
		if (!tempDir.exists())
			tempDir.mkdirs();
	}
}
