package main.my.projects.java;

public class FederalDTO {
    String storeName;
    String storeDistrict;
    String storeAddress;
    String drugName;
    String actualDate;
    int federalCount;

    public FederalDTO(String storeName, String storeDistrict, String storeAddress, String drugName, String actualDate, int federalCount) {
        this.storeName = storeName;
        this.storeDistrict = storeDistrict;
        this.storeAddress = storeAddress;
        this.drugName = drugName;
        this.actualDate = actualDate;
        this.federalCount = federalCount;
    }

    @Override
    public String toString() {
        return storeName + " " + storeDistrict + '\n' +
                storeAddress + "\n" +
                drugName + "\n" +
                actualDate + "\n" +
                federalCount + " шт\n";
    }
}