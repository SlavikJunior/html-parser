import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {

        Elements elementsByTagTable = Jsoup.connect("https://www.russianfood.com/recipes/recipe.php?rid=129539").get().body()
                .getElementById("layout")
                .getElementsByClass("layout").first()
                .getElementsByClass("wrapper").first()
                .getElementsByTag("table");

        Elements heads = new Elements();
        Elements video = new Elements();
        Elements steps = new Elements();

        outer: for (Element elementTable : elementsByTagTable) {
            Elements subCenter = elementTable.getElementsByClass("sub_center");
            for (Element element : subCenter) {
                Elements trs = element.getElementsByClass("center_block")
                        .getFirst().getElementsByClass("recipe_new").getFirst().getElementsByTag("tr");

                for (Element tr : trs) {
                    Element head = tr.getElementById("from");
                    if (head != null)
                        heads.add(head);

                    Element currentVideo = tr.getElementById("player0");
                    if (currentVideo != null)
                        video.add(currentVideo);

                    Element allSteps = tr.getElementById("step_images_n");
                    if (allSteps != null) {
                        steps.addAll(allSteps.getElementsByClass("step_n"));
                    }

                    break outer;
                }
            }
        }
        System.out.println("START\n");
        System.out.println(heads);
        System.out.println("\n===================\n");
        System.out.println(video);
        System.out.println("\n===================\n");
        System.out.println(steps);
        System.out.println("\nEND");
    }
}