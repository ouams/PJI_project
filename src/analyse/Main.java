package analyse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cleaning.Cleaner;
import csv.CSVHandler;

public class Main {

	static int indentLevel = -1;

	/**
	 * Fonction pour qui récupère l'arboresence d'un repertoire
	 *
	 * @param path
	 *            le chemin vers le repertoire
	 * @param listFiles
	 *            liste qui contiendra toute l'arborescence
	 * @return la liste contenant tout les chemins de l'arborescence
	 */
	static List<String> listPath(File path, List<String> listFiles) {
		File files[];
		indentLevel++;

		files = path.listFiles();

		Arrays.sort(files);
		for (final File file : files) {
			if (file.isDirectory()) {
				listPath(file, listFiles);
			} else if (file.toString().contains(".html")) {
				listFiles.add(file.toString());
			}
		}
		indentLevel--;
		return listFiles;
	}

	/**
	 * Procède à l'analyse et au stockages des informations d'une séance
	 *
	 * @param file
	 *            le chemin du fichier html de la séance à analyser
	 * @throws IOException
	 */
	public static void proccess(String file) throws IOException {
		final Cleaner cleaner = new Cleaner();
		final TalkAnalyser analyser = new TalkAnalyser();

		// On récupère l'encodage
		analyser.setCharset(file);

		// on nettoie le fichier, de manière visuelle
		cleaner.process(file, analyser.getCharset());

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
								file.length() - 5) + ".csv",
						analyser.getCharset());

	}

	/**
	 * Main du projet qui va nettoyer, analyser puis sauvegarder tout les
	 * fichiers de l'arborescence du dossier "cri/"
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		List<String> arborescence = new ArrayList<String>();
		arborescence = listPath(new File("cri/"), arborescence);

		for (int i = 0; i < arborescence.size(); i++) {
			System.out.println("Fichier courrant: " + arborescence.get(i));
			proccess(arborescence.get(i));
			System.out.println("Fichiers restants : "
					+ (arborescence.size() - i));
			System.out.println("Terminé");
		}
	}
}
