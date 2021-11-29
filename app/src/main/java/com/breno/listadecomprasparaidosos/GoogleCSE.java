package com.breno.listadecomprasparaidosos;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleCSE extends Thread {
    private static final String API_KEY = "AIzaSyCYhMrCoQ9smWJJn7axXuz-wooweThQtXU";
    private static final String SEARCH_ENGINE_ID = "124702026900ad302";
    private static boolean useRestrictedApi;
    private static String query;
    private static URL imageURL = null;

    private static URL getImageCSE(String queryTerm, boolean restrictedApi) {
        query = queryTerm;

        Thread downloadThread = new GoogleCSE();
        downloadThread.start();

        try {
            downloadThread.join();
        } catch (InterruptedException e) {
            downloadThread.interrupt();
            e.printStackTrace();
        }


        return imageURL;
    }

    @NonNull
    static ArrayList<URL> setImagesOnList(ArrayList<String> items) {
        ArrayList<URL> imagesUrl = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            imagesUrl.add(getImageCSE(items.get(i).split(" ", 2)[1], useRestrictedApi));
            if (imagesUrl.get(i) == null) {
                useRestrictedApi = true; //ALL FREE QUOTA USED - USE RESTRICTED API INSTEAD
                imagesUrl.set(i, getImageCSE(items.get(i).split(" ", 2)[1], useRestrictedApi));
            }
        }
        return imagesUrl;
    }

    //NETWORK OPERATION - GETTING IMAGE URL
    @Override
    public void run() {

        try {
            String link;
            List<String> linkList = new ArrayList<>();
            HttpURLConnection urlConnection;
            String apiUrlRequest;

            if (useRestrictedApi)
                apiUrlRequest = "https://www.googleapis.com/customsearch/v1/siterestrict?key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";
            else
                apiUrlRequest = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";

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
            } catch (Exception e) { //IMAGE NOT FOUND - USE BLACK SCREEN INSTEAD
                try {
                    imageURL = new URL("https://www.solidbackgrounds.com/images/1200x600/1200x600-black-solid-color-background.jpg");
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
