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
        // Prioritize infobox images
        Elements infoboxImages = doc.select(".infobox img");
        if (!infoboxImages.isEmpty()) {
            String src = infoboxImages.first().attr("src");
            if (src != null && !src.isEmpty()) {
                // Ensure it's an absolute URL or prepend https:
                return src.startsWith("http") ? src : "https:" + src;
            }
        }

        // Check for images within the main content area, often near the beginning
        Elements contentImages = doc.select("#mw-content-text img");
         for (Element img : contentImages) {
            String src = img.attr("src");
            if (src != null && !src.isEmpty()) {
                 // Basic filtering for known Wikipedia icons/logos/small images
                 if (src.contains("/static/images/") ||
                    src.contains("wikimediafoundation.org") ||
                    src.contains("px-") || // Often indicates small thumbnail sizes
                    img.hasClass("mw-file-element") // Filter out file icons etc.
                    ) {
                    continue; // Skip known non-content or very small thumbnail images
                }
                 // If a potential content image is found, return it
                 return src.startsWith("http") ? src : "https:" + src;
            }
        }


        // Fallback to searching all images as a last resort, with more aggressive filtering
        Elements allImages = doc.select("img");
        for (Element img : allImages) {
            String src = img.attr("src");
             if (src != null && !src.isEmpty()) {
                // More aggressive filtering for the general image search
                if (src.contains("/static/images/") ||
                    src.contains("wikimediafoundation.org") ||
                     src.contains("px-") ||
                     src.contains("svg") || // Often icons or diagrams
                     img.hasClass("mw-file-element") ||
                     img.parents().is(".thumb")) // Skip gallery thumbnails already covered
                    {
                    continue; // Skip non-content or less relevant images
                }
                 // If a potentially valid image is found, return it
                 return src.startsWith("http") ? src : "https:" + src;
            }
        }

        // If no suitable image was found after all attempts
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
