package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;

import java.util.Set;

/**
 * Interface pour le contrôleur de chat.
 * Gère l'envoi et la récupération des messages.
 */
public interface IChatController {

    /**
     * Envoie un message dans un canal donné.
     * @param text Le contenu du message.
     * @param channel Le canal destinataire.
     */
    void sendMessage(String text, Channel channel);

    /**
     * Supprime un message (soft delete).
     * @param message Le message à supprimer.
     */
    void deleteMessage(Message message);

    /**
     * Récupère les messages d'un canal donné.
     * @param channel Le canal dont on veut les messages.
     * @return La liste des messages.
     */
    Set<Message> getMessagesForChannel(Channel channel);
}
