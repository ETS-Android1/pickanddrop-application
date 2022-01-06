package uk.ac.tees.aad.W9517102.Common;

import uk.ac.tees.aad.W9517102.Customer.CustomerHomeActivity;

public class Request {
    private String customerID;
    private String customerName;
    private String driverID;
    private String driverName;
    private String status;

    public Request(String customerID, String customerName, String driverID, String driverName, String status) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.driverID = driverID;
        this.driverName = driverName;
        this.status = status;
    }

    public Request() {
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
