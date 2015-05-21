package cleaning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
	 * @param charset
	 * @return une chaine de caractere contenant le html propre
	 */
	public String clean(String filepath, String charset) {
		System.out.println("Netoyage du fichier ...");
		final HtmlCleaner clean = new HtmlCleaner();
		clean.getProperties();
		TagNode node = null;
		try {
			node = clean.clean(new File(filepath), charset);
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
	public void save(String filepath, String content, String charset) {
		System.out.println("Sauvegarde du fichier ...");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filepath), charset));
			writer.write(content);
			writer.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Methode qui va nettoyer et sauvegarder le fichier passé en parametre
	 * 
	 * @param file
	 *            le fichier à nettoyer
	 * @param charset
	 *            l'encodage du fichier
	 */
	public void process(String file, String charset) {
		final String cleanfile = this.clean(file, charset);
		this.save(file, cleanfile, charset);
	}

}
