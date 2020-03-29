package com.example.standapp;

/**
 * The Dashboard item is a class consisting of the icon,
 * title and price of the snack within in the menu (e.g., Pizza with price of 3 euros etc.)
 */
class DashboardItem {
    private int icon;
    private String title;
    private String price;
    private String prep_time;
    private String count;
    private String category;
    private String description;

    DashboardItem(int icon, String title, String price, String preptime, String count, String category, String description) {
        this.icon = icon; //thumbnail for item
        this.title = title; //name of item
        this.price = price; //price of item
        this.prep_time = preptime; //preparation time of item
        this.count = count; //amount of items in stock (count=-1 if no items in stock)
        this.category = category; //category that the item belongs to
        this.description = description; //description of the item
    }

    DashboardItem(DashboardItem item) {
        this.icon = item.icon;
        this.title = item.title;
        this.price = item.price;
        this.prep_time = item.prep_time;
        this.count = item.count;
        this.category = item.category;
        this.description = item.description;
    }

    int getIcon() {
        return icon;
    }

    void setIcon(int icon) {
        this.icon = icon;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getPrice() {
        return price;
    }

    void setPrice(String price) {
        this.price = price;
    }

    String getPrep_time() {
        return prep_time;
    }

    void setPrep_time(String prep_time) {
        this.prep_time = prep_time;
    }

    String getCount() {
        return count;
    }

    void setCount(String count) {
        this.count = count;
    }

    String getCategory() {
        return category;
    }

    void setCategory(String category) {
        this.category = category;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }
}