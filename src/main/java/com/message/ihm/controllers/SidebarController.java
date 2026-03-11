package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IChannelController;
import com.message.ihm.controllers.interfaces.IProfileController;
import com.message.ihm.controllers.interfaces.ISessionController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SidebarController {

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IProfileController mProfileController;

    /**
     * Contrôleur de la barre latérale qui gère l'affichage des canaux et des utilisateurs connectés.
     * Fait le lien entre la vue SidebarView et les contrôleurs de session et de canaux.
     */
    public SidebarController(ISessionController sessionController, IChannelController channelController, IProfileController profileController) {
        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mProfileController = profileController;
    }

    /**
     * Récupère la liste de tous les canaux disponibles via le contrôleur de canaux.
     * SRS-MAP-USR-001 (Canaux) : L'utilisateur connecté peut consulter la liste des canaux enregistrés.
     * @return L'ensemble des canaux.
     */
    public Set<Channel> getChannels() {
        return mChannelController.getAllChannels();
    }

    /**
     * Récupère la liste de tous les utilisateurs via le contrôleur de session.
     * SRS-MAP-USR-007 : L'utilisateur connecté peut consulter la liste des utilisateurs enregistrés.
     * @return L'ensemble des utilisateurs.
     */
    public Set<User> getUsers() {
        return mSessionController.getAllUsers();
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     * @return L'utilisateur courant.
     */
    public User getCurrentUser() {
        return mSessionController.getCurrentUser();
    }

    /**
     * Sélectionne un canal spécifique pour afficher ses messages.
     * @param channel Le canal à sélectionner.
     */
    public void selectChannel(Channel channel) {
        mSessionController.selectChannel(channel);
    }

    /**
     * Sélectionne ou crée un canal de messagerie directe avec un utilisateur spécifique.
     * Crée un canal temporaire ("fantôme") si nécessaire pour initier la conversation.
     * SRS-MAP-MSG-007 : L'utilisateur connecté peut envoyer un message privé à un utilisateur.
     * @param user L'utilisateur avec qui démarrer la conversation.
     */
    public void selectDmChannel(User user) {
        User currentUser = mSessionController.getCurrentUser();
        if (currentUser != null && !currentUser.getUuid().equals(user.getUuid())) {
            // Création d'un canal "fantôme" pour le DM
            List<User> participants = new ArrayList<>();
            participants.add(currentUser);
            participants.add(user);

            // Le nom est celui de l'autre utilisateur, pour l'affichage dans le header du chat
            Channel phantomChannel = new Channel(currentUser, user.getName(), participants);
            phantomChannel.setDirectMessage(true);

            mSessionController.selectChannel(phantomChannel);
        }
    }

    /**
     * Déconnecte l'utilisateur actuel et retourne à l'écran de connexion.
     * SRS-MAP-USR-005 : L'utilisateur connecté peut se déconnecter de l'application.
     */
    public void logout() {
        mSessionController.logout();
    }

    // Expose controllers for observation if needed, or add wrapper methods
    
    /**
     * Retourne le contrôleur de session associé.
     */
    public ISessionController getSessionController() {
        return mSessionController;
    }

    /**
     * Retourne le contrôleur de canaux associé.
     */
    public IChannelController getChannelController() {
        return mChannelController;
    }

    /**
     * Retourne le contrôleur de profil associé.
     */
    public IProfileController getProfileController() {
        return mProfileController;
    }
}
