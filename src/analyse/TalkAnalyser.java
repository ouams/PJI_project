package analyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	public List<String[]> interventionsByIntervenants;
	public List<String[]> motsByIntervenantsByIntervention;

	public TalkAnalyser() {
		this.intervenants = new ArrayList<String>();
		this.interventionsByIntervenants = new ArrayList<String[]>();
		this.motsByIntervenantsByIntervention = new ArrayList<String[]>();
	}

	/**
	 * Lit un fichier
	 *
	 * @param filePath
	 *            le chemin du fichier à lire
	 * @return
	 */
	public String readFile(String filePath) {
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
	public void getInterventionsByIntervenant(String html) {
		System.out.println("Récupération des interventions ...");
		final Document doc = Jsoup.parse(html);
		final Elements divInterv = doc.getElementsByClass("intervention");
		/* AJOUTER DIV "Point" */

		// Parcours toute les div "intervention"
		for (final Element interventionHtml : divInterv) {
			// recupere tout les paragraphes contenant les interventions
			final Elements intervParagraphes = interventionHtml.select("p");

			if (intervParagraphes.hasAttr("a")) {
				intervParagraphes.select("a").first().remove();
			}

			// Parcours les paragraphes
			for (final Element element : intervParagraphes) {
				final String rowInterventions[] = new String[2];
				final String rowNbWords[] = new String[2];

				// Si le nom d'un intervenant n'est pas cité avec un lien
				if (!element.select("a").hasText()) {
					rowNbWords[0] = rowInterventions[0] = element.select("b")
							.text();
					// System.out.println(intervenant);
					element.select("b").remove();
				} else {
					rowNbWords[0] = rowInterventions[0] = element.select(
							"a[href^=/tribun/fiches_id/]").text();
					element.select("a").remove();
				}

				// Suppression des didascalies
				element.select("i").remove();
				element.text();
				// Recupération de l'intervention contenu dans ce paragraphe
				if (!element.text().isEmpty()) {
					rowInterventions[1] = element.text()
							.substring(element.text().indexOf(".") + 1).trim();
					// Calcul du nombre de mots dans l'intervention
					rowNbWords[1] = ""
							+ this.countUtilWords(rowInterventions[1]);
					// Ajout des infos dans les listes
					this.motsByIntervenantsByIntervention.add(rowNbWords);
					this.interventionsByIntervenants.add(rowInterventions);
				}

			}
		}
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
	 * Procède à l'analyse et au stockages des informations d'une séance
	 *
	 * @param file
	 *            le chemin du fichier html de la séance à analyser
	 * @throws IOException
	 */
	public static void proccess(String file) throws IOException {
		final TalkAnalyser analyser = new TalkAnalyser();
		final String textHtml = analyser.readFile(file);
		final String path = "data"
				+ file.substring(file.indexOf("/"), file.lastIndexOf("/"))
				+ "/";

		System.out.println("Récupérartion des informations ...");
		analyser.getInterventionsByIntervenant(textHtml);
		System.out.println("Sauvegarde ...");
		CSVHandler.saveAll(
				analyser.interventionsByIntervenants,
				path
				+ "Interventions"
						+ File.separator
				+ file.substring(file.lastIndexOf("/"),
						file.length() - 5) + ".csv");
		CSVHandler.saveAll(
				analyser.motsByIntervenantsByIntervention,
				path
				+ "WordsCount"
						+ File.separator
				+ file.substring(file.lastIndexOf("/"),
						file.length() - 5) + ".csv");

	}

}
