package analyse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.DateHandler;

/**
 * Classe qui va analyser les différentes prises de paroles et classer les
 * intervention par intervenant dans des dossiers
 *
 * @author sais
 */
public class TalkAnalyser {
	/**
	 * Liste qui contient chaque intervention de la séance
	 */
	public List<String[]> dataSeance;
	/**
	 * Liste qui contient les mot clés pour les réactions
	 */
	public List<String> reacs;
	/**
	 * String qui contient le président de la séance
	 */
	private String president;
	/**
	 * String qui contient la date de la séance
	 */
	private String date;
	/**
	 * Encodage du fichier analysé
	 */
	static String charset;

	/**
	 * Création d'un nouvel objet d'Analyse
	 */
	public TalkAnalyser() {
		this.dataSeance = new ArrayList<String[]>();
		this.president = "";
	}

	/**
	 * Lit un fichier
	 *
	 * @param filePath
	 *            le chemin du fichier à lire
	 * @return une chaine de caractere contenant tout le contenu du fichier
	 */
	public String readFile(String filePath) {
		try {

			final BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath), charset));
			final StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			final String allText = sb.toString();

			br.close();
			return allText;

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setCharset(String file) {
		charset = file.split("_")[1];
		charset = charset.split("\\.")[0];
	}

	public String getCharset() {
		return charset;
	}

	/**
	 * Analyse le contenu html d'une session
	 *
	 * @param html
	 *            le texte html de la session à analyser
	 */
	public void GetData(String html) {

		System.out.println("Récupération des interventions et réactions ...");
		final Document doc = Jsoup.parse(html);
		final Elements listParagraphe = doc
				.select("div.Point > p, div.intervention > p");
		this.date = DateHandler.dateConverter(doc.select("title").text()
				.substring(doc.select("title").text().indexOf("du") + 3));

		/*
		 * SI LES INTERVENTIONS NE SONT PAS DANS final LES CLASS "INTERVENTION"
		 * (ANNEE<(2012-2013))
		 */
		if (!listParagraphe.isEmpty()) {
			this.processDivIntervention(doc, listParagraphe);
		} else {
			this.processParagrapheOnly(doc);
		}
	}

	/**
	 * Méthode qui se charge de la récupération des données pour les fichier les
	 * plus récents
	 *
	 * @param listParagraphe
	 *            la liste des paragraphes récupérés
	 */
	private void processDivIntervention(Document doc,
			final Elements listParagraphe) {
		// Récupération du nom du président
		final Elements presidents = doc.select("p.sompresidence");
		for (final Element president : presidents) {
			this.president += " "
					+ president.text().substring(14)
							.replaceAll("[:punct:]", "");
		}

		final String president[] = { "Présidence de" + this.president };
		this.dataSeance.add(president);

		// Parcours des paragraphes contenant les interventions
		for (final Element paragraphe : listParagraphe) {
			final String rowData[] = new String[5];

			// Si le nom d'un intervenant n'est pas cité avec un lien
			if (!paragraphe.hasAttr("b")) {
				rowData[0] = paragraphe.select("b").text();
				// System.out.println(intervenant);
				paragraphe.select("b").remove();
			} else {
				rowData[0] = paragraphe.select("a[href^=/tribun/fiches_id/]")
						.text();
				paragraphe.select("a").remove();
			}
			// Sil'intervenant récupéré est le président/présidente
			if (rowData[0].toLowerCase().contains("président")
					|| rowData[0].toLowerCase().contains("présidente")) {
				rowData[0] = this.president;
			}

			// Traitement des didascalies
			this.computeDidascalies(rowData, paragraphe.select("i"));
			paragraphe.select("i").remove();

			// Recupération de l'intervention contenu dans ce paragraphe
			if (!paragraphe.text().isEmpty()) {
				rowData[1] = ""
						+ paragraphe.text()
								.substring(paragraphe.text().indexOf(".") + 1)
								.trim();
				rowData[2] = "" + this.countUtilWords(rowData[1]);
				// Ajout de la date puis des infos dans les listes
				rowData[4] = this.date;
				this.dataSeance.add(rowData);
			}
		}
	}

	/**
	 * Méthode qui se charge de récuperer des données sur les fichiers les plus
	 * anciens
	 *
	 * @param doc
	 *            Le document à analyser
	 */
	private void processParagrapheOnly(Document doc) {
		final Elements listParagraphe = doc
				.select("p:not(.sommaigre, .sommaigreliste)");
		String rowData[] = { "", "", "", "", "" };
		final Elements presidence = doc
				.select("h5.presidence, p.sompresidence");
		// Récupération du président
		for (final Element element : presidence) {
			if (element.text().toUpperCase().contains("PRÉSIDENCE DE")) {
				if (!this.president
						.equals(" "
								+ element
								.text()
								.substring(
										element.text().indexOf(
												"PRÉSIDENCE DE") + 14)
												.trim().replaceAll(",", ""))) {

					this.president += " "
							+ element
							.text()
							.substring(
									element.text().indexOf(
											"PRÉSIDENCE DE") + 14)
											.trim().replaceAll(",", "");
				}
			}
		}

		final String president[] = { "Présidence de" + this.president };
		this.dataSeance.add(president);

		for (final Element paragraphe : listParagraphe) {

			if (paragraphe.hasText()) {
				// Récupération de l'intervenant si il y en a un
				if (!paragraphe.select("a").isEmpty()
						&& !paragraphe.select("a").hasAttr("name")) {
					// On ajoute que si la ligne contient des informations
					if (rowData[0].startsWith("M.")
							|| rowData[0].startsWith("Mme")) {
						rowData[4] = this.date;
						this.dataSeance.add(rowData);
					}

					// Nouvelles lignes
					rowData = new String[5];
					rowData[1] = "";
					rowData[0] = paragraphe.select("a").text();
					paragraphe.select("a").remove();

				} else if (!paragraphe.select("b").isEmpty()) {
					if (rowData[0].startsWith("M.")
							|| rowData[0].startsWith("Mme")
							|| rowData[0].startsWith(" Mme")
							|| rowData[0].startsWith("M.")) {
						rowData[4] = this.date;
						this.dataSeance.add(rowData);
					}
					// Nouvelles lignes
					rowData = new String[5];
					rowData[1] = "";
					rowData[0] = paragraphe.select("b").text();
					paragraphe.select("b").remove();

				}

				if (rowData[0].contains("président")
						|| rowData[0].contains("présidente")) {
					rowData[0] = this.president.trim();
				}
				// Récupération de l'intervention
				if (paragraphe.hasText()) {
					// On gere les didascalies
					this.computeDidascalies(rowData, paragraphe.select("i"));
					// paragraphe.select("i").remove();

					// Le reste de l'intervention
					// System.out.println(paragraphe.text());

					if (paragraphe.text().trim().startsWith(".")) {
						rowData[1] += ""
								+ paragraphe.text().substring(1).trim();
					} else {
						rowData[1] += "" + paragraphe.text();
					}
					rowData[2] = "" + this.countUtilWords(rowData[1]);

				}

			}
		}
		rowData[4] = this.date;
		this.dataSeance.add(rowData);
	}

	/**
	 * Méthode qui se charge de gerer les didascalies en récupérant celles qui
	 * concernent une réaction uniquement
	 *
	 * @param rowData
	 *            le tableau qui contient les données récupérée
	 * @param didascalies
	 *            la didascalie à analyser
	 */
	private void computeDidascalies(String[] rowData, Elements didascalies) {

		// Pour chaque didascalie
		for (final Element reac : didascalies) {
			// On regarde si elle contient un des mots référence des réactions
			for (final String element : this.reacs) {
				if (reac.text().toLowerCase().contains(element)) {
					rowData[3] = reac.text().substring(1,
							reac.text().length() - 1);
				}
			}
		}
		didascalies.remove();
	}

	/**
	 * Compte le nombre de mots utiles d'une intervention
	 *
	 * @param talks
	 *            la chaine de charactere de l'intervention
	 * @return le nombre de mots de l'intervention
	 */
	private int countUtilWords(String talks) {
		int nbwords = 0;
		final String[] words = talks.split("\\s");

		for (int i = 0; i < words.length; i++) {
			if (!Pattern.matches("[:punct:]", words[i])) {
				nbwords++;
			}
		}
		return nbwords;
	}

	/**
	 * Méthode qui lit et stocke le fichier contenant les mots clés des
	 * réactions
	 */
	protected void readReac() {
		final String reactions = this.readFile("Reac.txt");
		this.reacs = new ArrayList<String>(Arrays.asList(reactions.split(" ")));
	}

}
