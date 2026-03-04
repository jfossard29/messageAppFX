package com.message.ihm.views;

import com.message.datamodel.User;
import com.message.ihm.controllers.IProfileController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProfileView {

    private final IProfileController mController;
    private TextField mNameField;

    public ProfileView(Stage owner, IProfileController controller) {
        this.mController = controller;
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Mon Profil");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #36393F;");

        // Titre
        Label titleLabel = new Label("MON PROFIL");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Segoe UI", 18));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Label Nom
        Label nameLabel = new Label("NOM D'AFFICHAGE");
        nameLabel.setTextFill(Color.web("#B9BBC0"));
        nameLabel.setFont(Font.font("Segoe UI", 12));

        // Input Nom
        mNameField = new TextField();
        mNameField.setStyle("-fx-background-color: #40444B; -fx-text-fill: white; -fx-padding: 10;");
        User currentUser = mController.getCurrentUser();
        if (currentUser != null) {
            mNameField.setText(currentUser.getName());
        }

        // Bouton Sauvegarder
        Button saveBtn = new Button("Sauvegarder");
        saveBtn.setStyle(
                "-fx-background-color: #5865F2; -fx-text-fill: white; -fx-padding: 10 20 10 20;"
        );
        saveBtn.setOnAction(e -> {
            String newName = mNameField.getText();
            if (newName == null || newName.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom ne peut pas être vide.", ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
            } else {
                mController.updateDisplayName(newName);
                dialog.close();
            }
        });

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #4F545C;");

        // Section Danger Zone
        Label dangerLabel = new Label("ZONE DANGEREUSE");
        dangerLabel.setTextFill(Color.web("#ED4245"));
        dangerLabel.setFont(Font.font("Segoe UI", 12));

        // Bouton Supprimer Compte
        Button deleteBtn = new Button("Supprimer mon compte");
        deleteBtn.setStyle(
                "-fx-background-color: #ED4245; -fx-text-fill: white; -fx-padding: 10 20 10 20;"
        );
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.",
                    ButtonType.YES, ButtonType.NO);
            confirm.initOwner(dialog);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    mController.deleteAccount();
                    dialog.close();
                }
            });
        });

        root.getChildren().addAll(
                titleLabel,
                nameLabel,
                mNameField,
                saveBtn,
                separator,
                dangerLabel,
                deleteBtn
        );

        Scene scene = new Scene(root, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}