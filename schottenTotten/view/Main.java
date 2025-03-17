package com.schottenTotten.view;
import java.util.Scanner;
import java.util.List;
import com.schottenTotten.controller.Jeu;
import com.schottenTotten.model.*;
import com.schottenTotten.ai.*;
interface GestionEntrees {
    int obtenirChoixValide(Scanner scanner, int min, int max, String message);
    void viderBuffer(Scanner scanner);
}

interface AffichageJeu {
    void afficherEtatJeu(Jeu jeu);
    void afficherResultatFinal(Jeu jeu);
    void afficherBornesDisponibles(Jeu jeu, Joueur joueur);
}

class GestionnaireEntrees implements GestionEntrees {
    @Override
    public int obtenirChoixValide(Scanner scanner, int min, int max, String message) {
        int choix = -1;
        while (choix < min || choix >= max) {
            try {
                System.out.println(message);
                choix = scanner.nextInt();
                if (choix < min || choix >= max) {
                    System.out.printf("Entrée invalide ! Choisissez entre %d et %d.\n", min, max);
                }
            } catch (Exception e) {
                System.out.println("Entrée invalide ! Veuillez entrer un nombre.");
                viderBuffer(scanner);
            }
        }
        return choix;
    }

    @Override
    public void viderBuffer(Scanner scanner) {
        scanner.next();
    }
}

class AffichageJeuImpl implements AffichageJeu {
    @Override
    public void afficherEtatJeu(Jeu jeu) {
        System.out.println("\n=== État du jeu ===");
        jeu.afficherBornes();
    }

    @Override
    public void afficherResultatFinal(Jeu jeu) {
        System.out.println("\n=== Fin du jeu! ===");
        int bornesJ1 = jeu.getJoueur1().getBornesGagnees().size();
        int bornesJ2 = jeu.getJoueur2().getBornesGagnees().size();
        System.out.println("Bornes gagnées par Joueur 1: " + bornesJ1);
        System.out.println("Bornes gagnées par Joueur 2: " + bornesJ2);
        
        if (bornesJ1 > bornesJ2) System.out.println("Joueur 1 gagne!");
        else if (bornesJ2 > bornesJ1) System.out.println("Joueur 2 gagne!");
        else System.out.println("Match nul!");
    }

    @Override
    public void afficherBornesDisponibles(Jeu jeu, Joueur joueur) {
        System.out.println("\nBornes disponibles :");
        for (int i = 0; i < jeu.getBornes().size(); i++) {
            Borne borne = jeu.getBornes().get(i);
            if (!borne.isControlee() && (joueur.getNumJoueur() == 1 ? 
                borne.getMainJoueur1().size() < 3 : borne.getMainJoueur2().size() < 3)) {
                System.out.printf("%d ", i+1);
            }
        }
    }
}

public class Main {
    private static final GestionEntrees gestionEntrees = new GestionnaireEntrees();
    private static final AffichageJeu affichage = new AffichageJeuImpl();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Jouer contre : \n1. Joueur\n2. IA Simple\n3. IA Avancée");
        int choix = scanner.nextInt();
        
        Jeu jeu = new Jeu(choix);
        
        while (!jeu.verifierVictoire()) {
            affichage.afficherEtatJeu(jeu);
            
            System.out.println("\nJoueur 1 - Votre main: ");
            afficherMain(jeu.getJoueur1().getMainJoueur());
            jouerTourHumain(jeu, jeu.getJoueur1(), scanner);
            
            if (jeu.verifierVictoire()) break;
            
            executerTourJoueur2(jeu, choix, scanner);
            affichage.afficherEtatJeu(jeu);
        }
        
        scanner.close();
        affichage.afficherResultatFinal(jeu);
    }
    
    private static void jouerTourHumain(Jeu jeu, Joueur joueur, Scanner scanner) {
        affichage.afficherBornesDisponibles(jeu, joueur);
        
        int carteIndex = gestionEntrees.obtenirChoixValide(scanner, 0, joueur.getMainJoueur().size(),
            "\nChoisir une carte (1-" + joueur.getMainJoueur().size() + "): ") - 1;
            
        int borneIndex = -1;
        while (borneIndex == -1) {
            borneIndex = gestionEntrees.obtenirChoixValide(scanner, 1, 10, "Choisir une borne (1-9): ") - 1;
            if (!estBorneJouable(jeu, joueur, borneIndex)) {
                System.out.println("Cette borne n'est pas disponible ! Choisissez-en une autre.");
                borneIndex = -1;
            }
        }
        
        jeu.jouerTour(joueur, borneIndex, joueur.getMainJoueur().get(carteIndex));
    }

    private static boolean estBorneJouable(Jeu jeu, Joueur joueur, int borneIndex) {
        Borne borne = jeu.getBornes().get(borneIndex);
        List<Carte> cartesBorne = (joueur.getNumJoueur() == 1) ? 
            borne.getMainJoueur1() : borne.getMainJoueur2();
        return !borne.isControlee() && cartesBorne.size() < 3;
    }

    private static void executerTourJoueur2(Jeu jeu, int choix, Scanner scanner) {
        if (choix == 1) {
            System.out.println("\nJoueur 2 - Votre main: ");
            afficherMain(jeu.getJoueur2().getMainJoueur());
            jouerTourHumain(jeu, jeu.getJoueur2(), scanner);
        } else if (choix == 2) {
            System.out.println("\nTour de l'IA Simple");
            ((JoueurIA)jeu.getJoueur2()).jouerTourIA(jeu, jeu.getBornes(), jeu.getPioche());
        } else {
            System.out.println("\nTour de l'IA Avancée");
            ((JoueurIA_av)jeu.getJoueur2()).jouerTourIA(jeu);
        }
    }
    
    private static void afficherMain(List<Carte> main) {
        for (int i = 0; i < main.size(); i++) {
            Carte c = main.get(i);
            System.out.printf("%d: %s de %s\n", i+1, c.getCarteNum(), c.getCouleurCarte());
        }
    }
}