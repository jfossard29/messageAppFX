package com.message.ihm;

import com.message.datamodel.User;

import java.util.Set;

/**
 * Interface pour écouter les événements de la vue principale.
 */
public interface IMessageAppMainViewListener {
    
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

    /**
     * Déconnecte l'utilisateur courant.
     */
    void logout();

    /**
     * Récupère l'utilisateur courant.
     * @return L'utilisateur connecté ou null.
     */
    User getCurrentUser();

    /**
     * Récupère la liste des utilisateurs enregistrés.
     * @return La liste des utilisateurs.
     */
    Set<User> getAllUser();
}
