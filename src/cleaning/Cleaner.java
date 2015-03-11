package cleaning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XmlSerializer;

public class Cleaner {

	public Cleaner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Procede au netoyage du html brut
	 *
	 * @param filepath
	 *            chemin vers le fichier html
	 * @return une chaine de caractere contenant le html propre
	 */
	public String clean(String filepath) {
		System.out.println("Netoyage du fichier ...");
		final HtmlCleaner clean = new HtmlCleaner();
		clean.getProperties();
		TagNode node = null;
		try {
			node = clean.clean(new File(filepath));
		} catch (final IOException e) {
			System.out
			.println("Erreur : une erreur est survenue lors du nettoyage du fichier");
		}
		clean.getInnerHtml(node);

		// set up properties for the serializer (optional, see online docs)
		final CleanerProperties cleanerProperties = clean.getProperties();
		cleanerProperties.setOmitXmlDeclaration(true);

		// use the getAsString method on an XmlSerializer class
		final XmlSerializer xmlSerializer = new PrettyXmlSerializer(
				cleanerProperties);
		final String cleanHtml = xmlSerializer.getAsString(node);
		return cleanHtml;
	}

	/**
	 * Sauvegarde le nouveau html propre au même endroit
	 * 
	 * @param filepath
	 *            le chemin vers le fichier html
	 * @param content
	 *            le nouveau contenu html du fichier
	 */
	public void save(String filepath, String content) {
		System.out.println("Sauvegarde du fichier ...");
		PrintWriter writer;
		try {
			writer = new PrintWriter(filepath, "UTF-8");
			writer.print(content);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void process(String file) {
		final String cleanfile = this.clean(file);
		this.save(file, cleanfile);
	}

}
