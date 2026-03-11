package com.message.ihm.views.fx;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class EasterEggAnimationFx {
    private EasterEggAnimationFx() {
    }

    /**
     * Fait trembler un composant (Earthquake)
     */
    public static void playEarthquake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(-5);
        tt.setToX(5);
        tt.setCycleCount(20);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /**
     * Retourne l'interface à 180° (Flip)
     */
    public static void playFlip(Node node) {
        RotateTransition rt = new RotateTransition(Duration.seconds(1), node);
        rt.setByAngle(360);
        rt.setInterpolator(Interpolator.EASE_BOTH);
        rt.setOnFinished(e -> node.setRotate(0));
        rt.play();
    }

    /**
     * Simule un effet de fête (Party) en faisant scintiller l'opacité ou les couleurs
     */
    public static void playParty(Node node) {
        // 1. Effet Arc-en-ciel (Modification de la teinte)
        ColorAdjust colorAdjust = new ColorAdjust();
        // On booste un peu la saturation pour un effet plus fluo/techno
        colorAdjust.setSaturation(0.5);
        node.setEffect(colorAdjust);

        // On fait tourner la teinte de -1.0 à 1.0 très rapidement
        Timeline rainbowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(colorAdjust.hueProperty(), -1.0, Interpolator.LINEAR)),
                new KeyFrame(Duration.millis(300), new KeyValue(colorAdjust.hueProperty(), 1.0, Interpolator.LINEAR))
        );
        // 300ms * 33 cycles = environ 10 secondes d'arc-en-ciel
        rainbowTimeline.setCycleCount(33);
        rainbowTimeline.setAutoReverse(false);

        // 2. Effet de Battement de basse (Pulse)
        ScaleTransition beatTransition = new ScaleTransition(Duration.millis(150), node);
        beatTransition.setFromX(1.0);
        beatTransition.setFromY(1.0);
        beatTransition.setToX(1.03); // Zoom léger de 3%
        beatTransition.setToY(1.03);
        // 150ms * 66 = environ 10 secondes de battements
        beatTransition.setCycleCount(66);
        beatTransition.setAutoReverse(true);
        beatTransition.setInterpolator(Interpolator.EASE_BOTH);

        rainbowTimeline.setOnFinished(e -> node.setEffect(null));
        beatTransition.setOnFinished(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });

        rainbowTimeline.play();
        beatTransition.play();
    }

    public static void playDetach(Node node) {
        Random random = new Random();

        // Distance de chute (suffisamment grande pour sortir de l'écran)
        double fallDistance = 1000;

        // --- Calculs Aléatoires pour CE nœud ---
        Duration startDelay = Duration.millis(random.nextInt(800));
        Duration fallDuration = Duration.millis(500 + random.nextInt(500));
        double fallRotation = random.nextDouble() * 90 - 45;
        double fallDriftX = random.nextDouble() * 100 - 50;

        // 1. Animation de CHUTE (Falling)
        TranslateTransition fallTranslate = new TranslateTransition(fallDuration, node);
        fallTranslate.setToY(fallDistance);
        fallTranslate.setToX(fallDriftX);
        fallTranslate.setInterpolator(Interpolator.EASE_IN); // Accélère (gravité)

        RotateTransition fallRotate = new RotateTransition(fallDuration, node);
        fallRotate.setToAngle(fallRotation);
        fallRotate.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition individualFall = new ParallelTransition(fallTranslate, fallRotate);
        individualFall.setDelay(startDelay);

        // 2. Animation de RETOUR (Returning)
        Duration returnDelay = Duration.millis(random.nextInt(500));
        Duration returnDuration = Duration.millis(800);

        TranslateTransition returnTranslate = new TranslateTransition(returnDuration, node);
        returnTranslate.setFromY(fallDistance); // Part de là où il est tombé
        returnTranslate.setFromX(fallDriftX);
        returnTranslate.setToY(0);             // Revient à sa place initiale
        returnTranslate.setToX(0);
        returnTranslate.setInterpolator(Interpolator.EASE_OUT); // Décélère

        RotateTransition returnRotate = new RotateTransition(returnDuration, node);
        returnRotate.setFromAngle(fallRotation);
        returnRotate.setToAngle(0);
        returnRotate.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition individualReturn = new ParallelTransition(returnTranslate, returnRotate);
        individualReturn.setDelay(returnDelay);

        // 3. Pause
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        // 4. Enchaînement final : Chute -> Pause -> Retour
        SequentialTransition sequence = new SequentialTransition(individualFall, pause, individualReturn);

        // On sort temporairement le composant de la gestion stricte du layout (VBox)
        node.setManaged(false);
        node.setCache(true);
        node.setCacheHint(javafx.scene.CacheHint.SPEED);

        sequence.setOnFinished(e -> {
            // Nettoyage : s'assurer que tout est bien remis à zéro
            node.setManaged(true);
            node.setTranslateY(0);
            node.setTranslateX(0);
            node.setRotate(0);
        });

        sequence.play();
    }

    public static void playFlash(Node node) {
        // 1. GESTION DE L'AUDIO (Java 21 / JavaFX Media)
        try {
            URL soundUrl = EasterEggAnimationFx.class.getResource("/com/message/sounds/flash.mp3");
            if (soundUrl != null) {
                AudioClip flashSound = new AudioClip(soundUrl.toExternalForm());
                flashSound.play(0.1); // Le son démarre immédiatement à 20%
            } else {
                System.out.println("Fichier son introuvable : /com/message/sounds/flash.mp3");
            }
        } catch (Exception e) {
            System.err.println("Erreur audio : " + e.getMessage());
        }

        // 2. GESTION VISUELLE (Le voile blanc complet avec un Rectangle)
        if (node instanceof Pane pane) {
            Rectangle whiteOverlay = new Rectangle();
            whiteOverlay.setFill(Color.WHITE); // Couleur purement blanche

            // Laisse passer les clics à travers
            whiteOverlay.setMouseTransparent(true);

            // Le rectangle s'attache à la taille exacte de l'application
            whiteOverlay.widthProperty().bind(pane.widthProperty());
            whiteOverlay.heightProperty().bind(pane.heightProperty());

            // 3. SYNCHRONISATION : On attend 1 seconde avant d'afficher le blanc
            PauseTransition syncDelay = new PauseTransition(Duration.seconds(1));
            syncDelay.setOnFinished(event -> {

                // On ajoute le calque blanc à l'écran seulement après la seconde écoulée
                pane.getChildren().add(whiteOverlay);
                whiteOverlay.toFront();

                // 4. ANIMATION DU FONDU
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(whiteOverlay.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.seconds(2), new KeyValue(whiteOverlay.opacityProperty(), 1.0)),
                        new KeyFrame(Duration.seconds(7), new KeyValue(whiteOverlay.opacityProperty(), 0.0, Interpolator.EASE_IN))
                );

                timeline.setOnFinished(e -> pane.getChildren().remove(whiteOverlay));
                timeline.play();

            });

            // On lance le compte à rebours visuel
            syncDelay.play();
        }
    }

    public static void playDvd(Node node) {
        // 1. On rétrécit la zone de chat (30% de sa taille)
        ScaleTransition shrink = new ScaleTransition(Duration.millis(500), node);
        shrink.setToX(0.3);
        shrink.setToY(0.3);
        shrink.play();

        // On prépare le filtre de couleur (pour changer la teinte au rebond)
        ColorAdjust colorAdjust = new ColorAdjust();
        // On augmente la saturation et la luminosité pour que les couleurs "pètent" bien
        colorAdjust.setSaturation(0.8);
        colorAdjust.setBrightness(0.2);
        node.setEffect(colorAdjust);

        // 2. Le moteur physique (AnimationTimer tourne à 60 FPS)
        // Vitesses de déplacement en X et Y
        final double[] velocity = {4.0, 4.0};

        AnimationTimer dvdTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // On récupère le parent (le MainPanelViewFx) pour connaître les limites de l'écran
                Pane parent = (Pane) node.getParent();
                if (parent == null) return;

                double currentX = node.getTranslateX();
                double currentY = node.getTranslateY();

                // On calcule grossièrement les bords de l'écran (ajusté pour la taille réduite)
                double maxX = parent.getWidth() / 2.0 - (node.getBoundsInLocal().getWidth() * 0.15);
                double maxY = parent.getHeight() / 2.0 - (node.getBoundsInLocal().getHeight() * 0.15);

                boolean bounced = false;

                // Test de collision horizontale (Bords gauche/droite)
                if (Math.abs(currentX + velocity[0]) > maxX) {
                    velocity[0] *= -1; // On inverse la direction
                    bounced = true;
                }
                // Test de collision verticale (Bords haut/bas)
                if (Math.abs(currentY + velocity[1]) > maxY) {
                    velocity[1] *= -1; // On inverse la direction
                    bounced = true;
                }

                // S'il a touché un mur, on change la couleur aléatoirement !
                if (bounced) {
                    // Le Hue (Teinte) va de -1.0 à 1.0 en JavaFX
                    colorAdjust.setHue((Math.random() * 2) - 1);
                }

                // On applique le mouvement
                node.setTranslateX(currentX + velocity[0]);
                node.setTranslateY(currentY + velocity[1]);
            }
        };

        // On lance le rebond !
        dvdTimer.start();

        // 3. On arrête tout après 15 secondes
        PauseTransition stopDelay = new PauseTransition(Duration.seconds(12));
        stopDelay.setOnFinished(e -> {
            dvdTimer.stop(); // On coupe le moteur physique
            node.setEffect(null); // On retire les couleurs

            // On remet le chat à sa taille et position normales en douceur
            ScaleTransition growBack = new ScaleTransition(Duration.millis(500), node);
            growBack.setToX(1.0);
            growBack.setToY(1.0);

            TranslateTransition moveBack = new TranslateTransition(Duration.millis(500), node);
            moveBack.setToX(0);
            moveBack.setToY(0);

            new ParallelTransition(growBack, moveBack).play();
        });
        stopDelay.play();
    }

    /**
     * L'effet "Zéro Gravité" (/zerog) : Les éléments se détachent et flottent lentement
     * dans l'espace avant de revenir à leur place.
     */
    public static void playZeroG(Node node) {
        Random random = new Random();

        // Durée aléatoire pour que chaque élément flotte à sa propre vitesse
        Duration duration = Duration.seconds(5 + random.nextInt(5));

        // Destination aléatoire : entre -300 et +300 pixels dans toutes les directions
        double destX = random.nextDouble() * 600 - 300;
        double destY = random.nextDouble() * 600 - 300;
        // Rotation douce : entre -90 et +90 degrés
        double destAngle = random.nextDouble() * 180 - 90;

        // Mouvement de translation
        TranslateTransition tt = new TranslateTransition(duration, node);
        tt.setToX(destX);
        tt.setToY(destY);
        tt.setInterpolator(Interpolator.EASE_BOTH); // Démarrage et arrêt en douceur

        // Mouvement de rotation
        RotateTransition rt = new RotateTransition(duration, node);
        rt.setToAngle(destAngle);
        rt.setInterpolator(Interpolator.LINEAR); // Rotation constante

        // On groupe les deux
        ParallelTransition floatAnim = new ParallelTransition(tt, rt);

        // Magique : on lui dit de faire l'animation à l'envers une fois arrivé au bout !
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(2);

        // Optimisations pour éviter que ça rame
        node.setManaged(false);
        node.setCache(true);
        node.setCacheHint(javafx.scene.CacheHint.SPEED);

        // Nettoyage à la fin
        floatAnim.setOnFinished(e -> {
            node.setManaged(true);
            node.setCache(false);
            node.setTranslateX(0);
            node.setTranslateY(0);
            node.setRotate(0);
        });

        floatAnim.play();
    }

    /**
     * L'effet "Matrix" (/matrix) : Pluie de caractères verts sur fond noir.
     */
    public static void playMatrix(Node node) {
        if (!(node instanceof Pane pane)) return;

        // 1. Création de la toile de dessin (Canvas)
        Canvas canvas = new Canvas();
        canvas.setMouseTransparent(true); // Ne bloque pas les clics en dessous
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        pane.getChildren().add(canvas);
        canvas.toFront();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        int fontSize = 16;
        // On prévoit assez de colonnes même pour un très grand écran (ex: 4K)
        int maxColumns = 300;
        double[] drops = new double[maxColumns];

        // Initialisation : toutes les gouttes commencent en haut (ligne 1)
        for (int i = 0; i < maxColumns; i++) {
            drops[i] = 1;
        }

        // Les caractères possibles dans notre pluie Matrix
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%^&*";

        // 2. Le Moteur de dessin (AnimationTimer)
        AnimationTimer matrixTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            private boolean firstFrame = true;

            @Override
            public void handle(long now) {
                // On limite la vitesse pour que le texte soit lisible (ex: toutes les 50ms)
                if (now - lastUpdate < 50_000_000) return;
                lastUpdate = now;

                double width = canvas.getWidth();
                double height = canvas.getHeight();
                int columns = (int) (width / fontSize);

                if (firstFrame) {
                    // À la première image, on remplit tout en noir opaque pour cacher le chat
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, width, height);
                    firstFrame = false;
                } else {
                    // L'ASTUCE MATRIX : On dessine un rectangle noir très transparent par-dessus l'existant.
                    // Ça crée l'effet de "traînée" qui s'efface doucement !
                    gc.setFill(Color.rgb(0, 0, 0, 0.15));
                    gc.fillRect(0, 0, width, height);
                }

                // Configuration du texte vert fluo
                gc.setFill(Color.rgb(0, 255, 0));
                gc.setFont(Font.font("Monospace", fontSize));

                // On dessine un caractère aléatoire pour chaque colonne
                for (int i = 0; i < columns && i < maxColumns; i++) {
                    String text = String.valueOf(chars.charAt((int) (Math.random() * chars.length())));
                    double x = i * fontSize;
                    double y = drops[i] * fontSize;

                    gc.fillText(text, x, y);

                    // Si la goutte dépasse l'écran, on a 5% de chance de la ramener tout en haut
                    // (L'aléatoire évite que toutes les colonnes tombent en un seul bloc horizontal)
                    if (y > height && Math.random() > 0.95) {
                        drops[i] = 0;
                    }
                    drops[i]++; // La goutte descend d'une ligne
                }
            }
        };

        // Lancement de la pluie de code
        matrixTimer.start();

        // 3. Arrêt automatique après 10 secondes avec un fondu
        PauseTransition stopDelay = new PauseTransition(Duration.seconds(10));
        stopDelay.setOnFinished(e -> {
            matrixTimer.stop();

            // On fait disparaître le Canvas doucement
            FadeTransition ft = new FadeTransition(Duration.seconds(1), canvas);
            ft.setToValue(0);
            ft.setOnFinished(ev -> pane.getChildren().remove(canvas));
            ft.play();
        });
        stopDelay.play();
    }
}