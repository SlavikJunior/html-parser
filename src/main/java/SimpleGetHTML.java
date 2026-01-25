import org.jsoup.Jsoup;

import java.io.IOException;

public class SimpleGetHTML {

    public static void main(String[] args) throws IOException {

        System.out.println(Jsoup.connect("https://www.russianfood.com/recipes/recipe.php?rid=129539").get());
    }
}
