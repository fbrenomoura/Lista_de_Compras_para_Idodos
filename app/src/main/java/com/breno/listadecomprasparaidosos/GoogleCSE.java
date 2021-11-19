package com.breno.listadecomprasparaidosos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoogleCSE extends Thread {
    private static final String  API_KEY          = "AIzaSyCYhMrCoQ9smWJJn7axXuz-wooweThQtXU";
    private static final String  SEARCH_ENGINE_ID = "124702026900ad302";
    private static       boolean useRestrictedApi;
    private static       String  query;
    private static       URL     imageURL       = null;

    private static URL getImageCSE(String queryTerm, boolean restrictedApi) {
        useRestrictedApi = restrictedApi;
        query            = queryTerm;

        Thread downloadThread = new GoogleCSE();
        downloadThread.start();

        try{
            downloadThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    //NETWORK OPERATION - GETTING IMAGE URL
    @Override
    public void run() {
        String            link;
        List<String>      linkList      = new ArrayList<>();
        HttpURLConnection urlConnection;
        String            apiUrlRequest;

        if (useRestrictedApi)
            apiUrlRequest = "https://www.googleapis.com/customsearch/v1/siterestrict?key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";
        else
            apiUrlRequest = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID + "&q=imagesize%3A1200x600+" + query + "&searchType=image";

        URL url = null;
        try {
            url = new URL(apiUrlRequest);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        conn.setRequestProperty("Accept", "application/json");
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //The response is json
        //Process to find URL from json
        String output = null;
        while (true) {
            try {
                if ((output = br.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

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
            try {
                imageURL = new URL("https://www.solidbackgrounds.com/images/1200x600/1200x600-black-solid-color-background.jpg");
            } catch (MalformedURLException malformedURLException) {
                malformedURLException.printStackTrace();
            }
        }
    }

}
