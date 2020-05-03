package com.geoape.backgroundlocationexample;

public class GPS {
    double latitude;
    double longitube;
    String locationName;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitube() {
        return longitube;
    }

    public void setLongitube(double longitube) {
        this.longitube = longitube;
    }
}
