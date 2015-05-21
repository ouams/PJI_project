package util;

import java.util.List;

import csv.CSVHandler;

public class DateHandler {

	public static String dateConverter(String date) {
		final List<String[]> listMois = CSVHandler.read("Mois.csv", ',',
				"UTF-8");
		System.out.println(date);
		final String mois = date.split(" ")[2];

		for (final String[] strings : listMois) {
			if (mois.equals(strings[0])) {
				date = date.replace(mois, strings[1]);
			}
		}

		date = date.replaceAll(" ", "/");

		return date.substring(date.indexOf("/") + 1);
	}
}
