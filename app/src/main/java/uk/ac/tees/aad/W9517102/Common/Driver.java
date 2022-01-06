package uk.ac.tees.aad.W9517102.Common;

public class Driver {
    private String image;
    private String phone;
    private String status;
    private String userId;
    private String userType;
    private String username;
    private Double latitude;
    private Double longitude;

    public Driver(String image, String phone, String status, String userId, String userType, String username, Double latitude, Double longitude) {
        this.image = image;
        this.phone = phone;
        this.status = status;
        this.userId = userId;
        this.userType = userType;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Driver() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
