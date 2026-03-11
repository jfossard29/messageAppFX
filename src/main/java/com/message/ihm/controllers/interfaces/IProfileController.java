package com.message.ihm.controllers.interfaces;

import com.message.datamodel.User;

public interface IProfileController {
    /**
     * Met à jour le profil de l'utilisateur courant.
     * @param newName Le nouveau nom.
     * @param newPictureUrl La nouvelle URL de l'image de profil.
     */
    void updateProfile(String newName, String newPictureUrl);

    /**
     * Supprime le compte de l'utilisateur courant.
     */
    void deleteAccount();

    /**
     * Récupère l'utilisateur courant.
     * @return L'utilisateur connecté.
     */
    User getCurrentUser();

    void addObserver(IProfileControllerObserver observer);
    void removeObserver(IProfileControllerObserver observer);

    interface IProfileControllerObserver {
        void onProfileUpdated();
    }
}
