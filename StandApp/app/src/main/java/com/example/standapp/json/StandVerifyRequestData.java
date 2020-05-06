package com.example.standapp.json;

public class StandVerifyRequestData {

    private String standName;
    private String brandName;

    public StandVerifyRequestData(String standName, String brandName) {
        this.standName = standName;
        this.brandName = brandName;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

}
