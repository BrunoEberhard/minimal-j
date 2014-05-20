package org.minimalj.autofill;

public class NameGenerator {

	// http://www.adp-gmbh.ch/misc/tel_book_ch.html
	
	private enum OfficialName {
		Müller(0.0082), Meier(0.0053), Schmid(0.0047), Keller(0.0036), Weber(0.0036),
		Huber(0.0029), Schneider(0.0029), Meyer(0.0028), Steiner(0.0025), Fischer(0.0023),
		Gerber(0.0023), Brunner(0.0023), Baumann(0.0022), Frei(0.0022), Zimmermann(0.0022),
		Moser(0.0021), Widmer(0.0021), Wyss(0.0018), Graf(0.0019), Roth(0.0018);
		
		private double percentage;
		
		private OfficialName(double percentage) {
			this.percentage = percentage;
		}
	}
	
	public static String officialName() {
		while (true) {
			double r = Math.random();
			
			for (OfficialName name : OfficialName.values()) {
				if (r < name.percentage) return name.name();
				else r = r - name.percentage;
			}
		}
	}

	private static int streetCount = 0;
	
	private enum StreetName {
		Bahnhofstrasse(1368), Hauptstrasse(1269), Dorfstrasse(1193), Industriestrasse(523), Schulstrasse(440),
		Oberdorfstrasse(424), Posstrasse (362), Schulhausstrasse(351), Kirchweg(347), Birkenweg(338),
		Kirchgasse(307), Kirchstrasse(301), Bergstrasse(295), Bahnhofplatz(288), Unterdorfstrasse(284),
		Gartenstrasse(272), Rosenweg(257), Bachstrasse(253), Ringstrasse(248);
		
		private int amount;
		
		private StreetName(int amount) {
			this.amount = amount;
			streetCount += amount;
		}
	}
	
	public static String street() {
		while (true) {
			int r = (int)(Math.random() * streetCount);
			
			for (StreetName name : StreetName.values()) {
				if (r < name.amount) return name.name();
				else r = r - name.amount;
			}
		}
	}
}
