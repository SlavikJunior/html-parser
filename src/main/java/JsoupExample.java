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
        String baseUrl = "https://www.russianfood.com";
        String urlSearch = "https://www.russianfood.com/search/simple/index.php";

        String searchQuery = "блины";
        String category = "27"; // блины и оладьи
        String kitchen = "103"; // русская
        boolean vegetarian = true; // ← ВОТ ЭТО НОВЫЙ ПАРАМЕТР

        String encodedQuery = URLEncoder.encode(searchQuery, "Windows-1251");

        // Собираем URL с веганским фильтром
        StringBuilder urlBuilder = new StringBuilder(urlSearch);
        urlBuilder.append("?sskw_title=").append(encodedQuery)
                .append("&tag_tree[1][]=").append(category)
                .append("&tag_tree[2][]=").append(kitchen);

        // ← ДОБАВЛЯЕМ ВЕГАНСКИЙ ФИЛЬТР ЕСЛИ НУЖНО
        if (vegetarian) {
            urlBuilder.append("&tag_tree[7][216]="); // Пустое значение для активации чекбокса
        }

        urlBuilder.append("&ssgrtype=bytype");

        String url = urlBuilder.toString();

        System.out.println("Поисковый URL: " + url);
        System.out.println("Фильтр 'Вегетарианские': " + (vegetarian ? "ВКЛ" : "ВЫКЛ"));

        Document doc = Jsoup.connect(url)
                .userAgent(HttpConnection.DEFAULT_UA)
                .referrer("https://www.russianfood.com/search/")
                .timeout(10000)
                .get();

        Elements elements = doc.getElementsByClass("in_seen");

        Elements onUser = getFirstN(elements, -1);

//        onUser.forEach((element -> System.out.println(element + "\n=============================")));

        List<String> recipeLinks = new ArrayList<>();

        for (Element recipeElement : onUser) {
            // Находим ссылку внутри элемента
            Element linkElement = recipeElement.selectFirst("a[href]");
            if (linkElement != null) {
                String relativeLink = linkElement.attr("href");
                // Преобразуем относительную ссылку в абсолютную
                String absoluteLink;
                if (!relativeLink.startsWith(baseUrl))
                    absoluteLink = baseUrl.concat(relativeLink);
                else
                    absoluteLink = relativeLink;
                recipeLinks.add(absoluteLink);

                // Выводим также название рецепта
                Element titleElement = recipeElement.selectFirst("h3");
                String title = titleElement != null ? titleElement.text() : "Без названия";

                System.out.println("Рецепт: " + title);
                System.out.println("Ссылка: " + absoluteLink);
                System.out.println("---");
            }
        }

        System.out.println("\nВсего найдено ссылок: " + recipeLinks.size());
    }

    private static Elements getFirstN(Elements src, int n) {
        if (n < 0)
            return src;

        Elements dst = new Elements();
        int count = Math.min(n, src.size());
        for (int i = 0; i < count; i++) {
            dst.add(src.get(i));
        }
        return dst;
    }
}