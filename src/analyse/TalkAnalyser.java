package analyse;

import java.io.BufferedReader;
import java.io.File;
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
import cleaning.Cleaner;
import csv.CSVHandler;

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
	private String president = "";
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

		// Récupération du nom du président
		final Elements presidents = doc.select("p.sompresidence");
		for (final Element president : presidents) {
			this.president += president.text().substring(14) + " ";
		}

		final String president[] = { "Présidence de " + this.president };
		this.dataSeance.add(president);
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

	private void processDivIntervention(final Elements listParagraphe) {
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
				// Ajout de la date puis des infos dans les listes
				rowData[4] = this.date;
				this.dataSeance.add(rowData);
			}
		}
	}

	private void processParagrapheOnly(Document doc) {
		final Elements listParagraphe = doc
				.select("p:not(div#somjo p, .sompopup)");
		String rowData[] = { "", "", "", "", "" };

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
						rowData[4] = this.date;
						this.dataSeance.add(rowData);
					}

					// Nouvelles lignes
					rowData = new String[5];
					rowData[1] = "";
					rowData[0] = paragraphe.select("a").text();
					paragraphe.select("a").remove();

				} else if (!paragraphe.select("b").isEmpty()) {
					if (rowData[0].startsWith("Mr.")
							|| rowData[0].startsWith("Mme")) {
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
		final String reactions = this.readFile("Reac.txt");
		this.reacs = new ArrayList<String>(Arrays.asList(reactions.split(" ")));
	}

	/**
	 * Procède à l'analyse et au stockages des informations d'une séance
	 *
	 * @param file
	 *            le chemin du fichier html de la séance à analyser
	 * @throws IOException
	 */
	public void proccess(String file) throws IOException {
		final Cleaner cleaner = new Cleaner();
		final TalkAnalyser analyser = new TalkAnalyser();

		// On récupère l'encodage
		charset = file.split("_")[1];
		charset = charset.split("\\.")[0];

		// on nettoie le fichier, de manière visuelle
		cleaner.process(file, charset);

		// On lit le fichier nettoyé
		final String textHtml = analyser.readFile(file);
		// On lit le fichier qui va permettre de gerer les réactions
		analyser.readReac();

		// Création du chemin pour l'enregistrement des informations récupérées
		final String path = "data"
				+ file.substring(file.indexOf("/"), file.lastIndexOf("/"))
				+ "/";

		System.out.println("Récupération des informations ...");

		// Lancement de l'analyse des données
		analyser.GetData(textHtml);

		System.out.println("Sauvegarde ...");

		// Sauvegarde des données récupérées
		CSVHandler.saveAll(
				analyser.dataSeance,
				path
						+ "infoSeance"
						+ File.separator
						+ file.substring(file.lastIndexOf("/"),
								file.length() - 5) + ".csv", charset);

	}
}
