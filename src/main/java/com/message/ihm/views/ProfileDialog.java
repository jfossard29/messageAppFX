package com.message.ihm.views;

import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IProfileController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProfileDialog extends Stage {

    private final IProfileController controller;
    private final User currentUser;

    private static final String COLOR_BACKGROUND = "#36393F";
    private static final String COLOR_INPUT = "#40444B";
    private static final String COLOR_ACCENT = "#5865F2";
    private static final String COLOR_DANGER = "#ED4245";

    public ProfileDialog(Stage owner, IProfileController controller, User currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Mon Profil");

        initUI();
    }

    private void initUI() {

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");

        // --- Name Section ---
        Label nameLabel = new Label("NOM D'AFFICHAGE");
        nameLabel.setTextFill(Color.web("#B9BBBE"));
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextField nameField = new TextField(currentUser.getName());
        nameField.setStyle(
                "-fx-background-color: " + COLOR_INPUT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );

        // --- Picture URL Section ---
        Label pictureLabel = new Label("URL DE L'IMAGE DE PROFIL");
        pictureLabel.setTextFill(Color.web("#B9BBBE"));
        pictureLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextField pictureField = new TextField(currentUser.getPicturePath());
        pictureField.setPromptText("https://...");
        pictureField.setStyle(
                "-fx-background-color: " + COLOR_INPUT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );

        // --- Save Button ---
        Button saveBtn = new Button("Enregistrer");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle(
                "-fx-background-color: " + COLOR_ACCENT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 5;"
        );

        saveBtn.setOnAction(e -> {
            controller.updateProfile(nameField.getText(), pictureField.getText());
            close();
        });

        // --- Danger Zone ---
        VBox dangerBox = new VBox(15);
        dangerBox.setPadding(new Insets(15));
        dangerBox.setStyle(
                "-fx-border-color: " + COLOR_DANGER + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 2;"
        );

        Label dangerTitle = new Label("Zone de danger");
        dangerTitle.setTextFill(Color.web(COLOR_DANGER));
        dangerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button deleteBtn = new Button("Supprimer mon compte");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle(
                "-fx-background-color: " + COLOR_DANGER + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 5;"
        );

        deleteBtn.setOnAction(e -> showDeleteConfirmation());

        dangerBox.getChildren().addAll(dangerTitle, deleteBtn);

        root.getChildren().addAll(
                nameLabel,
                nameField,
                pictureLabel,
                pictureField,
                saveBtn,
                dangerBox
        );

        Scene scene = new Scene(root, 400, 420);
        setScene(scene);
    }

    private void showDeleteConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer votre compte ?");
        alert.setContentText(
                "Êtes-vous sûr de vouloir supprimer votre compte ?\n" +
                "Tous vos messages seront anonymisés.\n" +
                "Cette action est irréversible."
        );

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                controller.deleteAccount();
                close();
            }
        });
    }
}
