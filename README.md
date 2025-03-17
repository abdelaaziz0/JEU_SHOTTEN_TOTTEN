# Schotten-Totten en Java

🎴 **Un jeu de société stratégique développé en Java, basé sur Schotten-Totten**

---

## 📋 Fonctionnalités
✔️ **Modes de jeu variés** : Support de la variante de base et de la variante tactique.

✔️ **Paramétrage des joueurs** : Ajout de joueurs humains et IA avec différents niveaux de difficulté.

✔️ **Gestion des règles** : Vérification des revendications des bornes et détermination du gagnant.

✔️ **Architecture modulaire** : Possibilité d’ajouter facilement de nouvelles variantes et IA.

✔️ **Interface console** : Représentation textuelle du jeu pour faciliter l’interaction.

✔️ **Tests unitaires** : Validation des mécaniques du jeu avec JUnit.

---

## 🔧 Prérequis
📌 **JDK** : Java 11+

📌 **Outils** : `Maven` ou `Gradle` (gestion des dépendances et compilation)

📌 **Bibliothèques** : `JUnit` pour les tests unitaires

---

## 📦 Installation
```sh
# Cloner le dépôt
git clone https://github.com/abdelaaziz0/JEU_SHOTTEN_TOTTEN.git

# Compiler le projet
mvn clean install  # Ou ./gradlew build
```

---

## 🚀 Utilisation
### Lancer le jeu
```sh
java -jar target/SchottenTotten.jar
```

### Jouer une partie
L’application démarre en console et vous permet de :
- Sélectionner une variante du jeu.
- Définir les joueurs (humains ou IA).
- Effectuer des actions et revendiquer des bornes.

---

## 📝 Explication des Modules
### 📌 Architecture
Le projet est organisé en plusieurs packages pour assurer la modularité et l’extensibilité :
- **`com.schottenTotten.model`** : Gestion des éléments du jeu (Carte, Joueur, Borne, Deck).
- **`com.schottenTotten.controller`** : Logique du jeu et gestion des tours.
- **`com.schottenTotten.view`** : Interface en mode console.
- **`com.schottenTotten.ai`** : Implémentation des IA.

### 📌 Gestion des variantes
L’application utilise le pattern **Factory** pour instancier les différentes éditions du jeu :
```java
Jeu jeu = JeuFactory.getJeu(Variante.TACTIQUE);
```
Cela permet d’intégrer facilement de nouvelles règles sans modifier le code existant.

### 📌 Robustesse et gestion des erreurs
Le jeu gère plusieurs types d’exceptions pour éviter les comportements indésirables :
- **Validation des entrées** : Vérification des indices de cartes et des choix de l’utilisateur.
- **Gestion des erreurs** : Protection contre les erreurs de deck vide ou d’actions invalides.

---

## 🧪 Tests unitaires
Les tests sont réalisés avec JUnit pour valider les fonctionnalités essentielles :
```sh
mvn test  # Ou ./gradlew test
```
Tests couvrant :
✔️ Vérification des revendications de bornes.
✔️ Gestion des cartes et des decks.
✔️ Conditions de victoire.

---

## 📂 Structure du projet
```
.
├── src/main/java/com/schottenTotten
│   ├── model/        # Classes du modèle de jeu
│   ├── controller/   # Gestion du déroulement du jeu
│   ├── view/         # Interface utilisateur en mode console
│   ├── ai/           # IA pour les joueurs non humains
├── README.md         # Documentation du projet
```

---

## 📄 Licence
Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

