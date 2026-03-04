package com.message.ihm.controllers;

/**
 * Interface pour le contrôleur de la vue de connexion.
 */
public interface ILoginController {
    /**
     * Tente d'enregistrer un nouvel utilisateur.
     * @return true si l'enregistrement a réussi, false sinon.
     */
    boolean registerUser(String name, String tag, String password);

    /**
     * Tente d'authentifier un utilisateur.
     * @return true si l'authentification a réussi, false sinon.
     */
    boolean authenticate(String tag, String password);
}
