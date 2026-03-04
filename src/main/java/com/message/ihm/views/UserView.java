package com.message.ihm.views;

import com.message.datamodel.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 * Vue représentant le profil d'un utilisateur (avatar, nom, tag) en JavaFX.
 */
public class UserView extends HBox {

    private final Color COLOR_TEXT = Color.web("#DCDCDE");
    private final Color COLOR_TEXT_MUTED = Color.web("#8E9297");

    public UserView(User user) {
        this.initGui(user);
    }

    private void initGui(User user) {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setBackground(Background.EMPTY);

        // Avatar circulaire
        Circle avatarCircle = new Circle(20, Color.web("#5865F2")); // Rayon 20
        Label avatarLabel = new Label(user.getName().substring(0, 1).toUpperCase());
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setFont(Font.font("Segoe UI", 20));
        StackPane avatarStack = new StackPane(avatarCircle, avatarLabel);

        // VBox pour nom et tag
        VBox userInfoBox = new VBox(2);
        Label nameLabel = new Label(user.getName());
        nameLabel.setFont(Font.font("Segoe UI", 16));
        nameLabel.setTextFill(COLOR_TEXT);

        Label tagLabel = new Label("@" + user.getUserTag());
        tagLabel.setFont(Font.font("Segoe UI", 12));
        tagLabel.setTextFill(COLOR_TEXT_MUTED);

        userInfoBox.getChildren().addAll(nameLabel, tagLabel);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(avatarStack, userInfoBox);
    }
}