import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class traceroute {
	static String indexURL = "http://www.traceroute.org/";

	public static void main(String[] args) throws Exception {
		// String ip = args[0];
		// String filename = args[1];
		// FileOutputStream fos = new FileOutputStream(filename);
		// connect to the traceroute index
		Document doc = Jsoup.connect(indexURL).get();
		// find the list of countries
		Iterator<Element> countryIte = doc.select(
				"body > h3:contains(by country) + table tbody tr td span")
				.iterator();
		// for each country, find all its websites
		while (countryIte.hasNext()) {
			String countryName = countryIte.next().text();
			System.out.println(countryName);
			Iterator<Element> selectByCountry = doc.select(
					"body > h3:has(a[id=" + countryName + "]) + ul li a")
					.iterator();
			// for each website, test if it is available for connection
			while (selectByCountry.hasNext()) {
				Element selected = selectByCountry.next();
				String url = selected.attr("href");
				try {
					Document subdoc = Jsoup.connect(url).timeout(10000).get();
					Elements meta = subdoc.select("html head meta");
					if (meta.attr("http-equiv").contains("refresh"))
						subdoc = Jsoup.connect(
								meta.attr("content").split("=")[1]).get();
					Element form = subdoc.select("form").first();
					System.out.println(selected.text());
					// if (form.hasAttr("action"))
					// System.out.println(form.attr("action"));
					// if (form.hasAttr("method"))
					// System.out.println(form.attr("method"));
					// Iterator<Element> selectIte = form.select("select")
					// .iterator();
					// while (selectIte.hasNext()) {
					// Element selectEntry = selectIte.next();
					// if (selectEntry.hasAttr("name"))
					// System.out.println(selectEntry.attr("name"));
					// Iterator<Element> selectOption = selectEntry.select(
					// "option").iterator();
					// }
					System.out.println(form);
				} catch (Exception e) {
					continue;
				}
				// System.out.println(url);
			}
		}
	}
}
