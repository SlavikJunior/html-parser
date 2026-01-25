import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
            String url = "https://www.russianfood.com/recipes/recipe.php?rid=122848";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://www.russianfood.com")
                    .timeout(10000)
                    .get();

            // 1. НАЗВАНИЕ РЕЦЕПТА
            String title = extractTitle(doc);
            System.out.println("Название: " + title);

            // 2. ГЛАВНАЯ КАРТИНКА
            String mainImage = extractMainImage(doc);
            System.out.println("Картинка: " + mainImage);

            // 3. ОПИСАНИЕ РЕЦЕПТА
            String description = extractDescription(doc);
            System.out.println("Описание: " + description);

            // 4. ИНГРЕДИЕНТЫ И ПОРЦИИ
            System.out.println("\n=== ИНГРЕДИЕНТЫ ===");
            Elements ingredients = extractIngredients(doc);
            String portions = extractPortions(doc);
            System.out.println("Порции: " + portions);

            for (Element ingredient : ingredients) {
                System.out.println("  - " + ingredient.text());
            }

            // 5. ВИДЕО (если есть)
            System.out.println("\n=== ВИДЕО ===");
            String videoId = extractVideoId(doc);
            if (videoId != null && !videoId.isEmpty()) {
                System.out.println("YouTube ID: " + videoId);
                System.out.println("Ссылка: https://www.youtube.com/watch?v=" + videoId);
            } else {
                System.out.println("Видео не найдено");
            }

            // 6. ШАГИ ПРИГОТОВЛЕНИЯ
            System.out.println("\n=== ШАГИ ПРИГОТОВЛЕНИЯ ===");
            Elements steps = extractSteps(doc);
            System.out.println("Всего шагов: " + steps.size());

            for (int i = 0; i < steps.size(); i++) {
                Element step = steps.get(i);
                System.out.println("\nШаг " + (i + 1) + ":");

                // Извлекаем картинку шага
                Element stepImg = step.select("img").first();
                if (stepImg != null) {
                    String imgUrl = stepImg.attr("src");
                    if (imgUrl.startsWith("//")) {
                        imgUrl = "https:" + imgUrl;
                    }
                    System.out.println("Картинка: " + imgUrl);
                }

                // Извлекаем описание шага
                Element stepText = step.select("p").first();
                if (stepText != null) {
                    System.out.println("Описание: " + stepText.text());
                }
            }
    }

    // Методы для извлечения данных
    private static String extractTitle(Document doc) {
        // Ищем заголовок в разных местах
        Element titleElement = doc.select("h1").first();
        if (titleElement == null) {
            titleElement = doc.select(".title h3").first();
        }
        if (titleElement == null) {
            titleElement = doc.select(".center_block h2").first();
        }
        return titleElement != null ? titleElement.text() : "Нет названия";
    }

    private static String extractMainImage(Document doc) {
        // Ищем главную картинку в разных местах
        Element imgElement = doc.select(".foto_big img").first();
        if (imgElement == null) {
            imgElement = doc.select(".image img").first();
        }
        if (imgElement == null) {
            imgElement = doc.select("img[src*=/dycontent/images_upl/]").first();
        }

        if (imgElement != null) {
            // Сначала пытаемся взять большую картинку (data-src), потом маленькую (src)
            String imgUrl = imgElement.hasAttr("data-src")
                    ? imgElement.attr("data-src")
                    : imgElement.attr("src");

            // Делаем URL абсолютным
            if (imgUrl.startsWith("//")) {
                imgUrl = "https:" + imgUrl;
            } else if (imgUrl.startsWith("/")) {
                imgUrl = "https://www.russianfood.com" + imgUrl;
            }

            return imgUrl;
        }
        return "Картинка не найдена";
    }

    private static String extractDescription(Document doc) {
        // Ищем описание в разных местах
        Element descElement = doc.select(".announce p").first();
        if (descElement == null) {
            descElement = doc.select(".recipe_text p").first();
        }
        if (descElement == null) {
            descElement = doc.select(".center_block p").first();
        }
        return descElement != null ? descElement.text() : "Описание не найдено";
    }

    private static Elements extractIngredients(Document doc) {
        Element ingredientsTable = doc.getElementById("from");
        if (ingredientsTable != null) {
            return ingredientsTable.select("tr[class^=ingr_tr_] span");
        }
        return new Elements();
    }

    private static String extractPortions(Document doc) {
        Element ingredientsTable = doc.getElementById("from");
        if (ingredientsTable != null) {
            Element portionElement = ingredientsTable.select("span.portion").first();
            return portionElement != null ? portionElement.text() : "Порции не указаны";
        }
        return "Порции не указаны";
    }

    private static String extractVideoId(Document doc) {
        // Ищем элемент с видео
        Element videoElement = doc.getElementById("player0");
        if (videoElement == null) {
            // Пробуем найти по другому id
            videoElement = doc.select("div[id*=player]").first();
        }

        if (videoElement != null) {
            String html = videoElement.html();

            // Ищем videoId в JavaScript коде
            Pattern pattern = Pattern.compile("videoId:\\s*'([^']+)'");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1);
            }

            // Ищем другой паттерн
            pattern = Pattern.compile("videoId\\s*=\\s*'([^']+)'");
            matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        // Ищем во всем документе
        String fullHtml = doc.html();
        Pattern pattern = Pattern.compile("videoId[=:]['\"]?([^'\"]+)['\"]?");
        Matcher matcher = pattern.matcher(fullHtml);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static Elements extractSteps(Document doc) {
        // Ищем блок с шагами
        Element stepsContainer = doc.getElementsByClass("step_images_n").first();
        if (stepsContainer != null) {
            return stepsContainer.getElementsByClass("step_n");
        }
        return new Elements();
    }
}