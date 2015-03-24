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
	public List<String[]> reactionByIntervenant;

	public TalkAnalyser() {
		this.intervenants = new ArrayList<String>();
		this.interventionsByIntervenants = new ArrayList<String[]>();
		this.motsByIntervenantsByIntervention = new ArrayList<String[]>();
		this.reactionByIntervenant = new ArrayList<String[]>();
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
	public void GetData(String html) {
		System.out.println("Récupération des interventions et réactions ...");
		final Document doc = Jsoup.parse(html);
		Elements listParagraphe = doc.select("div.intervention > p");
		final int i = 0;

		/*
		 * SI LES INTERVENTIONS NE final SONT PAS DANS final LES CLASS
		 * "INTERVENTION" (ANNEE<(2012-2013))
		 */
		if (listParagraphe.isEmpty()) {
			listParagraphe = doc.select("p:not(div#somjo p, .sompopup)");
			String rowInterventions[] = new String[2];
			String rowNbWords[] = new String[2];
			System.out.println("ANCIENNNNNNNNNNNNNNNNNNNN");

			for (final Element paragraphe : listParagraphe) {

				if (paragraphe.hasText()) {
					System.out.println("hastext");
					// Récupération de l'intervenant si il y en a un
					if (!paragraphe.select("a").isEmpty()) {
						System.out.println("a");
						this.motsByIntervenantsByIntervention.add(rowNbWords);
						this.interventionsByIntervenants.add(rowInterventions);

						// Nouvelles lignes
						rowInterventions = new String[2];
						rowNbWords = new String[2];
						rowInterventions[0] = rowNbWords[0] = paragraphe
								.select("a").text();
						paragraphe.select("a").remove();

					} else if (!paragraphe.select("b").isEmpty()) {
						System.out.println("b");
						System.out.println("Ajouté :" + rowInterventions[0]
								+ " " + rowInterventions[1]);
						this.motsByIntervenantsByIntervention.add(rowNbWords);
						this.interventionsByIntervenants.add(rowInterventions);
						// Nouvelles lignes
						rowInterventions = new String[2];
						rowNbWords = new String[2];
						rowInterventions[0] = rowNbWords[0] = paragraphe
								.select("b").text();
						paragraphe.select("b").remove();
					}
					// Récupération de l'intervention
					if (paragraphe.hasText()) {

						rowInterventions[1] += " "
								+ paragraphe
										.text()
										.substring(
												paragraphe.text().indexOf(".") + 1)
										.trim();
						rowNbWords[1] = ""
								+ this.countUtilWords(rowInterventions[1]);

					}

				} else {
					System.out.println("vide");

				}
			}
			this.motsByIntervenantsByIntervention.add(rowNbWords);
			this.interventionsByIntervenants.add(rowInterventions);

		} else {
			/*
			 * SI LES INTERVENTIONS SONT DANS LES CLASS "INTERVENTION"
			 * (ANNEE>2012-2013)
			 */
			// Parcours des paragraphes contenant les interventions
			for (final Element paragraphe : listParagraphe) {
				final String rowInterventions[] = new String[2];
				final String rowNbWords[] = new String[2];

				// Si le nom d'un intervenant n'est pas cité avec un lien
				if (!paragraphe.hasAttr("b")) {
					rowNbWords[0] = rowInterventions[0] = paragraphe
							.select("b").text();
					// System.out.println(intervenant);
					paragraphe.select("b").remove();
				} else {
					rowNbWords[0] = rowInterventions[0] = paragraphe.select(
							"a[href^=/tribun/fiches_id/]").text();
					paragraphe.select("a").remove();
				}

				// System.out.println("Intervenant : " + rowNbWords[0]);

				// Traitement des didascalies
				this.computeDidascalies(rowInterventions[0],
						paragraphe.select("i"));
				paragraphe.select("i").remove();

				// Recupération de l'intervention contenu dans ce paragraphe
				if (!paragraphe.text().isEmpty()) {
					rowInterventions[1] = paragraphe.text()
							.substring(paragraphe.text().indexOf(".") + 1)
							.trim();
					rowNbWords[1] = ""
							+ this.countUtilWords(rowInterventions[1]);
					// Ajout des infos dans les listes
					this.motsByIntervenantsByIntervention.add(rowNbWords);
					this.interventionsByIntervenants.add(rowInterventions);
				}

			}
		}
		System.out.println(this.interventionsByIntervenants.size());

	}

	private void computeDidascalies(String intervenant, Elements didascalies) {

		for (final Element reac : didascalies) {
			final String rowReac[] = new String[2];
			rowReac[0] = intervenant;
			if (Pattern.matches("\\(([^\\)]+)\\)", reac.text())) {
				rowReac[1] = reac.text().substring(1, reac.text().length() - 2);
				this.reactionByIntervenant.add(rowReac);
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

		System.out.println("Récupération des informations ...");
		analyser.GetData(textHtml);
		System.out.println("Sauvegarde ...");
		CSVHandler.saveAll(
				analyser.interventionsByIntervenants,
				path
						+ "interventions"
						+ File.separator
						+ file.substring(file.lastIndexOf("/"),
								file.length() - 5) + ".csv");
		CSVHandler.saveAll(
				analyser.reactionByIntervenant,
				path
						+ "reac"
						+ File.separator
						+ file.substring(file.lastIndexOf("/"),
								file.length() - 5) + ".csv");
		CSVHandler.saveAll(
				analyser.motsByIntervenantsByIntervention,
				path
						+ "wordsCount"
						+ File.separator
						+ file.substring(file.lastIndexOf("/"),
								file.length() - 5) + ".csv");

	}
}
