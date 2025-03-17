package com.schottenTotten.ai;
import com.schottenTotten.model.Joueur;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Borne;
import com.schottenTotten.controller.Jeu;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoueurIA_av extends Joueur {
    // Constantes de base
    private static final String[] COULEURS = {"rouge", "vert", "bleu", "jaune", "violet", "orange"};
    
    // Scores des combinaisons
    private static final int SCORE_SUITE_COULEUR = 1000;
    private static final int SCORE_BRELAN = 800;
    private static final int SCORE_COULEUR = 600;
    private static final int SCORE_SUITE = 400;
    private static final int BASE_SCORE_SOMME = 200;
    
    // Paramètres stratégiques
    private static final int BONUS_CREATION_COMBINAISON = 150;
    private static final double POIDS_IMPACT_IMMEDIAT = 1.0;
    private static final double POIDS_POTENTIEL_FUTUR = 0.7;
    private static final double POIDS_BLOCAGE = 0.5;
    private static final double POIDS_RISQUE = 0.3;
    
    // État interne
    private Set<Carte> cartesJouees;
    private Map<Integer, List<Combinaison>> historiqueCombinaisons;
    private AnalyseurSituation analyseur;
    
    public JoueurIA_av(int numJoueur) {
        super(numJoueur, new ArrayList<>(), new ArrayList<>());
        this.cartesJouees = new HashSet<>();
        this.historiqueCombinaisons = new HashMap<>();
        this.analyseur = new AnalyseurSituation();
    }
    // Classes internes pour la représentation de l'état
    private class EtatJeu {
        List<EtatBorne> bornes;
        List<Carte> mainJoueur;
        Set<Carte> cartesJouees;
        
        // Constructeur à partir du jeu modifié
        EtatJeu(Jeu jeu) {
            this.bornes = jeu.getBornes().stream()
                            .map(b -> new EtatBorne(b))
                            .collect(Collectors.toList());
            this.mainJoueur = new ArrayList<>(getMainJoueur());
            // Initialiser cartesJouees avec un nouvel HashSet si cartesJouees est null
            this.cartesJouees = new HashSet<>();
            // Mettre à jour les cartes jouées à partir des bornes
            for (Borne borne : jeu.getBornes()) {
                this.cartesJouees.addAll(borne.getMainJoueur1());
                this.cartesJouees.addAll(borne.getMainJoueur2());
            }
        }
        
        // Constructeur copie profonde
        EtatJeu(EtatJeu autre) {
            this.bornes = autre.bornes.stream()
                            .map(b -> new EtatBorne(
                                    new ArrayList<>(b.cartesJoueur1),
                                    new ArrayList<>(b.cartesJoueur2),
                                    b.controlee))
                            .collect(Collectors.toList());
            this.mainJoueur = new ArrayList<>(autre.mainJoueur);
            this.cartesJouees = new HashSet<>(autre.cartesJouees);
        }
    }
    
    private class EtatBorne {
        List<Carte> cartesJoueur1;
        List<Carte> cartesJoueur2;
        boolean controlee;
        
        // Constructeur à partir d'une Borne
        EtatBorne(Borne borne) {
            this.cartesJoueur1 = new ArrayList<>(borne.getMainJoueur1());
            this.cartesJoueur2 = new ArrayList<>(borne.getMainJoueur2());
            this.controlee = borne.isControlee();
        }
        
        // Constructeur avec paramètres explicites
        EtatBorne(List<Carte> cartesJoueur1, List<Carte> cartesJoueur2, boolean controlee) {
            this.cartesJoueur1 = new ArrayList<>(cartesJoueur1);
            this.cartesJoueur2 = new ArrayList<>(cartesJoueur2);
            this.controlee = controlee;
        }
    }
    private class AnalyseurSituation {
        public Map<Integer, Double> evaluerMenaces(EtatJeu etat) {
            Map<Integer, Double> menaces = new HashMap<>();
            for (int i = 0; i < etat.bornes.size(); i++) {
                EtatBorne borne = etat.bornes.get(i);
                List<Carte> cartesAdversaire = getNumJoueur() == 1 ? 
                    borne.cartesJoueur2 : borne.cartesJoueur1;
                menaces.put(i, calculerMenaceAdversaire(cartesAdversaire));
            }
            return menaces;
        }
        
        private double calculerMenaceAdversaire(List<Carte> cartesAdversaire) {
            if (cartesAdversaire.size() < 2) return 0.0;
            
            double menaceMax = 0.0;
            if (peutFormerSuiteCouleurPotentielle(cartesAdversaire)) {
                menaceMax = Math.max(menaceMax, 0.8);
            }
            if (peutFormerBrelanPotentiel(cartesAdversaire)) {
                menaceMax = Math.max(menaceMax, 0.7);
            }
            return menaceMax;
        }
    }
    
    // Système de prise de décision
    public void jouerTourIA(Jeu jeu) {
        EtatJeu etatActuel = new EtatJeu(jeu);
        MeilleurCoup meilleurCoup = determinerMeilleurCoup(etatActuel);
        if (meilleurCoup != null) {
            jeu.jouerTour(this, meilleurCoup.borne, meilleurCoup.carte);
            mettreAJourHistorique(meilleurCoup);
        }
    }

    private class MeilleurCoup {
        Carte carte;
        int borne;
        private int score;
        Combinaison combinaisonVisee;
        
        MeilleurCoup(Carte carte, int borne, int score, Combinaison combinaisonVisee) {
            this.carte = carte;
            this.borne = borne;
            this.setScore(score);
            this.combinaisonVisee = combinaisonVisee;
        }
		public void setScore(int score) {
			this.score = score;
		}
		public int getScore() {
			return this.score;
		}
    }
    
    private MeilleurCoup determinerMeilleurCoup(EtatJeu etat) {
        MeilleurCoup meilleurCoup = null;
        int meilleurScore = Integer.MIN_VALUE;
        
        Map<Integer, Double> menaces = analyseur.evaluerMenaces(etat);
        
        for (Carte carte : etat.mainJoueur) {
            for (int i = 0; i < 9; i++) {
                if (peutJouerSurBorne(etat.bornes.get(i))) {
                    int scoreImpactImmediat = evaluerImpactImmediat(etat, i, carte);
                    double scorePotentielFutur = evaluerPotentielFutur(etat, i, carte);
                    double scoreBlocage = evaluerBlocage(etat, i, carte);
                    double scoreRisque = evaluerRisque(etat, i, carte, menaces.get(i));
                    
                    int scoreTotal = (int)(
                        scoreImpactImmediat * POIDS_IMPACT_IMMEDIAT +
                        scorePotentielFutur * POIDS_POTENTIEL_FUTUR +
                        scoreBlocage * POIDS_BLOCAGE -
                        scoreRisque * POIDS_RISQUE
                    );
                    
                    if (scoreTotal > meilleurScore) {
                        meilleurScore = scoreTotal;
                        Combinaison combinaisonVisee = determinerCombinaisonVisee(etat, i, carte);
                        meilleurCoup = new MeilleurCoup(carte, i, scoreTotal, combinaisonVisee);
                    }
                }
            }
        }
        return meilleurCoup;
    }
    
    // Méthodes d'évaluation
    
    private double evaluerPotentielFutur(EtatJeu etat, int borneIndex, Carte carte) {
        // Simulation des prochains tours possibles
        double potentiel = 0.0;
        List<Carte> cartesRestantes = calculerCartesRestantes(etat);
        
        for (Carte carteFuture : cartesRestantes) {
            EtatJeu simulationFuture = simulerCoup(etat, borneIndex, carte);
            int scoreAvecCarteFuture = evaluerImpactImmediat(simulationFuture, borneIndex, carteFuture);
            potentiel = Math.max(potentiel, scoreAvecCarteFuture * 0.8); // Discount pour l'incertitude
        }
        
        return potentiel;
    }
    
    private double evaluerBlocage(EtatJeu etat, int borneIndex, Carte carte) {
        if (etat == null || carte == null || borneIndex < 0 || borneIndex >= etat.bornes.size()) {
            return 0.0;
        }

        EtatBorne borne = etat.bornes.get(borneIndex);
        if (borne == null) return 0.0;

        List<Carte> cartesAdversaire = getNumJoueur() == 1 ? 
            borne.cartesJoueur2 : borne.cartesJoueur1;
            
        if (cartesAdversaire == null || cartesAdversaire.isEmpty()) {
            return 0.0;
        }

        if (carteEstStrategique(carte, cartesAdversaire)) {
            return BONUS_CREATION_COMBINAISON;
        }
        return 0.0;
    }

    
    private double evaluerRisque(EtatJeu etat, int borneIndex, Carte carte, double menaceActuelle) {
        if (etat == null || carte == null || borneIndex < 0) {
            return 0.0;
        }
        
        if (estBorneStrategiquePourSequence(etat, borneIndex)) {
            return menaceActuelle * 2;
        }
        return menaceActuelle;
    }
    private int evaluerCombinaisonsPotentielles(List<Carte> cartes) {
        if (cartes.isEmpty()) return 0;
        
        Map<String, Double> probabilites = new HashMap<>();
        
        // Évaluer chaque type de combinaison possible
        probabilites.put("SUITE_COULEUR", evaluerProbabiliteCombinaison(cartes, "SUITE_COULEUR"));
        probabilites.put("BRELAN", evaluerProbabiliteCombinaison(cartes, "BRELAN"));
        probabilites.put("COULEUR", evaluerProbabiliteCombinaison(cartes, "COULEUR"));
        probabilites.put("SUITE", evaluerProbabiliteCombinaison(cartes, "SUITE"));

        // Calculer le score maximum pondéré par les probabilités
        return (int) probabilites.entrySet().stream()
            .mapToDouble(e -> getScoreCombinaison(e.getKey()) * e.getValue())
            .max()
            .orElse(0.0);
    }

    private int getScoreCombinaison(String type) {
        return switch (type) {
            case "SUITE_COULEUR" -> SCORE_SUITE_COULEUR;
            case "BRELAN" -> SCORE_BRELAN;
            case "COULEUR" -> SCORE_COULEUR;
            case "SUITE" -> SCORE_SUITE;
            default -> BASE_SCORE_SOMME;
        };
    }

    private boolean carteEstStrategique(Carte carte, List<Carte> cartesAdversaire) {
        if (carte == null || cartesAdversaire == null || cartesAdversaire.size() != 2 || 
            cartesAdversaire.stream().anyMatch(c -> c == null)) {
            return false;
        }

        // Vérifier pour un brelan potentiel
        if (cartesAdversaire.get(0).getCarteNum() == cartesAdversaire.get(1).getCarteNum() &&
            carte.getCarteNum() == cartesAdversaire.get(0).getCarteNum()) {
            return true;
        }

        // Vérifier pour une suite couleur potentielle
        String couleurAdv = cartesAdversaire.get(0).getCouleurCarte();
        if (cartesAdversaire.get(1).getCouleurCarte().equals(couleurAdv) &&
            carte.getCouleurCarte().equals(couleurAdv)) {
            
            int val1 = cartesAdversaire.get(0).getCarteNum();
            int val2 = cartesAdversaire.get(1).getCarteNum();
            int diff = Math.abs(val1 - val2);

            if (diff <= 2) {
                int min = Math.min(val1, val2);
                int max = Math.max(val1, val2);

                if (diff == 1) {
                    return carte.getCarteNum() == min - 1 || carte.getCarteNum() == max + 1;
                } else if (diff == 2) {
                    return carte.getCarteNum() == min + 1;
                }
            }
        }

        // Vérifier pour une couleur pure
        boolean touteMemesCouleurs = cartesAdversaire.stream()
            .allMatch(c -> c.getCouleurCarte().equals(cartesAdversaire.get(0).getCouleurCarte()));
        if (touteMemesCouleurs && carte.getCouleurCarte().equals(cartesAdversaire.get(0).getCouleurCarte())) {
            return true;
        }

        // Vérifier pour une suite simple
        List<Integer> valeurs = cartesAdversaire.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());
        int diff = Math.abs(valeurs.get(0) - valeurs.get(1));
        if (diff == 1) {
            return carte.getCarteNum() == Math.min(valeurs.get(0), valeurs.get(1)) - 1 ||
                   carte.getCarteNum() == Math.max(valeurs.get(0), valeurs.get(1)) + 1;
        } else if (diff == 2) {
            return carte.getCarteNum() == Math.min(valeurs.get(0), valeurs.get(1)) + 1;
        }
        return carte.getCarteNum() >= 7;
    }



    private double evaluerProbabiliteSuiteCouleur(List<Carte> cartes) {
        if (cartes.size() < 1) return 0.0;
        
        String couleur = cartes.get(0).getCouleurCarte();
        if (!cartes.stream().allMatch(c -> c.getCouleurCarte().equals(couleur))) {
            return 0.0;
        }

        List<Integer> valeurs = cartes.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());

        Set<Integer> valeursNecessaires = new HashSet<>();
        int min = Collections.min(valeurs);
        int max = Collections.max(valeurs);

        // Identifier toutes les valeurs nécessaires possibles
        for (int i = max - 2; i <= min + 2; i++) {
            if (i >= 1 && i <= 9 && !valeurs.contains(i)) {
                valeursNecessaires.add(i);
            }
        }

        long cartesDisponibles = valeursNecessaires.stream()
            .filter(v -> !cartesJouees.contains(new Carte(v, couleur)))
            .count();

        return cartesDisponibles / (double) (3 - cartes.size());
    }

    private double evaluerProbabiliteBrelan(List<Carte> cartes) {
        if (cartes.isEmpty()) return 0.0;
        
        int valeur = cartes.get(0).getCarteNum();
        if (!cartes.stream().allMatch(c -> c.getCarteNum() == valeur)) {
            return 0.0;
        }

        long cartesDisponibles = Arrays.stream(COULEURS)
            .map(couleur -> new Carte(valeur, couleur))
            .filter(c -> !cartesJouees.contains(c) && !cartes.contains(c))
            .count();

        return cartesDisponibles / (double) (3 - cartes.size());
    }

    private double evaluerProbabiliteCouleur(List<Carte> cartes) {
        if (cartes.isEmpty()) return 0.0;
        
        String couleur = cartes.get(0).getCouleurCarte();
        if (!cartes.stream().allMatch(c -> c.getCouleurCarte().equals(couleur))) {
            return 0.0;
        }

        long cartesDisponibles = IntStream.rangeClosed(1, 9)
            .mapToObj(v -> new Carte(v, couleur))
            .filter(c -> !cartesJouees.contains(c) && !cartes.contains(c))
            .count();

        return cartesDisponibles / (double) (3 - cartes.size());
    }

    private double evaluerProbabiliteSuite(List<Carte> cartes) {
        if (cartes.isEmpty()) return 0.0;

        List<Integer> valeurs = cartes.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());

        Set<Integer> valeursNecessaires = new HashSet<>();
        int min = Collections.min(valeurs);
        int max = Collections.max(valeurs);

        // Calculer toutes les possibilités de suite
        for (int i = max - 2; i <= min + 2; i++) {
            if (i >= 1 && i <= 9 && !valeurs.contains(i)) {
                valeursNecessaires.add(i);
            }
        }

        long cartesDisponibles = valeursNecessaires.stream()
            .flatMap(v -> Arrays.stream(COULEURS)
                .map(c -> new Carte(v, c)))
            .filter(c -> !cartesJouees.contains(c))
            .count();

        return cartesDisponibles / ((double) (3 - cartes.size()) * valeursNecessaires.size());
    }

    private boolean peutFormerSuiteCouleur(List<Carte> cartes) {
        if (cartes == null || cartes.size() < 3 || cartes.stream().anyMatch(c -> c == null)) {
            return false;
        }
        
        String couleur = cartes.get(0).getCouleurCarte();
        if (!cartes.stream().allMatch(c -> c.getCouleurCarte().equals(couleur))) {
            return false;
        }
        
        List<Integer> valeurs = cartes.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());
            
        for (int i = 1; i < valeurs.size(); i++) {
            if (valeurs.get(i) != valeurs.get(i-1) + 1) return false;
        }
        return true;
    }

    private int evaluerCombinaisons(List<Carte> cartes) {
        if (cartes == null || cartes.stream().anyMatch(c -> c == null)) {
            return 0;
        }

        if (cartes.size() < 3) {
            return evaluerCombinaisonsPotentielles(cartes);
        }

        if (peutFormerSuiteCouleur(cartes)) return SCORE_SUITE_COULEUR;
        if (peutFormerBrelan(cartes)) return SCORE_BRELAN;
        if (peutFormerCouleur(cartes)) return SCORE_COULEUR;
        if (peutFormerSuite(cartes)) return SCORE_SUITE;

        return BASE_SCORE_SOMME + cartes.stream()
                                      .mapToInt(Carte::getCarteNum)
                                      .sum();
    }

    private boolean peutFormerBrelan(List<Carte> cartes) {
        if (cartes == null || cartes.size() < 3 || cartes.stream().anyMatch(c -> c == null)) {
            return false;
        }
        int valeur = cartes.get(0).getCarteNum();
        return cartes.stream().allMatch(c -> c.getCarteNum() == valeur);
    }

    private boolean peutFormerCouleur(List<Carte> cartes) {
        if (cartes == null || cartes.size() < 3 || cartes.stream().anyMatch(c -> c == null)) {
            return false;
        }
        String couleur = cartes.get(0).getCouleurCarte();
        return cartes.stream().allMatch(c -> c.getCouleurCarte().equals(couleur));
    }

    private boolean peutFormerSuite(List<Carte> cartes) {
        if (cartes == null || cartes.size() < 3 || cartes.stream().anyMatch(c -> c == null)) {
            return false;
        }
        List<Integer> valeurs = cartes.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());
            
        for (int i = 1; i < valeurs.size(); i++) {
            if (valeurs.get(i) != valeurs.get(i-1) + 1) return false;
        }
        return true;
    }

    private double evaluerProbabiliteCombinaison(List<Carte> cartes, String type) {
        if (cartes == null || cartes.isEmpty() || cartes.stream().anyMatch(c -> c == null)) {
            return 0.0;
        }

        return switch (type) {
            case "SUITE_COULEUR" -> evaluerProbabiliteSuiteCouleur(cartes);
            case "BRELAN" -> evaluerProbabiliteBrelan(cartes);
            case "COULEUR" -> evaluerProbabiliteCouleur(cartes);
            case "SUITE" -> evaluerProbabiliteSuite(cartes);
            default -> 0.0;
        };
    }

    private int evaluerImpactImmediat(EtatJeu etat, int borneIndex, Carte carte) {
        if (etat == null || carte == null || borneIndex < 0 || borneIndex >= etat.bornes.size()) {
            return 0;
        }

        EtatBorne borne = etat.bornes.get(borneIndex);
        List<Carte> nosCartes = getNumJoueur() == 1 ? 
            borne.cartesJoueur1 : borne.cartesJoueur2;
        
        if (nosCartes == null) {
            return 0;
        }

        List<Carte> simulation = new ArrayList<>(nosCartes);
        simulation.add(carte);
        
        return evaluerCombinaisons(simulation);
    }
    private boolean estBorneStrategiquePourSequence(EtatJeu etat, int borneIndex) {
        if (etat == null || borneIndex < 0 || borneIndex >= etat.bornes.size()) {
            return false;
        }

        int sequencePotentielle = 1;
        
        // Vérifier à gauche
        for (int i = borneIndex - 1; i >= Math.max(0, borneIndex - 2); i--) {
            if (nousControlonsBorne(etat.bornes.get(i))) {
                sequencePotentielle++;
            }
        }
        
        // Vérifier à droite
        for (int i = borneIndex + 1; i <= Math.min(8, borneIndex + 2); i++) {
            if (nousControlonsBorne(etat.bornes.get(i))) {
                sequencePotentielle++;
            }
        }
        
        return sequencePotentielle >= 2;
    }

    private boolean nousControlonsBorne(EtatBorne borne) {
        if (!borne.controlee) return false;
        List<Carte> nosCartes = getNumJoueur() == 1 ? 
            borne.cartesJoueur1 : borne.cartesJoueur2;
        List<Carte> cartesAdversaire = getNumJoueur() == 1 ? 
            borne.cartesJoueur2 : borne.cartesJoueur1;
            
        return evaluerCombinaisons(nosCartes) > evaluerCombinaisons(cartesAdversaire);
    }

    private boolean peutJouerSurBorne(EtatBorne borne) {
        List<Carte> cartesJoueur = getNumJoueur() == 1 ? 
            borne.cartesJoueur1 : borne.cartesJoueur2;
        return cartesJoueur.size() < 3 && !borne.controlee;
    }

    private EtatJeu simulerCoup(EtatJeu etat, int borneIndex, Carte carte) {
        EtatJeu nouvelEtat = new EtatJeu(etat);
        
        // Simuler le coup
        EtatBorne borneCiblee = nouvelEtat.bornes.get(borneIndex);
        if (getNumJoueur() == 1) {
            borneCiblee.cartesJoueur1.add(carte);
        } else {
            borneCiblee.cartesJoueur2.add(carte);
        }
        
        // Mettre à jour les cartes jouées
        nouvelEtat.cartesJouees.add(carte);
        nouvelEtat.mainJoueur.remove(carte);
        
        return nouvelEtat;
    }
    


    private List<Carte> calculerCartesRestantes(EtatJeu etat) {
        List<Carte> toutesCartes = new ArrayList<>();
        for (String couleur : COULEURS) {
            for (int valeur = 1; valeur <= 9; valeur++) {
                toutesCartes.add(new Carte(valeur, couleur));
            }
        }
        
        // Retirer les cartes jouées et celles dans notre main
        toutesCartes.removeAll(etat.cartesJouees);
        toutesCartes.removeAll(etat.mainJoueur);
        
        return toutesCartes;
    }

    private void mettreAJourHistorique(MeilleurCoup coup) {
        if (coup.combinaisonVisee != null) {
            historiqueCombinaisons.computeIfAbsent(coup.borne, k -> new ArrayList<>())
                                .add(coup.combinaisonVisee);
        }
    }

    private Combinaison determinerCombinaisonVisee(EtatJeu etat, int borneIndex, Carte carte) {
        EtatBorne borne = etat.bornes.get(borneIndex);
        List<Carte> nosCartes = getNumJoueur() == 1 ? 
            borne.cartesJoueur1 : borne.cartesJoueur2;
        List<Carte> simulation = new ArrayList<>(nosCartes);
        simulation.add(carte);

        // Déterminer la meilleure combinaison possible
        if (peutFormerSuiteCouleur(simulation) || 
            (simulation.size() < 3 && evaluerProbabiliteSuiteCouleur(simulation) > 0.5)) {
            return Combinaison.SUITE_COULEUR;
        }
        if (peutFormerBrelan(simulation) || 
            (simulation.size() < 3 && evaluerProbabiliteBrelan(simulation) > 0.5)) {
            return Combinaison.BRELAN;
        }
        if (peutFormerCouleur(simulation) || 
            (simulation.size() < 3 && evaluerProbabiliteCouleur(simulation) > 0.5)) {
            return Combinaison.COULEUR;
        }
        if (peutFormerSuite(simulation) || 
            (simulation.size() < 3 && evaluerProbabiliteSuite(simulation) > 0.5)) {
            return Combinaison.SUITE;
        }

        return Combinaison.SOMME;
    }

    private enum Combinaison {
        SUITE_COULEUR,
        BRELAN,
        COULEUR,
        SUITE,
        SOMME
    }
    private boolean peutFormerSuiteCouleurPotentielle(List<Carte> cartes) {
        if (cartes.size() < 1) return false;

        String couleur = cartes.get(0).getCouleurCarte();
        if (!cartes.stream().allMatch(c -> c.getCouleurCarte().equals(couleur))) {
            return false;
        }

        List<Integer> valeurs = cartes.stream()
            .map(Carte::getCarteNum)
            .sorted()
            .collect(Collectors.toList());

        if (cartes.size() == 2) {
            int diff = Math.abs(valeurs.get(0) - valeurs.get(1));
            if (diff > 2) return false; // Trop éloignées pour former une suite

            int min = Math.min(valeurs.get(0), valeurs.get(1));
            int max = Math.max(valeurs.get(0), valeurs.get(1));

            if (diff == 2) {
                Carte carteNecessaire = new Carte(min + 1, couleur);
                return !cartesJouees.contains(carteNecessaire);
            } else {
                boolean peutCompleterAvant = min > 1 && !cartesJouees.contains(new Carte(min - 1, couleur));
                boolean peutCompleterApres = max < 9 && !cartesJouees.contains(new Carte(max + 1, couleur));
                return peutCompleterAvant || peutCompleterApres;
            }
        }
        else if (cartes.size() == 1) {
            int valeur = valeurs.get(0);
            return peutFormerSuiteAutourDe(valeur, couleur);
        }

        return false;
    }

    private boolean peutFormerBrelanPotentiel(List<Carte> cartes) {
        if (cartes.size() < 1) return false;
        
        int valeur = cartes.get(0).getCarteNum();
        if (!cartes.stream().allMatch(c -> c.getCarteNum() == valeur)) {
            return false;
        }

        long cartesDisponibles = Arrays.stream(COULEURS)
            .map(couleur -> new Carte(valeur, couleur))
            .filter(c -> !cartesJouees.contains(c) && !cartes.contains(c))
            .count();

        return cartesDisponibles >= (3 - cartes.size());
    }

    private boolean peutFormerSuiteAutourDe(int valeur, String couleur) {
        List<List<Integer>> possibilitesSuite = new ArrayList<>();
        
        for (int debut = Math.max(1, valeur - 2); debut <= Math.min(7, valeur); debut++) {
            List<Integer> possibilite = Arrays.asList(debut, debut + 1, debut + 2);
            if (possibilite.contains(valeur)) {
                possibilitesSuite.add(possibilite);
            }
        }

        return possibilitesSuite.stream().anyMatch(suite -> 
            suite.stream()
                .filter(v -> v != valeur)
                .allMatch(v -> !cartesJouees.contains(new Carte(v, couleur)))
        );
    }

 
}