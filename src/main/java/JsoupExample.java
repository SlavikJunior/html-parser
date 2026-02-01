import com.example.whattoeat.data.net.hack.ProxyRepository;
import com.example.whattoeat.data.net.hack.UserAgentRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class JsoupExample {

    private static final ProxyRepository proxyRepository = new ProxyRepository();
    private static final UserAgentRepository userAgentRepository = new UserAgentRepository();

    private static final int MAX_RETRIES = 20;
    private static final int TIMEOUT = 7500;

    public static void main(String[] args) throws IOException {
        String baseUrl = "https://www.russianfood.com";
        String urlSearch = "https://www.russianfood.com/search/simple/index.php";

        String searchQuery = "блины";
        String category = "27";
        String kitchen = "103";
        boolean vegetarian = false;

        String encodedQuery = URLEncoder.encode(searchQuery, "Windows-1251");

        StringBuilder urlBuilder = new StringBuilder(urlSearch);
        urlBuilder.append("?sskw_title=").append(encodedQuery)
                .append("&tag_tree[1][]=").append(category)
                .append("&tag_tree[2][]=").append(kitchen);

        urlBuilder.append("&ssgrtype=bytype");
        String url = urlBuilder.toString();

        System.out.println("Поисковый URL: " + url);
        System.out.println("Фильтр 'Вегетарианские': " + (vegetarian ? "ВКЛ" : "ВЫКЛ"));

        Document doc = null;

        System.out.println("Пробуем с прокси");
        doc = tryWithProxies(url, MAX_RETRIES);

        // Обработка результата
        Elements elements = doc.getElementsByClass("in_seen");
        List<String> recipeLinks = new ArrayList<>();

        for (Element recipeElement : elements) {
            Element linkElement = recipeElement.selectFirst("a[href]");
            if (linkElement != null) {
                String relativeLink = linkElement.attr("href");
                String absoluteLink = relativeLink.startsWith(baseUrl) ?
                        relativeLink : baseUrl.concat(relativeLink);
                recipeLinks.add(absoluteLink);

                Element titleElement = recipeElement.selectFirst("h3");
                String title = titleElement != null ? titleElement.text() : "Без названия";

                System.out.println("Рецепт: " + title);
                System.out.println("Ссылка: " + absoluteLink);
                System.out.println("---");
            }
        }

        System.out.println("\nВсего найдено ссылок: " + recipeLinks.size());

        proxyRepository.printProxyStats();
    }

    private static Document tryWithProxies(String url, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                System.out.println("\nПопытка №" + (i + 1) + " с прокси");

                Document doc = Jsoup.connect(url)
                        .userAgent(userAgentRepository.getRandomUserAgent(true))
                        .proxy(proxyRepository.getRandomProxy(true))
                        .referrer("https://www.russianfood.com/search/")
                        .timeout(TIMEOUT)
                        .ignoreHttpErrors(true)
                        .execute()
                        .parse();

                System.out.println("Успешно с прокси!");
                return doc;

            } catch (Exception e) {
                System.out.println("Попытка " + (i + 1) + " не удалась: " +
                        (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }
        return null;
    }
}