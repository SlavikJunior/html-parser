import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        String url = "https://www.russianfood.com/recipes/recipe.php?rid=158937";

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

        // 3.1. ОПИСАНИЕ РЕЦЕПТА
        String description = extractDescription(doc);
        System.out.println("Описание: " + description);

        // 3.2. ПОЛНОЕ ОПИСАНИЕ РЕЦЕПТА
        String fullDescription = extractFullDescription(doc);
        System.out.println("Полное описание: " + fullDescription);

        // 4. ИНГРЕДИЕНТЫ И ПОРЦИИ
        System.out.println("\n=== ИНГРЕДИЕНТЫ ===");
        Elements ingredients = extractIngredients(doc);
        String portions = extractPortions(doc);
        System.out.println("Порции: " + portions);

        for (Element ingredient : ingredients) {
            System.out.println("  - " + ingredient.text());
        }

        // 5. ВРЕМЯ ПРИГОТОВЛЕНИЯ
        System.out.println("\n=== ВРЕМЯ ПРИГОТОВЛЕНИЯ ===");
        String cookingTime = extractCookingTime(doc);
        System.out.println("Общее время: " + cookingTime);

        String yourTime = extractYourTime(doc);
        System.out.println("Ваше время: " + (yourTime != null ? yourTime : "не указано"));

        // 6. ВИДЕО (если есть)
        System.out.println("\n=== ВИДЕО ===");
        String videoId = extractVideoId(doc);
        if (videoId != null && !videoId.isEmpty()) {
            System.out.println("YouTube ID: " + videoId);
            System.out.println("Ссылка: https://www.youtube.com/watch?v=" + videoId);
        } else {
            System.out.println("Видео не найдено");
        }

        // 7. ШАГИ ПРИГОТОВЛЕНИЯ
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

    private static String extractFullDescription(Document doc) {
        // Ищем полное описание
        Element fullDescElement = doc.getElementById("how");
        return fullDescElement != null ? fullDescElement.text() : "Полное описание не найдено";
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

    private static String extractCookingTime(Document doc) {
        // Ищем блок с информацией о времени приготовления
        Element timeElement = doc.select("div.sub_info div.el:has(i.ico_time)").first();
        if (timeElement != null) {
            // Извлекаем общее время (первый span с классом hl)
            Element totalTimeSpan = timeElement.select("span.hl").first();
            if (totalTimeSpan != null) {
                // Получаем полный текст времени (например: "50 мин")
                String timeText = totalTimeSpan.text();

                // Находим элемент <b> внутри span для получения числового значения
                Element bElement = totalTimeSpan.select("b").first();
                if (bElement != null) {
                    String timeValue = bElement.text();
                    String timeUnit = timeText.replace(timeValue, "").trim();
                    return timeValue + " " + timeUnit;
                }

                return timeText;
            }
        }

        // Альтернативный поиск по тексту
        Element timeBlock = doc.select("div.el:contains(мин)").first();
        if (timeBlock != null) {
            String text = timeBlock.text();
            // Ищем общее время (до скобки)
            if (text.contains("(")) {
                return text.substring(0, text.indexOf("(")).trim();
            }
            return text;
        }

        return "Время не указано";
    }

    private static String extractYourTime(Document doc) {
        // Ищем блок с информацией о времени приготовления
        Element timeElement = doc.select("div.sub_info div.el:has(i.ico_time)").first();
        if (timeElement != null) {
            String text = timeElement.text();

            // Ищем "ваши" в скобках
            if (text.contains("ваши")) {
                // Используем регулярное выражение для поиска вашего времени
                Pattern pattern = Pattern.compile("ваши\\s*<b>(\\d+)</b>\\s*(\\S+)");
                Matcher matcher = pattern.matcher(timeElement.html());
                if (matcher.find()) {
                    return matcher.group(1) + " " + matcher.group(2);
                }

                // Альтернативный поиск в тексте
                Pattern textPattern = Pattern.compile("ваши\\s*(\\d+)\\s*(мин|часов|час|ч)");
                Matcher textMatcher = textPattern.matcher(text);
                if (textMatcher.find()) {
                    return textMatcher.group(1) + " " + textMatcher.group(2);
                }
            }
        }

        // Альтернативный поиск
        Elements timeSpans = doc.select("span.hl");
        if (timeSpans.size() >= 2) {
            // Второй span.hl часто содержит "ваше время"
            Element yourTimeSpan = timeSpans.get(1);
            if (yourTimeSpan.text().contains("мин")) {
                return yourTimeSpan.text();
            }
        }

        return null;
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