package analyse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	 * Main du projet qui va nettoyer, analyser puis sauvegarder tout les
	 * fichiers de l'arborescence du dossier "cri/"
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final TalkAnalyser analyser = new TalkAnalyser();
		List<String> arborescence = new ArrayList<String>();
		arborescence = listPath(new File("cri/"), arborescence);

		for (int i = 0; i < arborescence.size(); i++) {
			System.out.println("Fichier courrant: " + arborescence.get(i));
			analyser.proccess(arborescence.get(i));
			System.out.println("Fichiers restants : "
					+ (arborescence.size() - i));
			System.out.println("Terminé");
		}
	}
}
