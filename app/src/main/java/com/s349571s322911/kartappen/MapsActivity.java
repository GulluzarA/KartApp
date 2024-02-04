package com.s349571s322911.kartappen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.s349571s322911.kartappen.databinding.ActivityMapsBinding;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        WebServices.WebServicesListener,
        OnMapReadyCallback,
        OnMapClickListener,
        OnMapLongClickListener,
        OnMarkerClickListener,
        OnMarkerDragListener,
        OnInfoWindowClickListener,
        OnInfoWindowLongClickListener,
        OnInfoWindowCloseListener {

    App application;
    WebServices webServices;
    public static final String TAG = "Erlend";
    private GoogleMap mMap;
    LayoutInflater inflater;
    AlertDialog alertDialogMarkerInfo;
    AlertDialog alertDialogMarkerInfoEdit;
    AlertDialog alertDialogBekreftSlett;
    ImageButton editDelete;
    EditText editNavn, editAdresse, editLiker, editBeskrivelse;
    TextView textNavn,textAdresse, textLiker, textBeskrivelse;
    WebMarker selectedWebMarker, temporaryWebMarker, sentWebMarker;
    MarkerPOJO temporaryWebMarkerPOJO;
    Boolean selectedIsInDatabase;
    ArrayList<WebMarker> temporaryWebMarkerListe = new ArrayList<>();

     void getApplicationClass(){
            application = (App) getApplicationContext();
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getApplicationClass();
        webServices = new WebServices(this);

        com.s349571s322911.kartappen.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        inflater = this.getLayoutInflater(); // For AlertDialogene (inflate layouts).

        // Lage alertDialogMarkerInfo.
        lagAlertDialogMarkerInfo();
        lagAlertDialogMarkerInfoEdit();
        lagAlertDialogBekreftSletting();
    }

    // Callback når kartet er klart/hentet
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        // Lager midlertidig WebMarker for nye markører:
        lagSentWebMarker();
        lagTemporaryWebMarker();

        mMap.setContentDescription(getString(R.string.map_description));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(58.8935453576277,0.7487007603049278)));

        // Hent WebMarkers:
        webServices.henteWebMarkers(mMap);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Log.d(TAG, "OnMapClick ~ point: " + latLng + ", lat: " + latLng.latitude + ", long: " + latLng.longitude);
    }
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        // Log.d(TAG, "onMapLongClick ~ point: " + latLng + ", lat: " + latLng.latitude + ", long: " + latLng.longitude);
        lagNyMarker(latLng);
    }
    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {

        WebMarker wm = webServices.finnWebMarkerFraMarker(marker);
        if(wm != null){
            selectedWebMarker = wm;
            selectedIsInDatabase = true;

        } else {
            selectedWebMarker = finnTemporaryWebMarkerMedMarker(marker);
            selectedIsInDatabase = false;
        }

        if(selectedWebMarker != null){
            settMarkerInfoInnhold(selectedWebMarker);
            settMarkerInfoEditInnhold(selectedWebMarker);
        }

        if(wm != null){
            visAlertDialogMarkerInfo();
        } else {
            visAlertDialogMarkerInfoEdit();
        }

        return false;
    }
    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }
    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {

    }
    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }
    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {

    }
    @Override
    public void onInfoWindowLongClick(@NonNull Marker marker) {

    }

    void lagSentWebMarker(){
        MarkerPOJO mp = new MarkerPOJO(getString(R.string.nytt_sted), "", "", "", "", "");
        sentWebMarker = new WebMarker(mp, null);
    }
    void lagTemporaryWebMarker(){
        temporaryWebMarkerPOJO = new MarkerPOJO(getString(R.string.nytt_sted), "", "", "", "", "");
        temporaryWebMarker = new WebMarker(temporaryWebMarkerPOJO, null);
    }
    void tilbakestillTemporaryWebMarker(){
        temporaryWebMarker.setId(-1);
        temporaryWebMarker.setNavn(getString(R.string.nytt_sted));
        temporaryWebMarker.setAddresse("");
        temporaryWebMarker.setLiker("");
        temporaryWebMarker.setBeskrivelse("");
        temporaryWebMarker.setLatitude("");
        temporaryWebMarker.setLongitude("");
    }

    private void lagNyMarker(LatLng latLng){
        tilbakestillTemporaryWebMarker();
        Marker m = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.nytt_sted))
                .draggable(true));
        temporaryWebMarker.setMarker(m);
        // LISTE FOR TEMPORARY WEB MARKERS (For å kunne finne og slette dem)
        MarkerPOJO mp = new MarkerPOJO(getString(R.string.nytt_sted), "", "", "", "", "");
        WebMarker nyWebMarker = new WebMarker(mp, m);
        temporaryWebMarkerListe.add(nyWebMarker);
    }

    void settMarkerInfoInnhold(WebMarker wm){
        textNavn.setText(wm.getNavn());
        textAdresse.setText(wm.getAddresse());
        textLiker.setText(wm.getLiker());
        textBeskrivelse.setText(wm.getBeskrivelse());
    }
    void settMarkerInfoEditInnhold(WebMarker wm){
        editNavn.setText(wm.getNavn());
        editAdresse.setText(wm.getAddresse());
        editLiker.setText(wm.getLiker());
        editBeskrivelse.setText(wm.getBeskrivelse());
    }

    void lagAlertDialogMarkerInfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setCancelable(true);

        // Custom layout.
        View dialogImageView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogImageView);

        textNavn = dialogImageView.findViewById(R.id.marker_navn);
        textAdresse = dialogImageView.findViewById(R.id.marker_addresse);
        textLiker = dialogImageView.findViewById(R.id.marker_liker);
        textBeskrivelse = dialogImageView.findViewById(R.id.marker_beskrivelse);

        builder.setPositiveButton(
                getString(R.string.endre),
                (dialog, id) -> {
                    // dialog.cancel();
                    visAlertDialogMarkerInfoEdit();
                });
        builder.setNegativeButton(
                getString(R.string.lukk),
                (dialog, id) -> dialog.cancel());

        alertDialogMarkerInfo = builder.create();

        // Plassere på bunnen av skjermen:
        Window window = alertDialogMarkerInfo.getWindow();
        assert window != null;
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
    void visAlertDialogMarkerInfo(){
        alertDialogMarkerInfo.show();
    }
    void skjulAlertDialogMarkerInfo(){
        alertDialogMarkerInfo.hide();
    }

    void lagAlertDialogMarkerInfoEdit(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setCancelable(true);
        builder.setPositiveButton(
                getString(R.string.lagre),
                (dialog, id) -> {
                    if(!selectedIsInDatabase){
                        lagre();
                    } else {
                        update();
                    }
                    visAlertDialogMarkerInfo();
                });
        builder.setNegativeButton(
                getString(R.string.avbryt),
                (dialog, id) -> visAlertDialogMarkerInfo()
        );

        // Custom layout.
        View dialogImageView = inflater.inflate(R.layout.dialog_layout_edit, null);
        builder.setView(dialogImageView);

        editDelete = dialogImageView.findViewById(R.id.marker_delete);
        editNavn = dialogImageView.findViewById(R.id.marker_navn_edit);
        editAdresse = dialogImageView.findViewById(R.id.marker_addresse_edit);
        editLiker = dialogImageView.findViewById(R.id.marker_liker_edit);
        editBeskrivelse = dialogImageView.findViewById(R.id.marker_beskrivelse_edit);

        // FOR DELETE
        editDelete.setOnClickListener(v -> visAlertDialogBekreftSlett());

        alertDialogMarkerInfoEdit = builder.create();

        // Plassere på bunnen av skjermen:
        Window window = alertDialogMarkerInfoEdit.getWindow();
        assert window != null;
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

    void visAlertDialogMarkerInfoEdit(){
        alertDialogMarkerInfoEdit.show();
    }
    void skjulAlertDialogMarkerInfoEdit(){
        alertDialogMarkerInfoEdit.hide();
    }

    void settSelectedWebMarkerInfo(){
        sentWebMarker.setMarker(selectedWebMarker.getMarker());
        sentWebMarker.setId(selectedWebMarker.getId());
        sentWebMarker.setNavn(editNavn.getText().toString());
        sentWebMarker.setAddresse(editAdresse.getText().toString());
        sentWebMarker.setLiker(editLiker.getText().toString());
        sentWebMarker.setBeskrivelse(editBeskrivelse.getText().toString());
        sentWebMarker.setLatitude(selectedWebMarker.getLatitude());
        sentWebMarker.setLongitude(selectedWebMarker.getLongitude());
        if(sentWebMarker.getMarker() != null){
            sentWebMarker.getMarker().setTitle(editNavn.getText().toString());
        }
    }

    void lagAlertDialogBekreftSletting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.delete);
        builder.setTitle(getString(R.string.bekreft_sletting));
        builder.setMessage(getString(R.string.bekreft_sletting_melding));

        builder.setPositiveButton(
                getString(R.string.ja),
                (dialog, id) -> delete());
        builder.setNegativeButton(
                getString(R.string.nei),
                (dialog, id) -> visAlertDialogMarkerInfo());

        alertDialogBekreftSlett = builder.create();

        // Plassere på bunnen av skjermen:
        Window window = alertDialogBekreftSlett.getWindow();
        assert window != null;
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
    void visAlertDialogBekreftSlett(){
         alertDialogBekreftSlett.show();
    }
    void skjulAlertDialogBekreftSlett(){
         alertDialogBekreftSlett.hide();
    }

    void lagre(){
        settSelectedWebMarkerInfo();
        webServices.lagreWebMarker(sentWebMarker);
    }
    void update(){
        settSelectedWebMarkerInfo();
        webServices.endreWebMarker(sentWebMarker);
    }
    void delete(){
        if(selectedIsInDatabase){
            webServices.slettWebMarker(selectedWebMarker);
        } else {
            slettTemporaryWebMarkerMedMarker(selectedWebMarker.getMarker());
            selectedWebMarker.getMarker().remove();
            skjulAlertDialogMarkerInfo();
            skjulAlertDialogMarkerInfoEdit();
            skjulAlertDialogBekreftSlett();
        }
    }

    // BRUKE DENNE TIL Å SLETTE MIDLERTIDIGE WEBMARKERS fra temporaryWebMarkerListe som ikke er lagret enda med webtjeneste.
    boolean slettTemporaryWebMarkerMedMarker(Marker marker){
        boolean fantTempMarkerMedSammeId = false;
        for(int i = 0; i < temporaryWebMarkerListe.size(); i++){
            if(marker.getId().equals(temporaryWebMarkerListe.get(i).getMarker().getId())){
                // Log.d(TAG, "slettTemporaryWebMarkerMedMarker ~ fant og slettet tempWebMarker fra lista!");
                temporaryWebMarkerListe.remove(i);
                fantTempMarkerMedSammeId = true;
                break;
            }
        }
        return fantTempMarkerMedSammeId;// Hvis ingen temporaryWebMarker har marker med samme ID som marker returneres false.
    }

    WebMarker finnTemporaryWebMarkerMedMarker(Marker marker){
        for(WebMarker wm : temporaryWebMarkerListe){
            if(marker.getId().equals(wm.getMarker().getId())){
                // Log.d(TAG, "finnTemporaryWebMarkerMedMarker ~ fant tempWebMarker i lista!");
                return wm;
            }
        }
        return null;
    }

    // CALLBACKS FRA WEBSERVICES

    @Override
    public void onHenteMarkerPOJOs(boolean success) {
        // Log.d(TAG, "onHenteMarkerPOJOs ~ success: " + success);
    }
    @Override
    public void onHenteWebMarkers(boolean success) {
        // Log.d(TAG, "onHenteWebMarkers ~ success: " + success);
    }
    @Override
    public void onHenteWebMarkersEtterLagring(boolean success, WebMarker webMarkerMedIdUt) {
        // Log.d(TAG, "onHenteWebMarkersEtterLagring ~ success: " + success);
        if(success){
            // Slette dets midlertidige WebMarker fra temporaryWebMarkerListe!
            slettTemporaryWebMarkerMedMarker(webMarkerMedIdUt.getMarker());
            // Log.d(TAG, "Fant temporaryWebMarker og slettet den: " + result);

            selectedIsInDatabase = true;
            settMarkerInfoInnhold(webMarkerMedIdUt);
            settMarkerInfoEditInnhold(webMarkerMedIdUt);
            webServices.webMarkerListe.add(webMarkerMedIdUt);
            // Oppdaterer selectedWebMarker data.
            selectedWebMarker.setMarker(webMarkerMedIdUt.getMarker());
            selectedWebMarker.setId(webMarkerMedIdUt.getId());
            selectedWebMarker.setNavn(webMarkerMedIdUt.getNavn());
            selectedWebMarker.setAddresse(webMarkerMedIdUt.getAddresse());
            selectedWebMarker.setLiker(webMarkerMedIdUt.getLiker());
            selectedWebMarker.setBeskrivelse(webMarkerMedIdUt.getBeskrivelse());
            selectedWebMarker.setLatitude(webMarkerMedIdUt.getLatitude());
            selectedWebMarker.setLongitude(webMarkerMedIdUt.getLongitude());
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.feilmelding_hente_etter_lagring), Toast.LENGTH_LONG).show());
        }
    }
    @Override
    public void onLagreWebMarker(boolean success, WebMarker wm) {
        // Log.d(TAG, "onLagreWebMarker ~ success: " + success);
        if(success){
            webServices.hentWebMarkersEtterLagring(wm);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.feilmelding_lagring), Toast.LENGTH_LONG).show());
        }
    }
    @Override
    public void onEndreWebMarker(boolean success, WebMarker wm) {
        // Log.d(TAG, "onEndreWebMarker ~ success: " + success);
        if(success){
            selectedWebMarker = wm;
            selectedIsInDatabase = true;
            settMarkerInfoInnhold(wm);
            settMarkerInfoEditInnhold(wm);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.feilmedling_endring), Toast.LENGTH_LONG).show());
        }
    }
    @Override
    public void onSletteWebMarker(boolean success, WebMarker wm) {
        // Log.d(TAG, "onSletteWebMarker ~ success: " + success);
        if(success){
            webServices.slettWebMarkerFraListen(wm);
            wm.getMarker().remove();
            skjulAlertDialogMarkerInfo();
            skjulAlertDialogMarkerInfoEdit();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.feilmelding_sletting), Toast.LENGTH_LONG).show());
        }
    }
}