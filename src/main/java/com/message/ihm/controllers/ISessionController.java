package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;

import java.util.Set;

/**
 * Interface pour le contrôleur de la vue principale (session).
 */
public interface ISessionController {
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
    Set<User> getAllUsers();

    /**
     * Sélectionne un canal pour afficher ses messages.
     * @param channel Le canal à sélectionner.
     */
    void selectChannel(Channel channel);

    /**
     * Récupère le canal actuellement sélectionné.
     * @return Le canal sélectionné ou null.
     */
    Channel getSelectedChannel();

    /**
     * Ajoute un observateur pour les changements de sélection de canal.
     * @param observer L'observateur à ajouter.
     */
    void addObserver(ISessionControllerObserver observer);
    
    /**
     * Supprime un observateur.
     * @param observer L'observateur à supprimer.
     */
    void removeObserver(ISessionControllerObserver observer);
    
    interface ISessionControllerObserver {
        void onChannelSelected(Channel channel);
        void onUsersUpdated();
        void onMessageReceived(Message message);
    }
}
