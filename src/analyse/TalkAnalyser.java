package analyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import csv.CSVHandler;

/**
 * Classe qui va analyser les différentes prises de paroles et classer les
 * intervention par intervenant dans des dossiers
 *
 * @author sais
 */
public class TalkAnalyser {
	public List<String> intervenants;
	public List<String[]> dataSeance;
	public List<String> reacs;
	public String president = "";

	public TalkAnalyser() {
		this.intervenants = new ArrayList<String>();
		this.dataSeance = new ArrayList<String[]>();

	}

	/**
	 * Lit un fichier
	 *
	 * @param filePath
	 *            le chemin du fichier à lire
	 * @return
	 */
	public static String readFile(String filePath) {
		try {
			final BufferedReader br = new BufferedReader(new FileReader(
					filePath));
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

	/**
	 * Recupere la liste des intervenant de la seance
	 *
	 * @param html
	 *            le contenu html de la séance
	 */
	public void setIntervenantsList(String html) {
		final Document doc = Jsoup.parse(html);
		final Elements intervenants = doc.select("a[href^=/tribun/fiches_id/]");

		for (final Element intervenant : intervenants) {
			if (!this.intervenants.contains(intervenant.text())) {
				this.intervenants.add(intervenant.text());
			}
		}
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
		final Elements listParagraphe = doc.select("div.intervention > p");
		final String date[] = { doc.select("title").text()
				.substring(doc.select("title").text().indexOf("du") + 3) };
		this.dataSeance.add(date);

		// Récupération du nom du président
		final Elements presidents = doc.select("p.sompresidence");
		for (final Element president : presidents) {
			this.president += president.text().substring(14) + " ";
		}

		/*
		 * SI LES INTERVENTIONS NE SONT PAS DANS final LES CLASS "INTERVENTION"
		 * (ANNEE<(2012-2013))
		 */
		if (!listParagraphe.isEmpty()) {

			this.processDivIntervention(listParagraphe);

		} else {
			this.processParagrapheOnly(doc);
		}
	}

	private void processOrateur(Document doc) {
		final Elements listParagraphe = doc
				.select("p:not(div#somjo p, .sompopup)");
		String rowData[] = { "", "", "", "" };

		for (final Element paragraphe : listParagraphe) {
			System.out.println(paragraphe.hasAttr("orateur"));
			if (paragraphe.hasAttr("orateur")) {
				this.dataSeance.add(rowData);

				// Nouvelles lignes
				rowData = new String[4];
				rowData[0] = paragraphe.select("orateur").text();
				System.out.println(rowData[0]);
			}

			this.computeDidascalies(rowData, paragraphe.select("i"));

			// Recupération de l'intervention contenu dans ce paragraphe
			if (!paragraphe.text().isEmpty()) {
				rowData[1] = ""
						+ paragraphe.text()
								.substring(paragraphe.text().indexOf(".") + 1)
								.trim();
				rowData[2] = "" + this.countUtilWords(rowData[1]);

			}

		}

	}

	private void processDivIntervention(final Elements listParagraphe) {

		// Parcours des paragraphes contenant les interventions
		for (final Element paragraphe : listParagraphe) {
			final String rowData[] = new String[4];

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

			if (rowData[0].contains("président")
					|| rowData[0].contains("présidente")) {
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
				// Ajout des infos dans les listes
				this.dataSeance.add(rowData);
			}
		}

		System.out.println(this.dataSeance.size());
	}

	private void processParagrapheOnly(Document doc) {
		final Elements listParagraphe = doc
				.select("p:not(div#somjo p, .sompopup)");
		String rowData[] = { "", "", "", "" };

		for (final Element paragraphe : listParagraphe) {

			if (paragraphe.text().toLowerCase().contains("présidence")
					&& this.president.equals("")) {
				this.president = paragraphe
						.text()
						.substring(
								paragraphe.text().indexOf("présidence de") + 14)
								.trim();
			}

			if (paragraphe.hasText()) {
				// Récupération de l'intervenant si il y en a un
				if (!paragraphe.select("a").isEmpty()
						&& !paragraphe.select("a").hasAttr("name")) {
					// On ajoute que si la ligne contient des informations
					if (rowData[0].startsWith("M.")
							|| rowData[0].startsWith("Mme")) {
						this.dataSeance.add(rowData);
					}

					// Nouvelles lignes
					rowData = new String[4];
					rowData[0] = paragraphe.select("a").text();
					paragraphe.select("a").remove();

				} else if (!paragraphe.select("b").isEmpty()) {
					if (rowData[0].startsWith("Mr.")
							|| rowData[0].startsWith("Mme")) {
						this.dataSeance.add(rowData);
					}
					// Nouvelles lignes
					rowData = new String[4];
					rowData[0] = paragraphe.select("b").text();
					paragraphe.select("b").remove();

				}

				if (rowData[0].contains("président")
						|| rowData[0].contains("présidente")) {
					rowData[0] = this.president;

				}
				// Récupération de l'intervention
				if (paragraphe.hasText()) {
					// On gere les didascalies
					this.computeDidascalies(rowData, paragraphe.select("i"));
					// paragraphe.select("i").remove();

					// Le reste de l'intervention
					// System.out.println(paragraphe.text());

					if (paragraphe.text().trim().startsWith(".")) {
						rowData[1] += paragraphe.text().substring(1).trim();
					} else {
						rowData[1] += " " + paragraphe.text();
					}

					rowData[2] = "" + this.countUtilWords(rowData[1]);

				}

			}
		}
		this.dataSeance.add(rowData);
	}

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

	private void readReac() {
		final String reactions = readFile("Reac.txt");
		this.reacs = new ArrayList<String>(Arrays.asList(reactions.split(" ")));
	}

	/**
	 * Procède à l'analyse et au stockages des informations d'une séance
	 *
	 * @param file
	 *            le chemin du fichier html de la séance à analyser
	 * @throws IOException
	 */
	public static void proccess(String file) throws IOException {
		final TalkAnalyser analyser = new TalkAnalyser();
		final String textHtml = analyser.readFile(file);
		analyser.readReac();
		final String path = "data"
				+ file.substring(file.indexOf("/"), file.lastIndexOf("/"))
				+ "/";

		System.out.println("Récupération des informations ...");
		analyser.GetData(textHtml);
		System.out.println("Sauvegarde ...");
		CSVHandler.saveAll(
				analyser.dataSeance,
				path
						+ "infoSeance"
						+ File.separator
						+ file.substring(file.lastIndexOf("/"),
								file.length() - 5) + ".csv");

	}
}
