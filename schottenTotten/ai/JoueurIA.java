package com.schottenTotten.ai;


import com.schottenTotten.controller.Jeu;
import com.schottenTotten.model.*;
import java.util.List;


public class JoueurIA extends Joueur {
    private IStrategieJeu strategie;

    public JoueurIA(int numJoueur) {
        super(numJoueur, null, null);
        this.strategie = new StrategieAleatoire();
    }

    public void jouerTourIA(Jeu jeu, List<Borne> bornes, Pioche pioche) {
        strategie.jouerTour(this, bornes, pioche);
    }
}