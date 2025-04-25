package com.luci.search.service;

import com.luci.search.model.SearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private String extractImageUrl(Document doc) {
        Elements infobox = doc.select(".infobox img");
        if (!infobox.isEmpty()) {
            return "https:" + infobox.first().attr("src");
        }
        Elements galleryImages = doc.select(".gallerybox .thumb img");
        if (!galleryImages.isEmpty()) {
            return "https:" + galleryImages.first().attr("src");
        }

        Elements images = doc.select("img");
        if (!images.isEmpty()) {
            String firstImageUrl = images.first().attr("src");
            if (firstImageUrl.contains("wikipedia")) {
                return "";
            }
            return "https:" + firstImageUrl;
        }
       
        return "";


    }

    public SearchResult searchWikipedia(String query) {

        try {
            String searchUrl = "https://en.wikipedia.org/wiki/" + query.replace(" ", "_");

            Document doc = Jsoup.connect(searchUrl).get();
            String title = doc.title().replace(" - Wikipedia", "");
            String summary;
            Element firstParagraph = doc.select("p").first();
            summary = firstParagraph != null ? firstParagraph.text() : "";
            if(summary.isEmpty()){
                Element secondParagraph = doc.select("p").get(1);
                summary = secondParagraph != null ? secondParagraph.text() : "";
            }          

            // Check for disambiguation page
            if (summary.contains("may refer to")) {
                Elements links = doc.select("div#mw-content-text ul li a[href^=/wiki/]");
                String targetLink = null;

                for (Element link : links) {
                    String linkText = link.text().toLowerCase();
                    if (linkText.contains(query.toLowerCase())) {
                        targetLink = link.attr("href");
                        break;
                    }
                  }
                  if (targetLink == null && !links.isEmpty()) {
                    targetLink = links.first().attr("href");
                }

                if (targetLink != null) {
                    Document newDoc = Jsoup.connect("https://en.wikipedia.org" + targetLink).get();
                    title = newDoc.title().replace(" - Wikipedia", "");
                    Element newFirstParagraph = newDoc.select("p").first();
                    summary = newFirstParagraph != null ? newFirstParagraph.text() : "";
                    if(summary.isEmpty()){
                        Element newSecondParagraph = newDoc.select("p").get(1);
                        summary = newSecondParagraph != null ? newSecondParagraph.text() : "";
                    }
                    doc = newDoc;
                }
            }
            String imageUrl = extractImageUrl(doc);
            return new SearchResult(title, summary, imageUrl);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            return new SearchResult("Not Found", "No summary available.", "");
        }
    }
}
