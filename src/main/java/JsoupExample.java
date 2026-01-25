import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class JsoupExample {

    public static void main(String[] args) throws IOException {
        String baseUrl = "https://www.russianfood.com/search/simple/index.php";

        String searchQuery = "блины";
        String category = "27"; // блины и оладьи
        String kitchen = "103"; // русская

        String encodedQuery = URLEncoder.encode(searchQuery, "Windows-1251");

        String url = baseUrl + "?sskw_title=" + encodedQuery +
                "&tag_tree[1][]=" + category +
                "&tag_tree[2][]=" + kitchen +
                "&ssgrtype=bytype";

        Document doc = Jsoup.connect(url)
                .userAgent(HttpConnection.DEFAULT_UA)
                .referrer("https://www.russianfood.com/search/")
                .timeout(10000) // время ожидания ответа от сервера, должно совпадать с тем, сколько ждём на ui
                .get();

        Elements elements = doc.getElementsByClass("in_seen");

        Elements onUser = getFirstN(elements, 5);

//        onUser.forEach((element -> System.out.println(element + "\n=============================")));

        List<String> recipeLinks = new ArrayList<>();

        for (Element recipeElement : onUser) {
            // Находим ссылку внутри элемента
            Element linkElement = recipeElement.selectFirst("a[href]");
            if (linkElement != null) {
                String relativeLink = linkElement.attr("href");
                // Преобразуем относительную ссылку в абсолютную
                String absoluteLink = "https://www.russianfood.com" + relativeLink;
                recipeLinks.add(absoluteLink);

                // Выводим ссылку
                System.out.println("Ссылка на рецепт: " + absoluteLink);
            }
        }

        System.out.println("\nВсего найдено ссылок: " + recipeLinks.size());
    }

    private static Elements getFirstN(Elements src, int n) {
        Elements dst = new Elements();
        for (int i = 0; i < n; i++) {
            dst.add(src.get(i));
        }
        return dst;
    }
}