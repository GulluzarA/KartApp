package com.s349571s322911.kartappen;

public class MarkerPOJO {
    private int id = -1;
    private String navn = "";
    private String addresse = "";
    private String liker = "";
    private String beskrivelse = "";
    private String latitude = "";
    private String longitude = "";

    MarkerPOJO(){}
    MarkerPOJO(String navn, String addresse, String liker, String beskrivelse, String latitude, String longitude){
        this.navn = navn;
        this.addresse = addresse;
        this.liker = liker;
        this.beskrivelse = beskrivelse;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    MarkerPOJO(int id, String navn, String addresse, String liker, String beskrivelse, String latitude, String longitude){
        this.id = id;
        this.navn = navn;
        this.addresse = addresse;
        this.liker = liker;
        this.beskrivelse = beskrivelse;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNavn() {
        return navn;
    }
    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getAddresse() {
        return addresse;
    }
    public void setAddresse(String addresse) {
        this.addresse = addresse;
    }

    public String getLiker() {
        return liker;
    }
    public void setLiker(String liker) {
        this.liker = liker;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
