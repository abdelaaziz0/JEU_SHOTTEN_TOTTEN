package com.schottenTotten.model;

public class Carte {
    private String couleurCarte;
    private int carteNum;

    public Carte(int carteNum, String couleurCarte) {
        this.couleurCarte = couleurCarte;
        this.carteNum = carteNum;
    }

    public String getCouleurCarte() {
        return couleurCarte;
    }

    public int getCarteNum() {
        return carteNum;
    }
}
