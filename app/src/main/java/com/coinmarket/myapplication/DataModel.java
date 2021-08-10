package com.coinmarket.myapplication;

public class DataModel {

    String name;
    String symbol;
    String platform;
    String address;
    String price;


    public DataModel(String name, String symbol, String platform, String address, String price) {
        this.name = name;
        this.symbol = symbol;
        this.platform = platform;
        this.address = address;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAddress() {
        return address;
    }

    public String getPrice() {
        return price;
    }

}
