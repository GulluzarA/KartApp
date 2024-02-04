package com.s349571s322911.kartappen;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class WebServices {

    String TAG = "Erlend";
    private final WebServicesListener webServicesListener;
    public ArrayList<WebMarker> webMarkerListe = new ArrayList<>();

    // Interface
    public interface WebServicesListener {
        void onHenteMarkerPOJOs(boolean success);
        void onHenteWebMarkers(boolean success);
        void onHenteWebMarkersEtterLagring(boolean success, WebMarker wm);
        void onLagreWebMarker(boolean success, WebMarker wm);
        void onSletteWebMarker(boolean success, WebMarker wm);
        void onEndreWebMarker(boolean success, WebMarker wm);
    }
    public WebServices(WebServicesListener webServicesListener){
        this.webServicesListener = webServicesListener;
    }

    // CALLBACKS

    public void onHenteMarkerPOJOsDone(boolean success){
        webServicesListener.onHenteMarkerPOJOs(success);
    }
    public void onHenteWebMarkersDone(boolean success){
        webServicesListener.onHenteWebMarkers(success);
    }
    public void onHenteWebMarkersEtterLagringDone(boolean success, WebMarker wm){
        webServicesListener.onHenteWebMarkersEtterLagring(success, wm);
    }
    public void onLagreWebMarkerDone(boolean success, WebMarker wm){
        webServicesListener.onLagreWebMarker(success, wm);
    }
    public void onSletteWebMarkerDone(boolean success, WebMarker wm){
        webServicesListener.onSletteWebMarker(success, wm);
    }
    public void onEndreWebMarkerDone(boolean success, WebMarker wm){
        webServicesListener.onEndreWebMarker(success, wm);
    }

    // LISTE METODER

    // Input er ID til en hentet MarkerPOJO. Hvis INGEN i webMarkerListen har denne IDen så betyr det at den er ny.
    public boolean sjekkOmWebMarkerEksistererIWebMarkerListen(int id){
        for(WebMarker wm : webMarkerListe){
            if(wm.getId() == id){
                return true;
            }
        }
        return false;
    }
    public WebMarker finnWebMarkerFraMarker(Marker m){
        // Log.d(TAG, m.getId());
        for(WebMarker wm : webMarkerListe){
            if(m.getId().equals(wm.getMarker().getId())){
                // Log.d(TAG, "finnWebMarkerFraMarker ~ fant en match! wm.getMarker().getId(): " + wm.getMarker().getId());
                return wm;
            }
        }
        return null;
    }
    public boolean slettWebMarkerFraListen(WebMarker slettetWebMarker){
        boolean fantOgSlettetWebMarker = false;
        for(int i = 0; i < webMarkerListe.size(); i++){
            if(webMarkerListe.get(i).getId() == slettetWebMarker.getId()){
                // Log.d(TAG, "slettWebMarkerFraListen ~ fant og slettet webMarker i webMarkerListen!");
                webMarkerListe.remove(i);
                return true;
            }
        }
        return fantOgSlettetWebMarker;
    }

    // CRUD METODER

    // HENTER OG LAGER MARKERS OG WEBMARKERS
    public void henteWebMarkers(GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {

            try {

                URL url = new URL("https://dave3600.cs.oslomet.no/~s349571/markers2_read.php");
                HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Accept", "application/json");
                httpUrlConnection.connect();
                int responseCode = httpUrlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpUrlConnection.getInputStream();
                    String response = readInputStreamToString(inputStream);
                    // Log.d(TAG, "henteWebMarkers ~ response: " + response);

                    Gson gson = new Gson();
                    ArrayList<MarkerPOJO> listFromGson = gson.fromJson(response,
                            new TypeToken<ArrayList<MarkerPOJO>>() {}.getType());

                    handler.post(() -> {
                        webMarkerListe.clear();
                        for(int i = 0; i < listFromGson.size(); i++){
                            // MarkerPOJO
                            MarkerPOJO markerPOJO = listFromGson.get(i);
                            // Marker
                            LatLng pos = new LatLng(
                                    Double.parseDouble(markerPOJO.getLatitude()),
                                    Double.parseDouble(markerPOJO.getLongitude())
                            );
                            Marker m = googleMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(markerPOJO.getNavn())
                                    .draggable(true)
                            );
                            // WebMarker
                            WebMarker wm = new WebMarker(markerPOJO, m);
                            webMarkerListe.add(wm);
                        }
                        handler.post(() -> {
                            // Log.d(TAG, "henteWebMarkers ~ success!");
                            onHenteWebMarkersDone(true);
                        });
                    });

                } else {
                    handler.post(() -> {
                        // Log.d(TAG, "henteWebMarkers ~ url feilet!");
                        onHenteWebMarkersDone(false);
                    });
                }

            } catch (Exception e) {
                handler.post(() -> {
                    // Log.d(TAG, "henteWebMarkers ~ caught exception! e: " + e);
                    onHenteWebMarkersDone(false);
                });
            }

        });
    }

    int lagretId = -1;
    // Kjøres etter lagring for å finne ID!
    public void hentWebMarkersEtterLagring(WebMarker webMarkerMedIdUt){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {

                URL url = new URL("https://dave3600.cs.oslomet.no/~s349571/markers2_read.php");
                HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Accept", "application/json");
                httpUrlConnection.connect();
                int responseCode = httpUrlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpUrlConnection.getInputStream();
                    String response = readInputStreamToString(inputStream);
                    // Log.d(TAG, "hentWebMarkersEtterLagring ~ response: " + response);

                    Gson gson = new Gson();
                    ArrayList<MarkerPOJO> listFromGson = gson.fromJson(response,
                            new TypeToken<ArrayList<MarkerPOJO>>() {}.getType());

                    handler.post(() -> {

                        lagretId = -1;
                        for(int i = 0; i < listFromGson.size(); i++){
                            // MarkerPOJO
                            MarkerPOJO markerPOJO = listFromGson.get(i);
                            int id = markerPOJO.getId();

                            // Ny løkke for å sjekke om en WebMarker eksisterer med dens ID.
                            boolean eksisterer = sjekkOmWebMarkerEksistererIWebMarkerListen(id);
                            if(!eksisterer){
                                // Hvis eksisterer returnerer false, betyr det at dette er den nye WebMarkeren.
                                // Log.d(TAG, "hentWebMarkersEtterLagring ~ success! Fant den nye IDen: " + id);
                                lagretId = id;
                                webMarkerMedIdUt.setId(id);
                                break;
                            }
                        }

                        if(lagretId != -1){
                            handler.post(() -> {
                                // Log.d(TAG, "hentWebMarkersEtterLagring ~ success!");
                                onHenteWebMarkersEtterLagringDone(true, webMarkerMedIdUt);
                            });
                        } else {
                            handler.post(() -> {
                                // Log.d(TAG, "hentWebMarkersEtterLagring ~ Fant ikke ID så returnerer false.");
                                onHenteWebMarkersEtterLagringDone(false, webMarkerMedIdUt);
                            });
                        }
                    });

                } else {
                    handler.post(() -> {
                        // Log.d(TAG, "hentWebMarkersEtterLagring ~ url feilet!");
                        onHenteWebMarkersEtterLagringDone(false, webMarkerMedIdUt);
                    });
                }

            } catch (Exception e) {
                handler.post(() -> {
                    // Log.d(TAG, "hentWebMarkersEtterLagring ~ caught exception! e: " + e);
                    onHenteWebMarkersEtterLagringDone(false, webMarkerMedIdUt);
                });
            }

        });
    }

    // LAGRE
    public void lagreWebMarker(WebMarker wmarker) {
        LatLng pos = wmarker.getMarker().getPosition();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {

                String builtUrl = "https://dave3600.cs.oslomet.no/~s349571/markers2_create.php?" +
                        "navn=" + wmarker.getNavn() +
                        "&addresse=" + wmarker.getAddresse() +
                        "&liker=" + wmarker.getLiker() +
                        "&beskrivelse=" + wmarker.getBeskrivelse() +
                        "&latitude=" + pos.latitude +
                        "&longitude=" + pos.longitude;
                URL url = new URL(builtUrl);
                HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Accept", "application/json");
                httpUrlConnection.connect();
                int responseCode = httpUrlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    handler.post(() -> {
                        // Log.d(TAG, "lagreWebMarker ~ success!");
                        onLagreWebMarkerDone(true, wmarker);
                    });
                } else {
                    handler.post(() -> {
                        // Log.d(TAG, "lagreWebMarker ~ url feilet!");
                        onLagreWebMarkerDone(false, wmarker);
                    });
                }

            } catch (Exception e) {
                handler.post(() -> {
                    // Log.d(TAG, "lagreWebMarker ~ caught exception! e: " + e);
                    onLagreWebMarkerDone(false, wmarker);
                });

            }

        });
    }

    // ENDRE
    public void endreWebMarker(WebMarker wmarker) {
        LatLng pos = wmarker.getMarker().getPosition();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {

                String builtUrl = "https://dave3600.cs.oslomet.no/~s349571/markers2_update.php?" +
                        "navn=" + wmarker.getNavn() +
                        "&addresse=" + wmarker.getAddresse() +
                        "&liker=" + wmarker.getLiker() +
                        "&beskrivelse=" + wmarker.getBeskrivelse() +
                        "&latitude=" + pos.latitude +
                        "&longitude=" + pos.longitude +
                        "&id=" + wmarker.getId();

                URL url = new URL(builtUrl);

                HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Accept", "application/json");
                httpUrlConnection.connect();
                int responseCode = httpUrlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    handler.post(() -> {
                        Log.d(TAG, "endreWebMarker ~ successful!");
                        onEndreWebMarkerDone(true, wmarker);
                    });
                } else {
                    handler.post(() -> {
                        // Log.d(TAG, "endreWebMarker ~ url feilet!");
                        onEndreWebMarkerDone(false, wmarker);
                    });
                }

            } catch (Exception e) {
                handler.post(() -> {
                    // Log.d(TAG, "endreWebMarker ~ caught exception! e: " + e);
                    onEndreWebMarkerDone(false, wmarker);
                });
            }

        });
    }

    // SLETT
    public void slettWebMarker(WebMarker marker){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {

                String builtUrl = "https://dave3600.cs.oslomet.no/~s349571/markers2_delete.php?id=" + marker.getId();

                URL url = new URL(builtUrl);

                HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Accept", "application/json");
                httpUrlConnection.connect();
                int responseCode = httpUrlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    handler.post(() -> {
                        // Log.d(TAG, "slettWebMarker ~ successful!");
                        onSletteWebMarkerDone(true, marker);
                    });
                } else {
                    handler.post(() -> {
                        // Log.d(TAG, "slettWebMarker ~ feilet?!");
                        onSletteWebMarkerDone(false, marker);
                    });
                }

            } catch (Exception e) {
                handler.post(() -> {
                    // Log.d(TAG, "slettWebMarker ~ caught exception! e: " + e);
                    onSletteWebMarkerDone(false, marker);
                });
            }

        });
    }

    // Metode fra notater (slides) for faget
    private String readInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }

}
