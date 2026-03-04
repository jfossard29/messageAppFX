package com.message.ihm.controllers;

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
     * Récupère tous les canaux.
     */
    Set<Channel> getAllChannels();
}
