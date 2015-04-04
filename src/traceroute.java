import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class traceroute {
	static String traceroute = "http://www.traceroute.org/";
	static String lookingglass = "http://www.lookinglass.org/sorted.php";
	static String traceroute2 = "http://www.bgp4.net/tr";
	// static Set<String> allUrl = new HashSet<String>();
	// static Set<String> parsedUrl = new HashSet<String>();
	static BufferedWriter bw;

	public static void parse(String url, String ip, String filename) {
		try {
			System.out.println("==================>parsing " + url);
			Map<String, String> postData = new HashMap<String, String>();
			String getData;
			Elements servers = null;
			Document doc = Jsoup.connect(url).followRedirects(true)
					.timeout(20000).get();
			Elements meta = doc.select("html head meta");
			if (meta.attr("http-equiv").contains("refresh")) {
				url = meta.attr("content").split("=")[1];
				doc = Jsoup.connect(url).timeout(20000).get();
			}

			Elements forms = doc.select("form");
			Elements frames = doc.select("frame");
			if (frames.isEmpty())
				frames = doc.getElementsByTag("iframe");
			if (!frames.isEmpty()) {
				for (Element frame : frames) {
					String frame_url = frame.absUrl("src");
					if (!frame_url.equals(url) && !frame_url.equals(""))
						parse(frame_url, ip, filename);
				}
			}
			if (forms.isEmpty()) {
				url = doc.select("a[href]:contains(trace)").first()
						.absUrl("href");
				if (!url.equals("")) {
					doc = Jsoup.connect(url).timeout(20000).get();
					forms = doc.select("form");
				} else
					return;
			}
			if (forms.isEmpty())
				return;
			// else System.out.println(forms);
			for (Element form : forms) {
				boolean isPost = false;
				boolean serverInSelect = false;
				postData.clear();
				getData = "?";
				if (form.attributes().toString().contains("search")
						|| form.attributes().toString().contains("login")
						|| form.attributes().toString().equals(""))
					continue;
				String action = form.attr("action").equals("") ? url : form
						.absUrl("action");
				String method = form.attr("method").equals("") ? "get" : form
						.attr("method").toLowerCase();
				if (method.equals("post"))
					isPost = true;
				Elements selects = form.select("select");
				for (Element select : selects) {
					if (select.attributes().toString().contains("server")
							|| select.attributes().toString()
									.contains("router")) {
						if (select.hasAttr("name")) {
							servers = select.select("option");
							serverInSelect = true;
						}
						continue;
					}
					Elements commands = select
							.select("option[value*=traceroute]");
					if (commands.isEmpty()) {
						commands = select.select("option[value*=trace]");
					}
					if (commands.isEmpty()) {
						commands = select.select("option[value*=tr]");
					}
					if (!commands.isEmpty()) {
						if (commands.size() > 1)
							commands = commands.not("option[value*=6]");
						Element command = commands.first();
						if (select.hasAttr("name") && command.hasAttr("value")) {
							if (isPost) {
								postData.put(select.attr("name"),
										command.attr("value"));
								continue;
							} else {
								getData += (select.attr("name") + "="
										+ command.attr("value") + "&");
								continue;
							}
						}
					} else {
						Element selected = select.select("option[selected]")
								.first();
						if (select.hasAttr("name") && selected.hasAttr("value")) {
							if (isPost) {
								postData.put(select.attr("name"),
										selected.attr("value"));
							} else {
								getData += (select.attr("name") + "="
										+ selected.attr("value") + "&");
							}
						}
					}

				}
				Elements texts = form
						.select("input[type=text],input:not(input[type])");
				// Elements texts = form.select("input[type~=(text)?]");
				// System.out.println(texts);
				boolean ipAdded = false;
				if (!texts.select("input[value~=(\\d+\\.){3}\\d+]").isEmpty()) {
					Element e = texts.select("input[value~=(\\d+\\.){3}\\d+]")
							.first();
					if (e.hasAttr("name") && e.hasAttr("value")) {
						if (isPost)
							postData.put(e.attr("name"), ip);
						else
							getData += (e.attr("name") + "=" + ip + "&");
						ipAdded = true;
						// texts.remove(texts.select(
						// "input[value~=(\\d+\\.){3}\\d+]").first());
						texts = texts.not("input[value~=(\\d+\\.){3}\\d+]");
					} else
						continue;
				} else if (!texts.select("input[name*=host]").isEmpty()) {
					Element e = texts.select("input[name*=host]").first();

					if (isPost)
						postData.put(e.attr("name"), ip);
					else
						getData += (e.attr("name") + "=" + ip + "&");
					ipAdded = true;
					// texts.remove(texts.select("input[name*=host]").first());
					texts = texts.not("input[name*=host]");

				} else if (!texts.select("input[name*=des]").isEmpty()) {
					Element e = texts.select("input[name*=des]").first();

					if (isPost)
						postData.put(e.attr("name"), ip);
					else
						getData += (e.attr("name") + "=" + ip + "&");
					ipAdded = true;
					// texts.remove(texts.select("input[name*=des]").first());
					texts = texts.not("input[name*=des]");
				} else if (!texts.select("input[name*=domain]").isEmpty()) {
					Element e = texts.select("input[name*=domain]").first();

					if (isPost)
						postData.put(e.attr("name"), ip);
					else
						getData += (e.attr("name") + "=" + ip + "&");
					ipAdded = true;
					// texts.remove(texts.select("input[name*=domain]").first());
					texts = texts.not("input[name*=domain]");

				} else if (!texts.select("input[name*=arg]").isEmpty()) {
					Element e = texts.select("input[name*=arg]").first();

					if (isPost)
						postData.put(e.attr("name"), ip);
					else
						getData += (e.attr("name") + "=" + ip + "&");
					ipAdded = true;
					// texts.remove(texts.select("input[name*=arg]").first());
					texts = texts.not("input[name*=arg]");

				} else if (!texts.select("input[name*=addr]").isEmpty()) {
					Element e = texts.select("input[name*=addr]").first();

					if (isPost)
						postData.put(e.attr("name"), ip);
					else
						getData += (e.attr("name") + "=" + ip + "&");
					ipAdded = true;
					// texts.remove(texts.select("input[name*=arg]").first());
					texts = texts.not("input[name*=addr]");

				}
				if (ipAdded) {
					for (Element input : texts) {
						if (input.hasAttr("name") && input.hasAttr("value")) {
							if (!input.attr("value").equals("")) {
								if (isPost)
									postData.put(input.attr("name"),
											input.attr("value"));
								else
									getData += (input.attr("name") + "="
											+ input.attr("value") + "&");
							}

						}
					}
				} else {
					boolean flag = false;
					for (Element input : texts) {
						if (input.hasAttr("name") && input.hasAttr("value")) {
							if (flag) {
								if (!input.attr("value").equals("")) {
									if (isPost)
										postData.put(input.attr("name"),
												input.attr("value"));
									else
										getData += (input.attr("name") + "="
												+ input.attr("value") + "&");
								}
							} else {
								if (isPost)
									postData.put(input.attr("name"), ip);
								else
									getData += (input.attr("name") + "=" + ip + "&");
								flag = true;
							}
						}
					}
				}
				Elements submits = form.select("input[type=submit]");
				for (Element submit : submits) {
					if (submit.hasAttr("name") && submit.hasAttr("value")) {
						if (isPost)
							postData.put(submit.attr("name"),
									submit.attr("value"));
						else
							getData += (submit.attr("name") + "="
									+ submit.attr("value") + "&");
					}
				}
				Elements hiddens = form.select("input[type=hidden]");
				for (Element hidden : hiddens) {
					if (hidden.hasAttr("name") && hidden.hasAttr("value")) {
						if (isPost)
							postData.put(hidden.attr("name"),
									hidden.attr("value"));
						else
							getData += (hidden.attr("name") + "="
									+ hidden.attr("value") + "&");
					}
				}
				Elements radios = form.select("input[type=radio]");
				boolean hasTrace = false;
				boolean hasVersion = false;
				for (Element radio : radios) {
					if (hasTrace == false) {
						if (!radio.select("input[value*=traceroute]").isEmpty()) {
							hasTrace = true;
							if (isPost)
								postData.put(radio.attr("name"),
										radio.attr("value"));
							else
								getData += (radio.attr("name") + "="
										+ radio.attr("value") + "&");
							continue;
						} else if (!radio.select("input[value*=trace]")
								.isEmpty()) {
							hasTrace = true;
							if (isPost)
								postData.put(radio.attr("name"),
										radio.attr("value"));
							else
								getData += (radio.attr("name") + "="
										+ radio.attr("value") + "&");
							continue;
						} else if (!radio.select("input[value*=tr]").isEmpty()) {
							hasTrace = true;
							if (isPost)
								postData.put(radio.attr("name"),
										radio.attr("value"));
							else
								getData += (radio.attr("name") + "="
										+ radio.attr("value") + "&");
							continue;
						}
					}
					if (hasVersion == false) {
						if (!(radio.select("input[name*=ip]").isEmpty() || radio
								.select("input[value*=4]").isEmpty())) {
							hasVersion = true;
							if (isPost)
								postData.put(radio.attr("name"),
										radio.attr("value"));
							else
								getData += (radio.attr("name") + "="
										+ radio.attr("value") + "&");
							continue;
						}
					}
				}
				// if (radios.select("input[value*=traceroute]").isEmpty()) {
				// if (radios.select("input[value*=trace]").isEmpty()) {
				// if (radios.select("input[value*=tr]").isEmpty()) {
				// } else {
				// hasRadio = true;
				// radios = radios.select("input[value*=tr]");
				// }
				// } else {
				// hasRadio = true;
				// radios = radios.select("input[value*=trace]");
				// }
				// } else {
				// hasRadio = true;
				// radios = radios.select("input[value*=traceroute]");
				// radios.select("input[value*=6]").remove();
				// }
				// if (hasRadio) {
				// Element radio = radios.first();
				// if (isPost)
				// postData.put(radio.attr("name"), radio.attr("value"));
				// else
				// getData += (radio.attr("name") + "="
				// + radio.attr("value") + "&");
				// }
				if (servers == null || servers.isEmpty()) {
					servers = form.select("input[name*=server]");
				}
				if (servers == null || servers.isEmpty()) {
					servers = form.select("input[name*=router]");
				}
				if (isPost) {
					if (postData.size() < 1)
						continue;
					if (servers != null && !servers.isEmpty()) {
						for (Element server : servers) {
							if (!serverInSelect)
								postData.put(server.attr("name"),
										server.attr("value"));
							else {
								if (server.hasAttr("value")) {
									postData.put(
											server.parents().select("select")
													.first().attr("name"),
											server.attr("value"));
								} else {
									postData.put(
											server.parents().select("select")
													.first().attr("name"),
											server.text());
								}
							}
							// System.out.println(url);

							Document result = Jsoup.connect(action)
									.referrer(url).timeout(300000)
									.data(postData).post();
							System.out.println(action);
							System.out.println(postData);
							bw.write("****" + url + "\n+=+=\n" + result
									+ "\n\n=+=+\n");
							if (!serverInSelect)
								postData.remove(server.attr("name"));
							else
								postData.remove(server.parents()
										.select("select").first().attr("name"));
						}
					} else {
						// System.out.println(url);

						Document result = Jsoup.connect(action).timeout(300000)
								.referrer(url).data(postData).post();
						System.out.println(action);
						System.out.println(postData);
						bw.write("****" + url + "\n+=+=\n" + result
								+ "\n\n=+=+\n");
					}
				} else {
					if (getData.length() < 10)
						continue;
					if (servers != null && !servers.isEmpty()) {
						for (Element server : servers) {
							String extradata = server.attr("name") + "="
									+ server.attr("value");
							// System.out.println(url);
							Document result = Jsoup
									.connect(
											action
													+ URLEncoder.encode(getData
															+ extradata,
															"UTF-8"))
									.referrer(url).timeout(300000).get();
							System.out.println(action + getData + extradata);
							bw.write("****" + url + "\n+=+=\n" + result
									+ "\n\n=+=+\n");
						}
					} else {
						getData = getData.substring(0, getData.length() - 1);
						if (getData.contains("&=") || getData.contains("=&"))
							continue;
						// System.out.println(url);
						Document result = Jsoup
								.connect(
										action
												+ URLEncoder.encode(getData,
														"UTF-8"))
								.timeout(300000).referrer(url).get();
						System.out.println(action + getData);
						bw.write("****" + url + "\n+=+=\n" + result
								+ "\n\n=+=+\n");
					}
				}
				// bw.flush();
				// parsedUrl.add(url);
			}
		} catch (Exception e) {
			try {
				bw.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
		String ip;
		if (args.length >= 1)
			ip = args[0];
		else
			ip = "166.111.132.123";
		String filename;
		if (args.length >= 2)
			filename = args[1];
		else
			filename = "test.txt";
		bw = new BufferedWriter(new FileWriter(filename));
		// connect to the traceroute index
		// Document doc = Jsoup.connect(traceroute).timeout(0).get();
		// // find the list of countries
		// Iterator<Element> countryIte = doc.select(
		// "body > h3:contains(by country) + table tbody tr td span")
		// .iterator();
		// // for each country, find all its websites
		// while (countryIte.hasNext()) {
		// String countryName = countryIte.next().text();
		// Iterator<Element> selectByCountry = doc.select(
		// "body > h3:has(a[id=" + countryName + "]) + ul li a")
		// .iterator();
		// // for each website, test if it is available for connection
		// while (selectByCountry.hasNext()) {
		// Element selected = selectByCountry.next();
		// String url = selected.attr("href");
		// if (!allUrl.contains(url)) {
		// allUrl.add(url);
		// parse(url, ip, filename);
		// }
		// }
		// }
		//
		// doc = Jsoup.connect(lookingglass).timeout(0).get();
		// Elements tr1 = doc.select("tr.tr-1 a");
		// Elements tr2 = doc.select("tr.tr-2 a");
		// for (Element tr : tr1) {
		// String url = tr.attr("href");
		// if (!allUrl.contains(url)) {
		// allUrl.add(url);
		// parse(url, ip, filename);
		// }
		// }
		// for (Element tr : tr2) {
		// String url = tr.attr("href");
		// if (!allUrl.contains(url)) {
		// allUrl.add(url);
		// parse(url, ip, filename);
		// }
		// }
		//
		// doc = Jsoup.connect(traceroute2).timeout(0).get();
		// Elements tr = doc.select("tr[class^=row] td.col2 a");
		// for (Element e : tr) {
		// String url = e.attr("href");
		// // System.out.println(url);
		// if (!allUrl.contains(url)) {
		// allUrl.add(url);
		// parse(url, ip, filename);
		// }
		// }
		BufferedReader br = new BufferedReader(new FileReader("urls.txt"));
		for (String url = br.readLine(); url != null && url.length() > 1; url = br
				.readLine()) {
			parse(url, ip, filename);
		}
		bw.close();
		br.close();
		// bw = new BufferedWriter(new FileWriter("urls.txt"));
		// for (String url : parsedUrl) {
		// bw.write(url + "\n");
		// }
		// bw.close();
		// br.close();
	}
}
