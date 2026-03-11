package com.message.ihm.views;

import com.message.datamodel.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue affichant le profil complet d'un utilisateur.
 * Présente l'avatar (image ou initiale), le nom et le tag de l'utilisateur.
 */
public class ProfileView extends VBox {

    /**
     * Initialise la vue de profil pour l'utilisateur spécifié.
     * Configure l'affichage de l'avatar avec gestion de l'image (chargement asynchrone ou repli sur les initiales).
     * @param user L'utilisateur dont le profil doit être affiché.
     */
    public ProfileView(User user) {
        super(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #2f3136;");

        // Avatar
        StackPane avatarStack = new StackPane();
        avatarStack.setPadding(new Insets(10));
        Circle avatarCircle = new Circle(50);

        String picturePath = user.getPicturePath();
        if (picturePath != null && !picturePath.trim().isEmpty()) {
            Image image = new Image(picturePath, true);
            
            image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0 && !image.isError()) {
                    avatarCircle.setFill(new ImagePattern(image));
                    if (avatarStack.getChildren().size() > 1) {
                        avatarStack.getChildren().remove(1);
                    }
                }
            });
            
            image.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    Platform.runLater(() -> showInitials(avatarStack, avatarCircle, user));
                }
            });
            
            showInitials(avatarStack, avatarCircle, user);

        } else {
            showInitials(avatarStack, avatarCircle, user);
        }

        // User Info
        Label nameLabel = new Label(user.getName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.WHITE);

        Label tagLabel = new Label("@" + user.getUserTag());
        tagLabel.setFont(Font.font("Segoe UI", 16));
        tagLabel.setTextFill(Color.web("#b9bbbe"));

        VBox userInfoBox = new VBox(5, nameLabel, tagLabel);
        userInfoBox.setAlignment(Pos.CENTER);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Additional Details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10));

        this.getChildren().addAll(avatarStack, userInfoBox, separator, detailsGrid);
    }

    /**
     * Affiche l'initiale de l'utilisateur dans un cercle coloré.
     * Utilisé comme solution de repli si l'image de profil n'est pas disponible ou échoue au chargement.
     */
    private void showInitials(StackPane avatarStack, Circle circle, User user) {
        circle.setFill(Color.web("#5865F2"));
        Label avatarLabel = new Label(user.getName().substring(0, 1).toUpperCase());
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        avatarStack.getChildren().setAll(circle, avatarLabel);
    }
}
