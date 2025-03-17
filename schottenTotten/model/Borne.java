package com.schottenTotten.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

//Interface for combinations evaluation
interface CombinateurCartes {
 int evaluerCombinaison(List<Carte> cartes);
}

//Interface for managing cards
interface GestionnaireCartes {
 void ajouterCarte(Carte carte, int joueur);
 List<Carte> getCartesJoueur(int joueur);
 boolean estComplete(int joueur);
}

//Interface for control status
interface GestionnaireControle {
 boolean isControlee();
 void rendreControlee(boolean controlee);
}

//Base combination evaluator implementation
class CombinateurCartesImpl implements CombinateurCartes {
 @Override
 public int evaluerCombinaison(List<Carte> cartes) {
     if (cartes.size() != 3) return 0;
     
     Collections.sort(cartes, Comparator.comparing(Carte::getCarteNum));
     
     if (estSuiteCouleur(cartes)) return 400;
     if (estBrelan(cartes)) return 300;
     if (estCouleur(cartes)) return 200;
     if (estSuite(cartes)) return 100;
     
     return cartes.stream().mapToInt(Carte::getCarteNum).sum();
 }
 
 private boolean estSuiteCouleur(List<Carte> cartes) {
     return estCouleur(cartes) && estSuite(cartes);
 }
 
 private boolean estBrelan(List<Carte> cartes) {
     return cartes.stream().map(Carte::getCarteNum).distinct().count() == 1;
 }
 
 private boolean estCouleur(List<Carte> cartes) {
     return cartes.stream().map(Carte::getCouleurCarte).distinct().count() == 1;
 }
 
 private boolean estSuite(List<Carte> cartes) {
     return cartes.get(2).getCarteNum() == cartes.get(1).getCarteNum() + 1 &&
            cartes.get(1).getCarteNum() == cartes.get(0).getCarteNum() + 1;
 }
}

//Cards manager implementation
class GestionnaireCartesImpl implements GestionnaireCartes {
 private final List<Carte> cartesJoueur1 = new ArrayList<>();
 private final List<Carte> cartesJoueur2 = new ArrayList<>();
 private long tempsPremierComplete = -1;
 
 @Override
 public void ajouterCarte(Carte carte, int joueur) {
     List<Carte> cartes = getCartesJoueur(joueur);
     cartes.add(carte);
     
     if ((estComplete(1) || estComplete(2)) && tempsPremierComplete == -1) {
         tempsPremierComplete = joueur;
     }
 }
 
 @Override
 public List<Carte> getCartesJoueur(int joueur) {
     return joueur == 1 ? cartesJoueur1 : cartesJoueur2;
 }
 
 @Override
 public boolean estComplete(int joueur) {
     return getCartesJoueur(joueur).size() == 3;
 }
 
 public long getTempsPremierComplete() {
     return tempsPremierComplete;
 }
}

//Main Borne class using composition
public class Borne {
 private final int numBorne;
 private final CombinateurCartes combinateur;
 private final GestionnaireCartes gestionnaireCartes;
 private boolean controlee;
 
 public Borne(int numBorne, List<Carte> cartesJoueur1, List<Carte> cartesJoueur2, boolean controlee) {
     this.numBorne = numBorne;
     this.combinateur = new CombinateurCartesImpl();
     this.gestionnaireCartes = new GestionnaireCartesImpl();
     this.controlee = controlee;
     
     cartesJoueur1.forEach(carte -> gestionnaireCartes.ajouterCarte(carte, 1));
     cartesJoueur2.forEach(carte -> gestionnaireCartes.ajouterCarte(carte, 2));
 }
 
 public void ajouterCarte(Carte carte, int joueur) {
     gestionnaireCartes.ajouterCarte(carte, joueur);
     if (gestionnaireCartes.estComplete(1) && gestionnaireCartes.estComplete(2)) {
         this.controlee = true;
     }
 }
 
 public int comparerMains() {
     if (!controlee || !gestionnaireCartes.estComplete(1) || !gestionnaireCartes.estComplete(2)) {
         return 0;
     }
     
     int valeurJ1 = combinateur.evaluerCombinaison(gestionnaireCartes.getCartesJoueur(1));
     int valeurJ2 = combinateur.evaluerCombinaison(gestionnaireCartes.getCartesJoueur(2));
     
     if (valeurJ1 > valeurJ2) return 1;
     if (valeurJ1 < valeurJ2) return -1;
     if (valeurJ1 == valeurJ2) {
         long tempsPremierComplete = ((GestionnaireCartesImpl)gestionnaireCartes).getTempsPremierComplete();
         return tempsPremierComplete == 1 ? 1 : -1;
     }
     return 0;
 }
 
 public int getNumBorne() { return numBorne; }
 public List<Carte> getMainJoueur1() { return gestionnaireCartes.getCartesJoueur(1); }
 public List<Carte> getMainJoueur2() { return gestionnaireCartes.getCartesJoueur(2); }
 public boolean isControlee() { return controlee; }
 public void rendreControlee(boolean controlee) { this.controlee = controlee; }
}