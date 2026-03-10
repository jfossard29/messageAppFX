package com.message.ihm.views;

import com.message.datamodel.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProfileView extends VBox {

    public ProfileView(User user) {
        super(20);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #2f3136;"); // Discord-like dark background

        // Avatar
        Circle avatarCircle = new Circle(50, Color.web("#5865F2")); // Larger avatar
        Label avatarLabel = new Label(user.getName().substring(0, 1).toUpperCase());
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        StackPane avatarStack = new StackPane(avatarCircle, avatarLabel);
        avatarStack.setPadding(new Insets(10));

        // User Info
        Label nameLabel = new Label(user.getName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.WHITE);

        Label tagLabel = new Label("@" + user.getUserTag());
        tagLabel.setFont(Font.font("Segoe UI", 16));
        tagLabel.setTextFill(Color.web("#b9bbbe")); // Lighter gray for tag

        VBox userInfoBox = new VBox(5, nameLabel, tagLabel);
        userInfoBox.setAlignment(Pos.CENTER);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Additional Details (example)
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10));


        this.getChildren().addAll(avatarStack, userInfoBox, separator, detailsGrid);
    }
}
