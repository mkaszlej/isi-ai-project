package crawler;


import java.util.ArrayList;

public class SimpleCrawler {

	
	public SimpleCrawler() {
		
	}
	
	public static void main(String [] args) 
	{
		ArrayList<String> gminy = new ArrayList<>();

		
		//JUSTYNA
		
////		gminy.add("Góra Kalwaria");
////		gminy.add("Konstancin-Jeziorna");
//		gminy.add("Piaseczno");
//		gminy.add("Tarczyn");
//		gminy.add("Lesznowola");
//		gminy.add("Prażmów");
//		gminy.add("Pionki");
//		gminy.add("Iłża");
//		gminy.add("Skaryszew");
//		gminy.add("Gózd");
//		gminy.add("Jastrzębia");
//		gminy.add("Jedlińsk");
//////		gminy.add("Jedlnia-Letnisko");
////		gminy.add("Kowala");
//		gminy.add("Przytyk");
//		gminy.add("Wierzbica");
////		gminy.add("Wolanów");
////		gminy.add("Zakrzew");
////		gminy.add("Sierpc");
//		gminy.add("Gozdowo");
//		gminy.add("Mochowo");
//		gminy.add("Rościszewo");
//		gminy.add("Szczutowo");
//		gminy.add("Zawidz");
////
////		//KAJECZNA
////		
//		gminy.add("Czerwonka");
		gminy.add("Jabłonna");
		gminy.add("Karniewo");
		gminy.add("Krosnosielc");
		gminy.add("Legionowo");
		gminy.add("Lipsko");
////		gminy.add("Maków Mazowiecki");
		gminy.add("Nieporęt");
		gminy.add("Pułtusk");
		gminy.add("Różan");
		gminy.add("Rzeczniów");
		gminy.add("Rzewnie");
		gminy.add("Serock");
		gminy.add("Sienno");
//		gminy.add("Wieliszew");
		
		ZumiCrawler z = new ZumiCrawler("mazowieckie", gminy);
		z.pelzaj();
	}
	
}
