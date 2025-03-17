package com.schottenTotten.ai;

import java.util.List;
import java.util.Random;

import com.schottenTotten.model.Borne;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Joueur;
import com.schottenTotten.model.Pioche;

public class StrategieAleatoire implements IStrategieJeu {
    private Random random = new Random();

    @Override
    public void jouerTour(Joueur joueur, List<Borne> bornes, Pioche pioche) {
        List<Carte> main = joueur.getMainJoueur();
        if (main.isEmpty()) return;
        
        Carte carteChoisie = main.get(random.nextInt(main.size()));
        
        List<Borne> bornesDisponibles = bornes.stream()
            .filter(b -> !b.isControlee())
            .collect(java.util.stream.Collectors.toList());
        if (bornesDisponibles.isEmpty()) return;
        
        Borne borneChoisie = bornesDisponibles.get(random.nextInt(bornesDisponibles.size()));
        
        joueur.jouerCarte(borneChoisie, carteChoisie, pioche);
    }
}