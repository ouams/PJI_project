package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Classe qui permet de gerer les csv
 *
 * @author sais
 *
 */
public class CSVHandler {
	public CSVHandler() {

	}

	/**
	 * @param rows
	 * @param path
	 */
	public static void saveAll(List<String[]> rows, String path)
			throws IOException {
		final CSVWriter writer;
		final File parentDir = new File(path).getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		writer = new CSVWriter(new FileWriter(path), ';', '"', '\\');
		writer.writeAll(rows, false);
		writer.close();

	}

	public static void save(String[] row, String path) {
		try {
			final CSVWriter writer = new CSVWriter(new FileWriter(path), ';',
					'"', '\\');
			writer.writeNext(row, false);
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param path
	 * @return
	 */
	public static List<String[]> read(String path) {

		// Build reader instance
		try {
			final CSVReader reader = new CSVReader(new FileReader(path), ';',
					'"', 1);
			final List<String[]> content = reader.readAll();
			reader.close();
			return content;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

}
