package com.s349571s322911.kartappen;

import com.google.android.gms.maps.model.Marker;

public class WebMarker {

    private MarkerPOJO markerPOJO = null;
    private Marker marker = null;

    WebMarker(MarkerPOJO markerPOJO, Marker marker){
        this.markerPOJO = markerPOJO;
        this.marker = marker;
    }

    public MarkerPOJO getMarkerPOJO() {
        return markerPOJO;
    }
    public void setMarkerPOJO(MarkerPOJO markerPOJO) {
        this.markerPOJO = markerPOJO;
    }

    public Marker getMarker() {
        return marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getId() {
        return markerPOJO.getId();
    }
    public void setId(int id) {
        this.markerPOJO.setId(id);
    }

    public String getNavn() {
        return this.markerPOJO.getNavn();
    }
    public void setNavn(String navn) {
        this.markerPOJO.setNavn(navn);
    }

    public String getAddresse() {
        return this.markerPOJO.getAddresse();
    }
    public void setAddresse(String addresse) {
        this.markerPOJO.setAddresse(addresse);
    }

    public String getLiker() {
        return this.markerPOJO.getLiker();
    }
    public void setLiker(String liker) {
        this.markerPOJO.setLiker(liker);
    }

    public String getBeskrivelse() {
        return this.markerPOJO.getBeskrivelse();
    }
    public void setBeskrivelse(String beskrivelse) {
        this.markerPOJO.setBeskrivelse(beskrivelse);
    }

    public String getLatitude() {
        return this.markerPOJO.getLatitude();
    }
    public void setLatitude(String latitude) {
        this.markerPOJO.setLatitude(latitude);
    }

    public String getLongitude() {
        return this.markerPOJO.getLongitude();
    }
    public void setLongitude(String longitude) {
        this.markerPOJO.setLongitude(longitude);
    }

}
