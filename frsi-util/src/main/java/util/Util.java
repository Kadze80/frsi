package util;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	// May be used in addition to java.util.logging, but currently log4j is used instead of JUL
	public static FileHandler getFileHandler(String path, Class c, int limit, int count) {
		FileHandler fileHandler = null;
		try {
			fileHandler = new FileHandler(path + "/" + c.getSimpleName() + ".%g.%u.log", limit, count, true);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileHandler;
	}

	public static String getTextFromResource(ClassLoader classLoader, String fileName) {
		StringBuilder sb = new StringBuilder("");
		File file = new File(classLoader.getResource(fileName).getFile());
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) scanner.close();
		}
		return sb.toString();
	}

	public static Date getFirstDayOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Integer getIntFromStr(String str) {
		Integer result = null;
		Pattern pattern = Pattern.compile("[-]?[0-9]+(.[0-9]+)?");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result = Integer.parseInt(matcher.group());
		}
		return result;
	}

	public static String getFileExtension(String fileName) {
		String result = "";
		int i = fileName.lastIndexOf('.');
		if (i >= 0) result = fileName.substring(i + 1);
		return result;
	}

	public static boolean checkPeriod(String periodTypeCode, Date date) {
		DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
		LocalDate ld = new LocalDate(date, dtZone);
		int day = ld.getDayOfMonth();
		int month = ld.getMonthOfYear();

		List<Integer> quarters = Arrays.asList(new Integer[]{1, 4, 7, 10});
		List<Integer> halfYears = Arrays.asList(new Integer[]{1, 7});

		boolean result;
		switch (periodTypeCode.charAt(0)) {
			case 'm':
				result = day == 1;
				break;
			case 'q':
				result = day == 1 && quarters.contains(month);
				break;
			case 'h':
				result = day == 1 && halfYears.contains(month);
				break;
			case 'y':
				result = day == 1 && month == 1;
				break;
			case 'd':
				result = true;
				break;
			default:
				result = false;
		}

		return result;
	}
}
