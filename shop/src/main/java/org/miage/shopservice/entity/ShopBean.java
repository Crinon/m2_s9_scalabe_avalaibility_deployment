package org.miage.shopservice.entity;

import java.math.BigDecimal;

public class ShopBean {

    private String id;
    private String source;
    private String cible;
    private Double price;

    public ShopBean() {
        // JPA
    }

    public ShopBean(String id, String source, String cible, Double price) {
        super();
        this.id = id;
        this.source = source;
        this.cible = cible;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCible() {
        return cible;
    }

    public void setCible(String cible) {
        this.cible = cible;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}
