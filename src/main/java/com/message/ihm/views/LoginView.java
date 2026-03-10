package com.message.ihm.views;

import com.message.ihm.controllers.ILoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginView extends StackPane {

    private final ILoginController controller;
    private boolean isRegistrationMode = false;

    // Couleurs Discord-like
    private static final String COLOR_BACKGROUND = "#36393F";
    private static final String COLOR_PANEL = "#2F3136";
    private static final String COLOR_INPUT = "#40444B";
    private static final String COLOR_TEXT = "#DCDDDE";
    private static final String COLOR_ACCENT = "#5865F2";

    private TextField nomField;
    private TextField tagField;
    private PasswordField passField;

    private Label titleLabel;
    private Label subTitleLabel;
    private Label nomLabel;

    private Button btnValider;
    private Button btnSwitch;

    public LoginView(ILoginController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {

        this.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");

        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(40));
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setMaxWidth(350);
        centerPanel.setStyle(
                "-fx-background-color: " + COLOR_PANEL + ";" +
                        "-fx-background-radius: 8;"
        );

        titleLabel = new Label("Bon retour !");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        subTitleLabel = new Label("Nous sommes ravis de vous revoir.");
        subTitleLabel.setTextFill(Color.web("#B9BBBE"));
        subTitleLabel.setFont(Font.font("Segoe UI", 14));

        nomLabel = createLabel("NOM D'AFFICHAGE");
        nomField = createTextField();

        Label tagLabel = createLabel("TAG UTILISATEUR");
        tagField = createTextField();

        Label passLabel = createLabel("MOT DE PASSE");
        passField = createPasswordField();

        btnValider = new Button("Se connecter");
        btnValider.setMaxWidth(Double.MAX_VALUE);
        btnValider.setStyle(
                "-fx-background-color: " + COLOR_ACCENT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 5;"
        );

        btnSwitch = new Button("Besoin d'un compte ? Inscris-toi");
        btnSwitch.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + COLOR_ACCENT + ";"
        );

        btnSwitch.setOnAction(e -> {
            isRegistrationMode = !isRegistrationMode;
            updateViewMode();
        });

        btnValider.setOnAction(e -> handleSubmit());

        centerPanel.getChildren().addAll(
                titleLabel,
                subTitleLabel,
                nomLabel,
                nomField,
                tagLabel,
                tagField,
                passLabel,
                passField,
                btnValider,
                btnSwitch
        );

        updateViewMode();

        this.getChildren().add(centerPanel);
    }

    private void handleSubmit() {
        String tag = tagField.getText();
        String pwd = passField.getText();

        if (isRegistrationMode) {
            String nom = nomField.getText();

            if (nom.isEmpty() || tag.isEmpty() || pwd.isEmpty()) {
                showError("Veuillez remplir tous les champs.");
                return;
            }

            boolean success = controller.registerUser(nom, tag, pwd);
            if (!success) {
                showError("Ce tag existe déjà.");
            }

        } else {

            if (tag.isEmpty() || pwd.isEmpty()) {
                showError("Veuillez remplir tous les champs.");
                return;
            }

            boolean success = controller.authenticate(tag, pwd);
            if (!success) {
                showError("Identifiants incorrects.");
            }
        }
    }

    private void updateViewMode() {
        if (isRegistrationMode) {
            nomLabel.setVisible(true);
            nomLabel.setManaged(true);
            nomField.setVisible(true);
            nomField.setManaged(true);

            btnValider.setText("S'inscrire");
            titleLabel.setText("Créer un compte");
            subTitleLabel.setText("Rejoignez la communauté.");
            btnSwitch.setText("Tu as déjà un compte ? Connecte-toi");
        } else {
            nomLabel.setVisible(false);
            nomLabel.setManaged(false);
            nomField.setVisible(false);
            nomField.setManaged(false);

            btnValider.setText("Se connecter");
            titleLabel.setText("Bon retour !");
            subTitleLabel.setText("Nous sommes ravis de vous revoir.");
            btnSwitch.setText("Besoin d'un compte ? Inscris-toi");
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#B9BBBE"));
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        return label;
    }

    private TextField createTextField() {
        TextField field = new TextField();
        field.setStyle(
                "-fx-background-color: " + COLOR_INPUT + ";" +
                        "-fx-text-fill: " + COLOR_TEXT + ";" +
                        "-fx-background-radius: 5;"
        );
        return field;
    }

    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setStyle(
                "-fx-background-color: " + COLOR_INPUT + ";" +
                        "-fx-text-fill: " + COLOR_TEXT + ";" +
                        "-fx-background-radius: 5;"
        );
        return field;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}