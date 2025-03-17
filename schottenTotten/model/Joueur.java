package com.schottenTotten.model;

import java.util.ArrayList;
import java.util.List;

interface GestionMain {
    void ajouterCarte(Carte carte);
    void retirerCarte(Carte carte);
    boolean contientCarte(Carte carte);
    List<Carte> getMain();
}

interface GestionBornes {
    void ajouterBorneGagnee(Borne borne);
    List<Borne> getBornesGagnees();
}

interface GestionTour {
    boolean estMonTour();
    void setTour(boolean isTour);
}

class MainJoueur implements GestionMain {
    private final List<Carte> cartes;

    public MainJoueur(List<Carte> cartesInitiales) {
        this.cartes = cartesInitiales != null ? cartesInitiales : new ArrayList<>();
    }

    @Override
    public void ajouterCarte(Carte carte) {
        cartes.add(carte);
    }

    @Override
    public void retirerCarte(Carte carte) {
        cartes.remove(carte);
    }

    @Override
    public boolean contientCarte(Carte carte) {
        return cartes.contains(carte);
    }

    @Override
    public List<Carte> getMain() {
        return cartes;
    }
}

class GestionnaireBornes implements GestionBornes {
    private final List<Borne> bornesGagnees;

    public GestionnaireBornes(List<Borne> bornesInitiales) {
        this.bornesGagnees = bornesInitiales != null ? bornesInitiales : new ArrayList<>();
    }

    @Override
    public void ajouterBorneGagnee(Borne borne) {
        bornesGagnees.add(borne);
    }

    @Override
    public List<Borne> getBornesGagnees() {
        return bornesGagnees;
    }
}

public class Joueur {
    private final int numJoueur;
    private final GestionMain gestionMain;
    private final GestionBornes gestionBornes;
    private boolean isTour;

    public Joueur(int numJoueur, List<Carte> mainJoueur, List<Borne> bornesGagnees) {
        this.numJoueur = numJoueur;
        this.gestionMain = new MainJoueur(mainJoueur);
        this.gestionBornes = new GestionnaireBornes(bornesGagnees);
        this.isTour = false;
    }

    public void jouerCarte(Borne borne, Carte carte, Pioche pioche) {
        if (gestionMain.contientCarte(carte)) {
            gestionMain.retirerCarte(carte);
            borne.ajouterCarte(carte, this.numJoueur);
            gestionMain.ajouterCarte(pioche.piocher());
        } else {
            System.out.println("La carte n'est pas dans la main du joueur.");
        }
    }

    public void ajouterCarteMain(Carte carte) {
        gestionMain.ajouterCarte(carte);
    }

    public void addBorneGagnee(Borne borne) {
        gestionBornes.ajouterBorneGagnee(borne);
    }

    public boolean isTour() {
        return isTour;
    }

    public void setTour(boolean isTour) {
        this.isTour = isTour;
    }

    public int getNumJoueur() {
        return numJoueur;
    }

    public List<Carte> getMainJoueur() {
        return gestionMain.getMain();
    }

    public List<Borne> getBornesGagnees() {
        return gestionBornes.getBornesGagnees();
    }
}