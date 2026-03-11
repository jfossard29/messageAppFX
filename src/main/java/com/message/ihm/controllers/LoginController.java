package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.datamodel.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController implements ILoginController {

    private final DataManager mDataManager;
    private final MessageAppController mMainController;

    /**
     * Manages user login and registration.
     * @param dataManager The data manager for handling user data.
     * @param mainController The main controller of the application.
     */
    public LoginController(DataManager dataManager, MessageAppController mainController) {
        this.mDataManager = dataManager;
        this.mMainController = mainController;
    }

    /**
     * Registers a new user.
     * @param name The name of the user.
     * @param tag The user's tag.
     * @param password The user's password.
     * @return True if registration is successful, false otherwise.
     */
    @Override
    public boolean registerUser(String name, String tag, String password) {
        // Vérifie si le tag utilisateur existe déjà
        for (User user : mDataManager.getUsers()) {
            if (user.getUserTag().equals(tag)) {
                return false; // Le tag est déjà pris
            }
        }
        // Crée un nouvel utilisateur et le notifie au DataManager
        User newUser = new User(tag, password, name);
        mDataManager.sendUser(newUser);
        
        // Notifie le contrôleur principal du succès de la connexion
        mMainController.loginSuccess(newUser);
        return true;
    }

    /**
     * Authenticates a user.
     * @param tag The user's tag.
     * @param password The user's password.
     * @return True if authentication is successful, false otherwise.
     */
    @Override
    public boolean authenticate(String tag, String password) {
        // Cherche un utilisateur correspondant au tag et au mot de passe
        for (User user : mDataManager.getUsers()) {
            if (user.getUserTag().equals(tag) && user.getUserPassword().equals(password)) {
                mMainController.loginSuccess(user);
                return true; // Authentification réussie
            }
        }
        return false; // Échec de l'authentification
    }

}
