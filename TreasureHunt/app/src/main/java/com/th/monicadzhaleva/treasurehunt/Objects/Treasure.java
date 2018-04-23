package com.th.monicadzhaleva.treasurehunt;

/**
 * Created by monicadzhaleva on 19/10/2017.
 */

public class Treasure {

    private int id;
    private String name;
    private String type;
    private String category;
    private String info;
    private int points;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String owner;
    private String ownerClan;

    public Treasure()
    {

    }
    public Treasure(int id, String name, String type, String category, String info, int points, double latitude, double longitude, String owner) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category=category;
        this.info = info;
        this.points = points;
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner=owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerClan() {
        return ownerClan;
    }

    public void setOwnerClan(String ownerClan) {
        this.ownerClan = ownerClan;
    }
}
