package crawler;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZumiCrawler {

	String bazowyUrl = "http://www.zumi.pl/lista-firm/";
	String wojewodztwo;
	String urlPrefiks;
	ArrayList<String> gminy;
	
	Baza dbConnection;
	
	public ZumiCrawler(String wojewodztwo, ArrayList<String> gminy)
	{
		this.wojewodztwo = wojewodztwo;
		this.gminy = gminy;
		
		this.dbConnection = new Baza();
		this.dbConnection.createTables();
	
		this.urlPrefiks = bazowyUrl+wojewodztwo+"-";
	}
		
	public void pelzaj()
	{
		for (String gmina : gminy) {
			zjedzMiasto(gmina);
			czekaj(10);
		}
	}
	
	private void zjedzMiasto(String gmina)
	{
		String kodowanaNazwa = URLEncoder.encode(gmina);
		String nazwaTabeli = gmina+System.currentTimeMillis();
		nazwaTabeli.replaceAll("[\\s]","");
		
		dbConnection.createTable(nazwaTabeli);
		
		/* Dane do bazy: */
		String kategoria = "";
		int TERYT = -1;
		
		/* 1. zjedz numer TERYT */
		TERYT = zjedzTERYT(gmina);
		
		/* 2. zjedz liste ulic */
		Elements dane;
		dane = zjedzListeUlic(kodowanaNazwa);
		if(dane == null)
			dane = zjedzListeBranz(kodowanaNazwa);
		if(dane == null){
			System.out.println("### BRAK DANYCH DLA GMINY " + gmina);
			return;
		}
		
		for (Element ulica : dane) {
		    zjedzUlice(kodowanaNazwa, TERYT, "http://www.zumi.pl"+ulica.attr("href"), nazwaTabeli);
		}
		
	}
	
	public void czekaj(int multiplier)
	{
		try{
			Thread.sleep(multiplier*(long)Math.abs((Math.random() * 1000 + 1)));
		}
		catch(Exception e)
		{
			System.out.println("### BLAD PODCZAS CZEKANIA...?");
			e.printStackTrace();
		}
	}
	
	public void zjedzUlice(String kodowanaNazwa, int TERYT, String link, String nazwaTabeli)
	{
		//Czekaj około 2 sekund...
		czekaj(2);

		Elements obiekty = zjedzListeObiektow(link);
		if(obiekty == null){
			System.out.println("### PUSTA ULICA: "+ link);
			return;
		}

		//Dla każdego obiektu z ulicy...
		for (Element obiekt : obiekty) 
		{
			String nazwa = obiekt.select("[itemprop=name]").text();
			String odnosnik = "http://www.zumi.pl"+obiekt.select("[itemprop=name]").attr("href");
			String adres = obiekt.select("[itemprop=adress]").text();
			String telefon = obiekt.select("[itemprop=telephone]").text();
			
			//Utworz nowy obiekt
			Obiekt ob = new Obiekt(URLDecoder.decode(kodowanaNazwa),TERYT,nazwa,adres,telefon);
			
			//zjedz strone obiektu
			zjedzStroneObiektu(ob, odnosnik);
			
			//zjedz kategorie obiektu
			zjedzKategorieObiektu(ob, odnosnik);
			
			//zapisz w bazie
			ob.zapisz(dbConnection, nazwaTabeli);
			
			System.out.println( ob.toString() );
		}
	}
	
	public void zjedzKategorieObiektu(Obiekt obiekt, String adres)
	{
		int wynik = -1;
		
		try {
			Document document = Jsoup.connect(adres)
					.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.referrer("http://www.google.com")
					.timeout(10000)
					.get();

			Element div = document.select(".block.blockText.blockKey").first();
			if(div == null){
				System.out.println("### BRAK KATEGORII DLA OBIEKTU...");
				return;
			}
			
			Iterator<Element> ite = div.select("a").iterator();
			
			// Lista obiektów do modyfikacji na bazie
			ArrayList<String> toAdd = new ArrayList<>();
			
			// Tablica licznikow kategorii obiektu
			int[] count = new int[29];
			for(int i=0; i<29; i++) count[i]=0;
			
			// Dla każdej kategorii...
			while(ite.hasNext())
			{
				// Pozbadz się przecinkow jesli są
				String[] tmp = ite.next().text().split(",");
				String kategoria = tmp[tmp.length-1];
				
				// Sprawdz czy jest w bazie i odczytaj wynik
				int result = dbConnection.slownikExsist( kategoria );
				
				if( result == -1 || result == 0 ){
					// Jest w bazie z -1 lub nie ma
					toAdd.add( kategoria );
				}
				else{
					// Jest w bazie z kategorią
					if(result >= 0 && result < 29 )	count[result]++;
						System.out.println("kategoria istnieje: " + kategoria );
				}
			}

			//znajdz najczęściej używaną kategorię
			int max = 0;
			for(int i=0; i<29; i++)
				if(count[i] > max)
				{
					max = count[i];
					wynik = i;
				}

			// zaktualizu obiekt
			obiekt.ustawKategorie(wynik);
			
			// Dodaj kategorie z listy toAdd
			Iterator<String> addIte = toAdd.iterator();
			while(addIte.hasNext())
			{
				String nazwa = addIte.next();
				if(dbConnection.insertSlownik( nazwa, wynik ))
					System.out.println("Dodano kategorię: " +  nazwa );
			}
			
			System.out.println("Ustawiono indekst kategorii: " + wynik );			
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
		
	}

	
	public void zjedzStroneObiektu(Obiekt obiekt, String odnosnik)
	{
		String wlasnosc = "";
		String zatrudnienie = "";
		String email = "";
		
		//PRZETWORZ ADRES
		String[] tmp = odnosnik.split("#");
		String aboutPage = tmp[ 0 ];
		
		try {
			Document doc = Jsoup.connect(aboutPage+"#aboutPage")
					.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.referrer("http://www.google.com")
					.timeout(10000)
					.get();
			
			Element lines = doc.select("div.block.blockText.blockServices").last();
			if(lines == null)
			{
				System.out.println("### BRAK DANYCH NA STRONIE ABOUT...1");
				return;
			}

			
			Iterator<Element> ite = lines.select(".textLabel").iterator();
			if(ite.hasNext() == false)
			{
				System.out.println("### BRAK DANYCH NA STRONIE ABOUT...2");
				return;
			}
			
			while(ite.hasNext())
			{
				Element e = ite.next();
				String result = e.text();
				if(result.contains("Forma własności:")) wlasnosc = e.nextSibling().nextSibling().toString();
				else if(result.contains("E-mail:")) email = e.nextElementSibling().text();
				else if(result.contains("Zatrudnienie:")) zatrudnienie = e.nextSibling().toString();
				
			}
			
			obiekt.ustawWlasnosc(wlasnosc);
			obiekt.ustawZatrudnienie(zatrudnienie);
			obiekt.ustawEmail(email);

		}
		catch(Exception e)
		{
			System.out.println("### BLAD PODCZAS PRZETWARZANIA STRONY ABOUT?");
			e.printStackTrace();
		}
	}
	
	public Elements zjedzListeObiektow(String link)
	{
		//Wez strone z ulicą
		try{
			Document document = Jsoup.connect(link)
				.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
				.referrer("http://www.google.com")
				.timeout(10000)
				.get();
			Elements objects = document.select("[itemprop=itemListElement]");
			if(objects.hasText())
				return objects;
			else return null;	

		}
		catch(Exception e)
		{
			System.out.println("### BLAD POBIERANIA OBIEKTOW Z ULICY");
			e.printStackTrace();
		}

		return null;	
		
	}
	
	public Elements zjedzListeBranz(String gmina)
	{
		try{
			Document document = Jsoup
								.connect(urlPrefiks+gmina)
								.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
								.referrer("http://www.google.com")
								.timeout(10000)
								.get();
			
			Elements streets = document.select(".popularCategories");
			if(streets.hasText())
			{
				Elements links = streets.select("a[href]");
					if(links.hasText())
					{	return links;	}
			}
			else return null;	
			
		}
		catch(Exception e)
		{
			System.out.println("### BRAK BRANZ DO POBRANIA:");
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public Elements zjedzListeUlic(String gmina)
	{
		try{
			Document document = Jsoup
								.connect(urlPrefiks+gmina)
								.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
								.referrer("http://www.google.com")
								.timeout(10000)
								.get();
			
			Elements streets = document.select(".streetsInCity");
			if(streets.hasText())
			{
				Elements links = streets.select("a[href]");
					if(links.hasText())
					{	return links;	}
			}
			else return null;	
			
		}
		catch(Exception e)
		{
			System.out.println("### BRAK ULIC DO POBRANIA:");
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static int zjedzTERYT(String gmina)
	{
		String url = "http://www.stat.gov.pl/broker/access/performSearch.jspa?searchString="+gmina+"&level=gmi&wojewodztwo=2217&powiat=&gmina=&miejscowosc=&advanced=true";

		System.out.println("1. POBIERAM TERYT...");
		
		try {
			Document document = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.referrer("http://www.google.com")
					.timeout(10000)
					.get();

			Element table = document.select("#row").first();
			
			Iterator<Element> ite = table.select("td").iterator();
			
			int TERYT = Integer.parseInt(ite.next().text());

			System.out.println("TERYT: "+TERYT);			
			
			return TERYT;
			
		} catch (Exception e) {
			System.out.println("### BLAD POBIERANIA TERYTu:");
			e.printStackTrace();
		}
		
		return -1;
	}
	
}
