package csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
	 * Enregistre des données dans un csv
	 *
	 * @param rows
	 *            le csv à enregistrer
	 * @param path
	 *            le chemin de l'endroit où enregistrer les données
	 * @param charset
	 *            l'encodage des données à enregistrer
	 */
	public static void saveAll(List<String[]> rows, String path, String charset)
			throws IOException {
		final CSVWriter writer;
		final File parentDir = new File(path).getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		writer = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(path), charset), ';', '"', '\\');
		writer.writeAll(rows, false);
		writer.close();

	}

	/**
	 * Enregistre une ligne dans un csv
	 *
	 * @param rows
	 *            le csv à enregistrer
	 * @param path
	 *            le chemin de l'endroit où enregistrer les données
	 * @param charset
	 *            l'encodage des données à enregistrer
	 */
	public static void save(String[] row, String path, String charset) {
		try {
			final CSVWriter writer = new CSVWriter(new OutputStreamWriter(
					new FileOutputStream(path), charset), ';', '"', '\\');
			writer.writeNext(row, false);
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Lit un csv
	 * 
	 * @param path
	 *            le chemin du csv à lire
	 * @return le csv lut sous forme de tableau de string
	 */
	public static List<String[]> read(String path, char delim, String charset) {

		// Build reader instance
		try {
			final CSVReader reader = new CSVReader(new InputStreamReader(
					new FileInputStream(path), charset), delim, '"', 1);
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
