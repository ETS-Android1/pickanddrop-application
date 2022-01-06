package uk.ac.tees.aad.W9517102.Common;

public class History {
    private String driveName;
    private String customerName;
    private String date;
    private Double latitudeStart;
    private Double longitudeStart;
    private Double latitudeEnd;
    private Double longitudeEnd;

    public History() {
    }

    public History(String driveName, String customerName, String date, Double latitudeStart, Double longitudeStart, Double latitudeEnd, Double longitudeEnd) {
        this.driveName = driveName;
        this.customerName = customerName;
        this.date = date;
        this.latitudeStart = latitudeStart;
        this.longitudeStart = longitudeStart;
        this.latitudeEnd = latitudeEnd;
        this.longitudeEnd = longitudeEnd;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDriveName() {
        return driveName;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getLatitudeStart() {
        return latitudeStart;
    }

    public void setLatitudeStart(Double latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    public Double getLongitudeStart() {
        return longitudeStart;
    }

    public void setLongitudeStart(Double longitudeStart) {
        this.longitudeStart = longitudeStart;
    }

    public Double getLatitudeEnd() {
        return latitudeEnd;
    }

    public void setLatitudeEnd(Double latitudeEnd) {
        this.latitudeEnd = latitudeEnd;
    }

    public Double getLongitudeEnd() {
        return longitudeEnd;
    }

    public void setLongitudeEnd(Double longitudeEnd) {
        this.longitudeEnd = longitudeEnd;
    }
}
