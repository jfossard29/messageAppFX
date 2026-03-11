package com.message.ihm.controllers.interfaces;

import com.message.datamodel.Channel;
import com.message.datamodel.User;

import java.util.List;
import java.util.Set;

public interface IChannelController {
    /**
     * Crée un nouveau canal.
     * @param name Nom du canal
     * @return true si la création a réussi
     */
    boolean createChannel(String name);

    /**
     * Crée un nouveau canal privé.
     * @param name Nom du canal
     * @param users Liste des utilisateurs autorisés
     * @return true si la création a réussi
     */
    boolean createChannel(String name, List<User> users);

    /**
     * Invite des utilisateurs dans un canal existant.
     * @param channel Le canal cible.
     * @param users Liste des utilisateurs à ajouter.
     * @return true si l'opération a réussi.
     */
    boolean inviteUsers(Channel channel, List<User> users);

    /**
     * Supprime un canal.
     * @param channel Le canal à supprimer.
     * @return true si l'opération a réussi.
     */
    boolean deleteChannel(Channel channel);

    /**
     * Quitte un canal privé.
     * @param channel Le canal à quitter.
     * @param user L'utilisateur qui quitte.
     * @return true si l'opération a réussi.
     */
    boolean leaveChannel(Channel channel, User user);

    /**
     * Récupère tous les canaux.
     */
    Set<Channel> getAllChannels();

    void addObserver(IChannelControllerObserver observer);
    void removeObserver(IChannelControllerObserver observer);

    interface IChannelControllerObserver {
        void onChannelListUpdated();
    }
}
