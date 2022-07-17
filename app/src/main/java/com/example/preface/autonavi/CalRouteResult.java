package com.example.preface.autonavi;

public class CalRouteResult {
    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    private int routeId;
    private int distance;
    private int time;
    private String label;
    private int lights;

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    private int cost;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLights() {
        return lights;
    }

    public void setLights(int lights) {
        lights = lights;
    }

    public CalRouteResult(int routeId, int distance, int time, int lights, String label, int cost){
        this.routeId = routeId;
        this.distance = distance;
        this.time = time;
        this.lights = lights;
        this.label = label;
        this.cost = cost;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
