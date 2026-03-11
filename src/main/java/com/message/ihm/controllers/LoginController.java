package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.ILoginController;

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
     * SRS-MAP-USR-001 : L'utilisateur peut enregistrer un compte utilisateur dans le système (nom, tag).
     * SRS-MAP-USR-002 : Le tag et le nom de l'utilisateur sont obligatoires.
     * SRS-MAP-USR-003 : Le tag correspondant à un utilisateur est unique dans le système.
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
     * SRS-MAP-USR-004 : L'utilisateur peut se connecter sur un compte préalablement enregistré.
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
