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

    static URL getImageCSE(String query, boolean useRestrictedApi) throws IOException {
        String            link;
        List<String>      linkList      = new ArrayList<>();
        URL               imageURL      = null;
        HttpURLConnection urlConnection;
        String            apiUrlRequest;

        if (useRestrictedApi)
            apiUrlRequest = "https://www.googleapis.com/customsearch/v1/siterestrict?key=" + apiKey + "&cx=" + searchEngineID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";
        else
            apiUrlRequest = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + searchEngineID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";

        URL url = new URL(apiUrlRequest);

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
        } catch (Exception e) { //IMAGES NOT FOUND - USE BLACK SCREEN INSTEAD
            imageURL = new URL("https://www.solidbackgrounds.com/images/1200x600/1200x600-black-solid-color-background.jpg");
        }

        return imageURL;
    }

    static ArrayList<URL> setImagesOnList(ArrayList<String> items) throws IOException {
        ArrayList<URL> imagesUrl = new ArrayList<>();

        for(int i = 0; i < items.size(); i++){
            try {
                imagesUrl.add(getImageCSE(items.get(i).split(" ", 2)[1], false));
            } catch (Exception e) {
                //ALL WEB SEARCH API QUOTA REACHED LIMIT - USE RESTRICTED API INSTEAD
                imagesUrl.add(getImageCSE(items.get(i).split(" ", 2)[1], true));
            }

        }
        return imagesUrl;
    }

}
