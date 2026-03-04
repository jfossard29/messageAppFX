package com.message.ihm.controllers;

import com.message.datamodel.User;

public interface IProfileController {
    /**
     * Modifie le nom d'affichage de l'utilisateur courant.
     * @param newName Le nouveau nom.
     */
    void updateDisplayName(String newName);

    /**
     * Supprime le compte de l'utilisateur courant.
     */
    void deleteAccount();

    /**
     * Récupère l'utilisateur courant.
     * @return L'utilisateur connecté.
     */
    User getCurrentUser();
}
