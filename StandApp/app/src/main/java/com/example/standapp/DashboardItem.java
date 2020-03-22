package com.example.standapp;

/**
 * The Dashboard item is a class consisting of the icon, title and price of the snack within in the menu (e.g., Pizza with price of 3 euros etc.)
 */
public class DashboardItem {
    public int icon;
    public String title;
    public String price;
    public String preptime;
    public String count;
    public String category;
    public String description;

    DashboardItem(int icon, String title, String price, String preptime, String count, String category, String description) {
        this.icon = icon; //thumbnail for item
        this.title = title; //name of item
        this.price = price; //price of item
        this.preptime = preptime; //preparation time of item
        this.count = count; //amount of items in stock (count=-1 if no items in stock)
        this.category = category; //category that the item belongs to
        this.description = description; //description of the item
    }

    public DashboardItem(DashboardItem item) {
        this.icon = item.icon;
        this.title = item.title;
        this.price = item.price;
        this.preptime = item.preptime;
        this.count = item.count;
        this.category = item.category;
        this.description = item.description;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPreptime() {
        return preptime;
    }

    public void setPreptime(String preptime) {
        this.preptime = preptime;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}