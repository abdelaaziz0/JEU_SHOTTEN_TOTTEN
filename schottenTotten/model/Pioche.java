package com.schottenTotten.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pioche implements IPioche {
    private List<Carte> cartes = new ArrayList<>();

    public Pioche() {
        String[] couleurs = {"rouge", "vert", "bleu", "jaune", "violet", "orange"};
        for (String couleur : couleurs) {
            for (int valeur = 1; valeur <= 9; valeur++) {
                cartes.add(new Carte(valeur, couleur));
            }
        }
        melanger();
    }

    @Override
    public void melanger() {
        Collections.shuffle(cartes);
    }

    @Override
    public Carte piocher() {
        return cartes.isEmpty() ? null : cartes.remove(0);
    }
}
