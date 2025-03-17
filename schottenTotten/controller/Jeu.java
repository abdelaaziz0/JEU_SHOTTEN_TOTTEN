package com.schottenTotten.controller;

import com.schottenTotten.model.*;
import com.schottenTotten.ai.*;
import java.util.ArrayList;
import java.util.List;

interface GestionJoueurs {
    void distribuerCartes(Pioche pioche);
    void jouerTour(Joueur joueur, int indiceBorne, Carte carte, List<Borne> bornes, Pioche pioche);
    void mettreAJourBornesGagnees(List<Borne> bornes);
}

interface VerificationVictoire {
    boolean verifierVictoire(List<Borne> bornes);
    boolean verifierVictoireParSequence(List<Borne> bornes);
    boolean verifierVictoireParNombreBornes(Joueur joueur1, Joueur joueur2);
}

class GestionnaireJeu implements GestionJoueurs, VerificationVictoire {
    private final Joueur joueur1;
    private final Joueur joueur2;

    public GestionnaireJeu(Joueur joueur1, Joueur joueur2) {
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
    }

    @Override
    public void distribuerCartes(Pioche pioche) {
        for (int i = 0; i < 6; i++) {
            joueur1.ajouterCarteMain(pioche.piocher());
            joueur2.ajouterCarteMain(pioche.piocher());
        }
    }

    @Override
    public void jouerTour(Joueur joueur, int indiceBorne, Carte carte, List<Borne> bornes, Pioche pioche) {
        validerCoup(indiceBorne, bornes, joueur);
        joueur.jouerCarte(bornes.get(indiceBorne), carte, pioche);
        mettreAJourBornesGagnees(bornes);
    }

    private void validerCoup(int indiceBorne, List<Borne> bornes, Joueur joueur) {
        if (indiceBorne < 0 || indiceBorne >= bornes.size()) {
            throw new IllegalArgumentException("Indice de borne invalide");
        }

        Borne borne = bornes.get(indiceBorne);
        if (borne.isControlee()) {
            throw new IllegalStateException("Cette borne est déjà contrôlée");
        }

        List<Carte> mainSurBorne = joueur.getNumJoueur() == 1 ? 
            borne.getMainJoueur1() : borne.getMainJoueur2();
            
        if (mainSurBorne.size() >= 3) {
            throw new IllegalStateException("Cette borne a déjà 3 cartes");
        }
    }

    @Override
    public void mettreAJourBornesGagnees(List<Borne> bornes) {
        joueur1.getBornesGagnees().clear();
        joueur2.getBornesGagnees().clear();

        for (Borne borne : bornes) {
            if (borne.isControlee()) {
                int resultat = borne.comparerMains();
                if (resultat > 0) joueur1.addBorneGagnee(borne);
                else if (resultat < 0) joueur2.addBorneGagnee(borne);
            }
        }
    }

    @Override
    public boolean verifierVictoire(List<Borne> bornes) {
        mettreAJourBornesGagnees(bornes);
        return verifierVictoireParSequence(bornes) || 
               verifierVictoireParNombreBornes(joueur1, joueur2);
    }

    @Override
    public boolean verifierVictoireParSequence(List<Borne> bornes) {
        for (int i = 0; i < bornes.size() - 2; i++) {
            Borne b1 = bornes.get(i);
            Borne b2 = bornes.get(i + 1);
            Borne b3 = bornes.get(i + 2);
            
            if (b1.isControlee() && b2.isControlee() && b3.isControlee()) {
                int r1 = b1.comparerMains();
                int r2 = b2.comparerMains();
                int r3 = b3.comparerMains();
                
                if ((r1 > 0 && r2 > 0 && r3 > 0) || (r1 < 0 && r2 < 0 && r3 < 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean verifierVictoireParNombreBornes(Joueur joueur1, Joueur joueur2) {
        return joueur1.getBornesGagnees().size() >= 5 || 
               joueur2.getBornesGagnees().size() >= 5;
    }
}

public class Jeu {
    private final Joueur joueur1;
    private final Joueur joueur2;
    private final Pioche pioche;
    private final List<Borne> bornes;
    private final GestionnaireJeu gestionnaireJeu;

    public Jeu(int choixAdversaire) {
        this.pioche = new Pioche();
        this.joueur1 = new Joueur(1, new ArrayList<>(), new ArrayList<>());
        this.joueur2 = creerJoueur2(choixAdversaire);
        this.bornes = initialiserBornes();
        this.gestionnaireJeu = new GestionnaireJeu(joueur1, joueur2);
        distribuerCartes();
    }

    private Joueur creerJoueur2(int choixAdversaire) {
        return switch (choixAdversaire) {
            case 1 -> new Joueur(2, new ArrayList<>(), new ArrayList<>());
            case 2 -> new JoueurIA(2);
            case 3 -> new JoueurIA_av(2);
            default -> throw new IllegalArgumentException("Choix d'adversaire invalide");
        };
    }

    private List<Borne> initialiserBornes() {
        List<Borne> bornes = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            bornes.add(new Borne(i + 1, new ArrayList<>(), new ArrayList<>(), false));
        }
        return bornes;
    }

    public void distribuerCartes() {
        gestionnaireJeu.distribuerCartes(pioche);
    }

    public void jouerTour(Joueur joueur, int indiceBorne, Carte carte) {
        gestionnaireJeu.jouerTour(joueur, indiceBorne, carte, bornes, pioche);
    }

    public boolean verifierVictoire() {
        return gestionnaireJeu.verifierVictoire(bornes);
    }

    public void afficherBornes() {
        System.out.println("\nÉtat des bornes:");
        for (int i = 0; i < bornes.size(); i++) {
            System.out.printf("Borne %d:\n", i + 1);
            System.out.print("  Joueur 1: ");
            afficherCartes(bornes.get(i).getMainJoueur1());
            System.out.print("  Joueur 2: ");
            afficherCartes(bornes.get(i).getMainJoueur2());
        }
    }

    private void afficherCartes(List<Carte> cartes) {
        if (cartes.isEmpty()) {
            System.out.println("[]");
            return;
        }
        for (Carte c : cartes) {
            System.out.printf("%d %s, ", c.getCarteNum(), c.getCouleurCarte());
        }
        System.out.println();
    }

    public Joueur getJoueur1() { return joueur1; }
    public Joueur getJoueur2() { return joueur2; }
    public Pioche getPioche() { return pioche; }
    public List<Borne> getBornes() { return bornes; }
}