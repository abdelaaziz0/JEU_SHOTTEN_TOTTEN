package com.schottenTotten.ai;

import com.schottenTotten.model.*;
import java.util.List;

public interface IStrategieJeu {
    void jouerTour(Joueur joueur, List<Borne> bornes, Pioche pioche);
}