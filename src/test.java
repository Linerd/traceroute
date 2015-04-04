import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class test {

	public static void main(String[] args) throws Exception {
		Document doc = Jsoup.connect("http://tools.telpin.com.ar/cgi-bin/traceroute").timeout(0).get();
		Elements es = doc.select("input");
		es = es.select("input:not(input[name])");
		System.out.println(es.toString());

	}

}
