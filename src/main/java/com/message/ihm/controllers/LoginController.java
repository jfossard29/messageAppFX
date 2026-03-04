package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.datamodel.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController implements ILoginController {

    @FXML private Label nameLabel;
    @FXML private TextField nameField;
    @FXML private TextField tagField;
    @FXML private PasswordField passwordField;
    @FXML private Button actionButton;
    @FXML private Button switchModeButton;

    private final DataManager mDataManager;
    private final MessageAppController mMainController;
    private boolean isRegistrationMode = false;

    public LoginController(DataManager dataManager, MessageAppController mainController) {
        this.mDataManager = dataManager;
        this.mMainController = mainController;
    }

    @FXML
    public void initialize() {
        updateViewMode();
    }

    @FXML
    public void onAction() {
        String tag = tagField.getText();
        String password = passwordField.getText();

        if (isRegistrationMode) {
            String name = nameField.getText();
            if (name.isEmpty() || tag.isEmpty() || password.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }
            if (!registerUser(name, tag, password)) {
                showAlert("Erreur", "Ce tag utilisateur est déjà pris.");
            }
        } else {
            if (tag.isEmpty() || password.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer votre tag et mot de passe.");
                return;
            }
            if (!authenticate(tag, password)) {
                showAlert("Erreur", "Tag ou mot de passe incorrect.");
            }
        }
    }

    @FXML
    public void onSwitchMode() {
        isRegistrationMode = !isRegistrationMode;
        updateViewMode();
    }

    private void updateViewMode() {
        nameLabel.setVisible(isRegistrationMode);
        nameField.setVisible(isRegistrationMode);

        if (isRegistrationMode) {
            actionButton.setText("S'inscrire");
            switchModeButton.setText("Déjà un compte ? Connectez-vous");
        } else {
            actionButton.setText("Se connecter");
            switchModeButton.setText("Besoin d'un compte ? Inscrivez-vous");
        }
    }

    @Override
    public boolean registerUser(String name, String tag, String password) {
        for (User user : mDataManager.getUsers()) {
            if (user.getUserTag().equals(tag)) {
                return false;
            }
        }
        User newUser = new User(tag, password, name);
        mDataManager.sendUser(newUser);
        mMainController.loginSuccess(newUser);
        return true;
    }

    @Override
    public boolean authenticate(String tag, String password) {
        for (User user : mDataManager.getUsers()) {
            if (user.getUserTag().equals(tag) && user.getUserPassword().equals(password)) {
                mMainController.loginSuccess(user);
                return true;
            }
        }
        return false;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
