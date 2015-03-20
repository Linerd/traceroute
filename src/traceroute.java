import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class traceroute {
	static String indexURL = "http://www.traceroute.org/";

	public static void main(String[] args) throws Exception {
		// String ip = args[0];
		String ip = "166.111.132.123";
		// String filename = args[1];
		FileOutputStream fos = new FileOutputStream("inputs");
		FileOutputStream fos2 = new FileOutputStream("options");
		FileOutputStream fos3 = new FileOutputStream("radios");
		// connect to the traceroute index
		Document doc = Jsoup.connect(indexURL).timeout(10000).get();
		// find the list of countries
		Iterator<Element> countryIte = doc.select(
				"body > h3:contains(by country) + table tbody tr td span")
				.iterator();
		// for each country, find all its websites
		while (countryIte.hasNext()) {
			String countryName = countryIte.next().text();
			System.out.println("^^^^^^^^^^^^^^^^^^" + countryName
					+ "^^^^^^^^^^^^^^^^^^");
			Iterator<Element> selectByCountry = doc.select(
					"body > h3:has(a[id=" + countryName + "]) + ul li a")
					.iterator();
			// for each website, test if it is available for connection
			Map<String, String> postData = new HashMap<String, String>();
			String getData = "";
			while (selectByCountry.hasNext()) {
				Element selected = selectByCountry.next();
				String url = selected.attr("href");
				try {
					Document subdoc = Jsoup.connect(url).timeout(10000).get();
					Elements meta = subdoc.select("html head meta");
					if (meta.attr("http-equiv").contains("refresh")) {
						url = meta.attr("content").split("=")[1];
						subdoc = Jsoup.connect(url).timeout(10000).get();
					}
					Elements forms = subdoc.select("form");
					if (forms.isEmpty()) {
						url = subdoc.select("a[href]:contains(trace)").first()
								.absUrl("href");
						if (url.equals("")) {
							url = subdoc.select("frame[src*=trace]").first()
									.absUrl("src");
						}
						if (url.equals("")) {
							url = subdoc.select("iframe[src*=trace]").first()
									.absUrl("src");
						}
						if (!url.equals("")) {
							subdoc = Jsoup.connect(url).timeout(10000).get();
							forms = subdoc.select("form");
							forms.select("form[**=search]").remove();
							forms.select("form[name*=login]").remove();
						} else
							continue;
					}
					if (forms.isEmpty())
						continue;
					System.out.println("=========================" + url
							+ "======================");
					for (Element form : forms) {
						boolean isPost = false;
						postData.clear();
						getData = "?";
						if (form.attributes().toString().contains("search")
								|| form.attributes().toString()
										.contains("login")
								|| form.attributes().toString().equals(""))
							continue;
						String action = form.attr("action").equals("") ? url
								: form.absUrl("action");
						String method = form.attr("method").equals("") ? "get"
								: form.attr("method").toLowerCase();
						if (method.equals("post"))
							isPost = true;
						System.out.println("action: " + action + ", method: "
								+ method);
						// System.out.println("form[action=" +
						// form.attr("action")
						// + ",method=" + form.attr("method") + "]");
						// System.out.println("form[" + form.attributes() +
						// "]");
						Elements selects = form.select("select");
						for (Element select : selects) {
							Elements options = select
									.select("option[value*=traceroute]");
							if (options.isEmpty()) {
								options = select.select("option[value*=trace]");
							}
							if (options.isEmpty()) {
								options = select.select("option[value*=tr]");
							}
							if (options.isEmpty()) {
								options = select.select("option[selected]");
							}
							if (options.size() == 1) {
								Element option = options.first();
								if (isPost)
									postData.put(select.attr("name"),
											option.attr("value"));
								else
									getData += (select.attr("name") + "="
											+ option.attr("value") + "&");
								// System.out.println("option["
								// + option.attributes() + "]");
								// fos2.write(("option[" + option.attributes() +
								// "]\n")
								// .getBytes());
							}
						}
						Elements inputs = form.select("input");
						boolean inputFlag = true;
						for (Element input : inputs) {
							if (inputFlag
									&& (input.attr("type").equals("") || input
											.attr("type").equalsIgnoreCase(
													"text"))) {
								if (input.attr("name").contains("port")) {
									if (isPost)
										postData.put(input.attr("name"),
												input.attr("value"));
									else
										getData += (input.attr("name") + "="
												+ input.attr("value") + "&");
									continue;
								}
								if (isPost)
									postData.put(input.attr("name"), ip);
								else
									getData += (input.attr("name") + "=" + ip + "&");
								inputFlag = false;
								// System.out.println("input["
								// + input.attributes() + "]");
								// fos.write(("input[" + input.attributes() +
								// "]\n")
								// .getBytes());
							}
							if (input.attr("type").equalsIgnoreCase("submit")) {
								if (input.hasAttr("name")) {
									if (isPost)
										postData.put(input.attr("name"),
												input.attr("value"));
									else
										getData += (input.attr("name") + "="
												+ input.attr("value") + "&");
								}
							}
						}
						Elements radios = form.select("input[type=radio]");
						boolean hasRadio = false;
						if (radios.select("input[value*=traceroute]").isEmpty()) {
							if (radios.select("input[value*=trace]").isEmpty()) {
								if (radios.select("input[value*=tr]").isEmpty()) {
								} else {
									hasRadio = true;
									radios = radios.select("input[value*=tr]");
								}
							} else {
								hasRadio = true;
								radios = radios.select("input[value*=trace]");
							}
						} else {
							hasRadio = true;
							radios = radios.select("input[value*=traceroute]");
							radios = radios.select("input[value*=6]").remove();
						}
						if (hasRadio && radios.size() == 1) {
							Element radio = radios.first();
							if (isPost)
								postData.put(radio.attr("name"),
										radio.attr("value"));
							else
								getData += (radio.attr("name") + radio
										.attr("value"));
							// System.out.println("radio[" + radio.attributes()
							// + "]");
							// fos3.write(("radio[" + radio.attributes() +
							// "]\n")
							// .getBytes());
						}
						if (isPost) {
							System.out.println(postData);
							Document result = Jsoup.connect(action)
									.data(postData).timeout(0).post();
							System.out.println(result);
						} else {
							if (getData.endsWith("&"))
								getData = getData.substring(0,
										getData.length() - 1);
							System.out.println(getData);
							action += getData;
							Document result = Jsoup.connect(action).timeout(0)
									.get();
							System.out.println(result);
						}
					}

					// System.out.println(selected.text());
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
					// System.out.println(form);
				} catch (Exception e) {
					// e.printStackTrace();
					continue;
				}
				// System.out.println(url);
			}
			System.out.println("\n\n");
		}
		fos.close();
		fos2.close();
		fos3.close();
	}
}
