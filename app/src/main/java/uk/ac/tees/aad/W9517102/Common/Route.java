package uk.ac.tees.aad.W9517102.Common;

public class Route {
  private Double targetLocationLat;
  private Double targetLocationLong;
  private String driverId;
  private String customerId;

    public Route(Double targetLocationLat, Double targetLocationLong, String driverId, String customerId) {
        this.targetLocationLat = targetLocationLat;
        this.targetLocationLong = targetLocationLong;
        this.driverId = driverId;
        this.customerId = customerId;
    }

    public Route() {
    }

    public Double getTargetLocationLat() {
        return targetLocationLat;
    }

    public void setTargetLocationLat(Double targetLocationLat) {
        this.targetLocationLat = targetLocationLat;
    }

    public Double getTargetLocationLong() {
        return targetLocationLong;
    }

    public void setTargetLocationLong(Double targetLocationLong) {
        this.targetLocationLong = targetLocationLong;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
