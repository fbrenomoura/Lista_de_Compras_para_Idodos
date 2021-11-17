package com.breno.listadecomprasparaidosos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoogleCSE {
    private final static String apiKey         = "AIzaSyCYhMrCoQ9smWJJn7axXuz-wooweThQtXU";
    private final static String searchEngineID = "124702026900ad302";

    static URL getImageCSE(String query) throws IOException {
        String            link;
        List<String>      linkList      = new ArrayList<>();
        URL               imageURL      = null;
        HttpURLConnection urlConnection;

        //Site restricted JSON API results
        //URL url = new URL("https://www.googleapis.com/customsearch/v1/siterestrict?key=" + apiKey + "&cx=" + searchEngineID + "&q=imagesize%3A1200x600+" + query + "&searchType=image");

        //All web results
        URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + searchEngineID + "&q=imagesize%3A1200x600+" + query + "&searchType=image");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        //The response is json
        //Process to find URL from json
        String output;
        while ((output = br.readLine()) != null) {

            if (output.contains("\"link\": \"")) {

                link = output.substring(output.indexOf("\"link\": \"") + ("\"link\": \"").length(),
                        output.indexOf("\","));
                linkList.add(link);
            }
        }
        conn.disconnect();

        //The process of downloading an image from a URL.

        try {
            imageURL = new URL(linkList.get(0));

            urlConnection = (HttpURLConnection) imageURL.openConnection();
            //If true, allow redirects
            urlConnection.setInstanceFollowRedirects(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageURL;
    }

    static ArrayList<URL> setImagesOnList(ArrayList<String> items) throws IOException {
        ArrayList<URL> imagesUrl = new ArrayList<>();

        for(int i = 0; i < items.size(); i++){
                imagesUrl.add(getImageCSE(items.get(i).split(" ", 2)[1]));
                System.out.println("URL RESULT: " + imagesUrl.get(i).toString());
        }
        return imagesUrl;
    }

}
