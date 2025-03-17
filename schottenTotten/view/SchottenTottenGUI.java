package com.schottenTotten.view;
import com.schottenTotten.model.Carte;
import com.schottenTotten.model.Borne;
import com.schottenTotten.controller.Jeu;
import com.schottenTotten.ai.JoueurIA;
import com.schottenTotten.ai.JoueurIA_av;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.List;

interface InterfaceJeu {
    void initialiserComposants();
    void mettreAJourInterface();
    void jouerCoup(int borneIndex, int carteIndex);
}

interface GestionnaireVictoire {
    boolean verifierVictoireConsecutive();
    boolean verifierVictoireCinqBornes();
    void afficherFinPartie();
}

class GestionnaireVictoireImpl implements GestionnaireVictoire {
    private final Jeu jeu;
    
    GestionnaireVictoireImpl(Jeu jeu) {
        this.jeu = jeu;
    }
    
    @Override
    public boolean verifierVictoireConsecutive() {
        for (int i = 0; i < 7; i++) {
            Borne b1 = jeu.getBornes().get(i);
            Borne b2 = jeu.getBornes().get(i + 1);
            Borne b3 = jeu.getBornes().get(i + 2);
            
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
    public boolean verifierVictoireCinqBornes() {
        return jeu.getJoueur1().getBornesGagnees().size() >= 5 || 
               jeu.getJoueur2().getBornesGagnees().size() >= 5;
    }

    @Override
    public void afficherFinPartie() {
        int scoreJ1 = jeu.getJoueur1().getBornesGagnees().size();
        int scoreJ2 = jeu.getJoueur2().getBornesGagnees().size();
        
        String raison = verifierVictoireConsecutive() ? 
            "Victoire par contrôle de 3 bornes consécutives!" : 
            "Victoire par contrôle de 5 bornes!";
            
        String vainqueur = scoreJ1 > scoreJ2 ? "Joueur 1" : 
                          jeu.getJoueur2() instanceof JoueurIA ? "IA" : "Joueur 2";
        
        String message = String.format("%s gagne!\n%s\nScore final : %d - %d", 
                                     vainqueur, raison, scoreJ1, scoreJ2);
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

public class SchottenTottenGUI extends Application implements InterfaceJeu {
    private Jeu jeu;
    private BorderPane mainLayout;
    private HBox mainJoueur1;
    private GridPane bornes;
    private HBox mainJoueur2;
    private Button[] cartesJoueur1;
    private Button[] cartesJoueur2;
    private Button[][] bornesButtons;
    private int carteSelectionneeIndex = -1;
    private Label statusLabel;
    private Label scoreLabel;
    private GestionnaireVictoire gestionnaireVictoire;

    @Override
    public void start(Stage primaryStage) {
        afficherMenuSelection(primaryStage);
    }

    private void afficherMenuSelection(Stage primaryStage) {
        Stage menuStage = new Stage();
        VBox menuBox = new VBox(10);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));

        Label titre = new Label("Choisissez votre adversaire");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");

        Button[] boutons = {
            creerBoutonMenu("Joueur", 1, primaryStage, menuStage),
            creerBoutonMenu("IA Simple", 2, primaryStage, menuStage),
            creerBoutonMenu("IA Avancée", 3, primaryStage, menuStage)
        };

        menuBox.getChildren().add(titre);
        for (Button btn : boutons) {
            menuBox.getChildren().add(btn);
        }

        Scene menuScene = new Scene(menuBox, 300, 200);
        menuStage.setTitle("Menu");
        menuStage.setScene(menuScene);
        menuStage.showAndWait();
    }

    private Button creerBoutonMenu(String texte, int choixAdversaire, Stage primaryStage, Stage menuStage) {
        Button btn = new Button(texte);
        btn.setPrefWidth(150);
        btn.setOnAction(e -> {
            jeu = new Jeu(choixAdversaire);
            gestionnaireVictoire = new GestionnaireVictoireImpl(jeu);
            menuStage.close();
            demarrerPartie(primaryStage);
        });
        return btn;
    }

    private void demarrerPartie(Stage primaryStage) {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));
        
        initialiserComposants();
        
        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setTitle("Schotten Totten");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        mettreAJourInterface();
    }

    @Override
    public void initialiserComposants() {
        initialiserMainJoueur1();
        initialiserBornes();
        initialiserMainJoueur2();
        initialiserStatus();
    }

    private void initialiserMainJoueur1() {
        mainJoueur1 = new HBox(10);
        mainJoueur1.setAlignment(Pos.CENTER);
        mainJoueur1.setPadding(new Insets(10));
        cartesJoueur1 = new Button[6];
        
        for (int i = 0; i < 6; i++) {
            Button carteBtn = creerBoutonCarte();
            final int index = i;
            carteBtn.setOnAction(e -> selectionnerCarte(index));
            cartesJoueur1[i] = carteBtn;
            mainJoueur1.getChildren().add(carteBtn);
        }
        
        mainLayout.setBottom(mainJoueur1);
    }

    private void initialiserMainJoueur2() {
        mainJoueur2 = new HBox(10);
        mainJoueur2.setAlignment(Pos.CENTER);
        mainJoueur2.setPadding(new Insets(10));
        cartesJoueur2 = new Button[6];
        
        for (int i = 0; i < 6; i++) {
            Button carteBtn = creerBoutonCarte();
            cartesJoueur2[i] = carteBtn;
            mainJoueur2.getChildren().add(carteBtn);
        }
        
        mainLayout.setTop(mainJoueur2);
    }

    private void initialiserBornes() {
        bornes = new GridPane();
        bornes.setAlignment(Pos.CENTER);
        bornes.setHgap(10);
        bornes.setVgap(10);
        bornesButtons = new Button[9][6];
        
        for (int i = 0; i < 9; i++) {
            VBox borneBox = new VBox(5);
            borneBox.setAlignment(Pos.CENTER);
            borneBox.setPadding(new Insets(5));
            
            HBox cartesJ2 = new HBox(2);
            for (int j = 0; j < 3; j++) {
                Button carteBtn = creerBoutonCarte();
                bornesButtons[i][j] = carteBtn;
                cartesJ2.getChildren().add(carteBtn);
            }
            
            Label borneLabel = new Label("Borne " + (i + 1));
            borneLabel.setStyle("-fx-font-weight: bold");
            
            HBox cartesJ1 = new HBox(2);
            for (int j = 0; j < 3; j++) {
                Button carteBtn = creerBoutonCarte();
                bornesButtons[i][j + 3] = carteBtn;
                cartesJ1.getChildren().add(carteBtn);
            }
            
            borneBox.getChildren().addAll(cartesJ2, borneLabel, cartesJ1);
            
            final int borneIndex = i;
            borneBox.setOnMouseClicked(e -> {
                if (carteSelectionneeIndex != -1) {
                    jouerCoup(borneIndex, carteSelectionneeIndex);
                }
            });
            
            bornes.add(borneBox, i, 0);
        }
        
        mainLayout.setCenter(bornes);
    }

    private void initialiserStatus() {
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(10));
        
        statusLabel = new Label("Tour du Joueur 1");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
        
        scoreLabel = new Label("Score: 0 - 0");
        scoreLabel.setStyle("-fx-font-size: 14px");
        
        statusBox.getChildren().addAll(statusLabel, scoreLabel);
        mainLayout.setRight(statusBox);
    }

    private Button creerBoutonCarte() {
        Button btn = new Button();
        btn.setPrefSize(60, 90);
        btn.setStyle("-fx-background-color: white; -fx-border-color: black;");
        return btn;
    }

    private void selectionnerCarte(int index) {
        if (jeu.verifierVictoire()) return;
        
        for (Button btn : cartesJoueur1) {
            btn.setStyle("-fx-background-color: white; -fx-border-color: black;");
        }
        
        carteSelectionneeIndex = index;
        cartesJoueur1[index].setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
    }

    @Override
    public void jouerCoup(int borneIndex, int carteIndex) {
        if (carteSelectionneeIndex == -1 || jeu.verifierVictoire()) return;
        
        Borne borne = jeu.getBornes().get(borneIndex);
        if (borne.isControlee() || borne.getMainJoueur1().size() >= 3) {
            afficherErreur("Cette borne est déjà pleine ou contrôlée!");
            return;
        }

        Carte carteChoisie = jeu.getJoueur1().getMainJoueur().get(carteIndex);
        jeu.jouerTour(jeu.getJoueur1(), borneIndex, carteChoisie);
        carteSelectionneeIndex = -1;

        // Tour de l'IA
        try {
            if (jeu.getJoueur2() instanceof JoueurIA_av) {
                ((JoueurIA_av)jeu.getJoueur2()).jouerTourIA(jeu);
            } else if (jeu.getJoueur2() instanceof JoueurIA) {
                ((JoueurIA)jeu.getJoueur2()).jouerTourIA(jeu, jeu.getBornes(), jeu.getPioche());
            }
        } catch (Exception e) {
            System.err.println("Erreur IA: " + e.getMessage());
            e.printStackTrace();
        }

        mettreAJourInterface();

        if (gestionnaireVictoire.verifierVictoireConsecutive() || 
            gestionnaireVictoire.verifierVictoireCinqBornes()) {
            gestionnaireVictoire.afficherFinPartie();
        }
    }

    @Override
    public void mettreAJourInterface() {
        mettreAJourMain(true);
        mettreAJourMain(false);
        mettreAJourBornes();
        mettreAJourScore();
    }

    private void mettreAJourMain(boolean estJoueur1) {
        Button[] boutons = estJoueur1 ? cartesJoueur1 : cartesJoueur2;
        List<Carte> main = estJoueur1 ? 
            jeu.getJoueur1().getMainJoueur() : 
            jeu.getJoueur2().getMainJoueur();
            
        for (int i = 0; i < 6; i++) {
            if (i < main.size() && main.get(i) != null) {
                if (estJoueur1) {
                    mettreAJourBoutonCarte(boutons[i], main.get(i));
                } else {
                    boutons[i].setText("?");
                    boutons[i].setStyle("-fx-background-color: gray; -fx-border-color: black;");
                }
            } else {
                boutons[i].setText("");
                boutons[i].setStyle("-fx-background-color: white; -fx-border-color: black;");
            }
        }
    }

    private void mettreAJourBornes() {
        for (int i = 0; i < 9; i++) {
            Borne borne = jeu.getBornes().get(i);
            VBox borneBox = (VBox) bornes.getChildren().get(i);
            Label borneLabel = (Label) borneBox.getChildren().get(1);

            if (borne.isControlee()) {
                int comparaison = borne.comparerMains();
                if (comparaison > 0) {
                    borneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: blue;");
                    borneBox.setStyle("-fx-background-color: lightblue; -fx-padding: 5;");
                } else if (comparaison < 0) {
                    borneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                    borneBox.setStyle("-fx-background-color: pink; -fx-padding: 5;");
                }
            } else {
                borneLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                borneBox.setStyle("-fx-padding: 5;");
            }

            for (int j = 0; j < 3; j++) {
                // Cartes J2
                if (j < borne.getMainJoueur2().size() && borne.getMainJoueur2().get(j) != null) {
                    mettreAJourBoutonCarte(bornesButtons[i][j], borne.getMainJoueur2().get(j));
                } else {
                    bornesButtons[i][j].setText("");
                    bornesButtons[i][j].setStyle("-fx-background-color: white; -fx-border-color: black;");
                }

                // Cartes J1
                if (j < borne.getMainJoueur1().size() && borne.getMainJoueur1().get(j) != null) {
                    mettreAJourBoutonCarte(bornesButtons[i][j + 3], borne.getMainJoueur1().get(j));
                } else {
                    bornesButtons[i][j + 3].setText("");
                    bornesButtons[i][j + 3].setStyle("-fx-background-color: white; -fx-border-color: black;");
                }
            }
        }
    }

    private void mettreAJourScore() {
        int scoreJ1 = jeu.getJoueur1().getBornesGagnees().size();
        int scoreJ2 = jeu.getJoueur2().getBornesGagnees().size();
        scoreLabel.setText(String.format("Score: %d - %d", scoreJ1, scoreJ2));
    }

    private void mettreAJourBoutonCarte(Button btn, Carte carte) {
        if (carte == null) {
            btn.setText("");
            btn.setStyle("-fx-background-color: white; -fx-border-color: black;");
            return;
        }

        btn.setText(carte.getCarteNum() + "\n" + carte.getCouleurCarte());
        String couleurFond = switch (carte.getCouleurCarte().toLowerCase()) {
            case "rouge" -> "red";
            case "bleu" -> "blue";
            case "vert" -> "green";
            case "jaune" -> "yellow";
            case "violet" -> "purple";
            case "orange" -> "orange";
            default -> "white";
        };
        btn.setStyle("-fx-background-color: " + couleurFond + "; -fx-border-color: black;");
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
    }