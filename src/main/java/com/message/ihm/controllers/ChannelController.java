package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.core.session.ISession;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IChannelController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChannelController implements IChannelController, IDatabaseObserver {

    private final DataManager mDataManager;
    private final ISession mSession;
    private final List<IChannelControllerObserver> mObservers = new ArrayList<>();

    public ChannelController(DataManager dataManager, ISession session) {
        this.mDataManager = dataManager;
        this.mSession = session;
        this.mDataManager.addObserver(this);
    }

    @Override
    /**
     * Crée un nouveau canal public avec le nom spécifié.
     * Le créateur est l'utilisateur connecté actuel.
     * SRS-MAP-CHN-003 : L'utilisateur connecté peut créer un canal public.
     */
    public boolean createChannel(String name) {
        User creator = mSession.getConnectedUser();
        if (creator == null || name == null || name.trim().isEmpty()) {
            return false;
        }

        // Vérifier si le canal existe déjà
        for (Channel c : mDataManager.getChannels()) {
            if (c.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        Channel newChannel = new Channel(creator, name);
        mDataManager.sendChannel(newChannel);
        return true;
    }

    @Override
    /**
     * Crée un nouveau canal privé avec le nom spécifié et une liste d'utilisateurs initiaux.
     * Le créateur est automatiquement ajouté à la liste des utilisateurs.
     * SRS-MAP-CHN-004 : L'utilisateur connecté peut créer un canal privé.
     */
    public boolean createChannel(String name, List<User> users) {
        User creator = mSession.getConnectedUser();
        if (creator == null || name == null || name.trim().isEmpty()) {
            return false;
        }

        // Vérifier si le canal existe déjà
        for (Channel c : mDataManager.getChannels()) {
            if (c.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        // Ajoute le créateur à la liste des utilisateurs autorisés s'il n'y est pas déjà
        if (!users.contains(creator)) {
            users.add(creator);
        }

        Channel newChannel = new Channel(creator, name, users);
        mDataManager.sendChannel(newChannel);
        return true;
    }

    @Override
    /**
     * Invite des utilisateurs existants à rejoindre un canal.
     * Met à jour la liste des utilisateurs du canal et synchronise avec la base de données.
     * SRS-MAP-CHN-007 : L'utilisateur connecté peut ajouter un utilisateur à un canal privé dont il est le propriétaire.
     */
    public boolean inviteUsers(Channel channel, List<User> users) {
        if (channel == null || users == null || users.isEmpty()) {
            return false;
        }

        channel.addUsers(users);
        mDataManager.sendChannel(channel); // Met à jour le canal existant
        return true;
    }

    @Override
    /**
     * Supprime un canal existant.
     * Cette action est généralement réservée à l'administrateur ou au créateur du canal.
     * SRS-MAP-CHN-006 : L'utilisateur connecté peut supprimer un canal privé dont il est le propriétaire.
     */
    public boolean deleteChannel(Channel channel) {
        if (channel == null) return false;
        mDataManager.deleteChannel(channel);
        return true;
    }

    @Override
    /**
     * Permet à un utilisateur de quitter un canal.
     * L'utilisateur est retiré de la liste des membres du canal.
     * SRS-MAP-CHN-005 : L'utilisateur connecté peut quitter un canal privé dont il n’est pas le propriétaire.
     * SRS-MAP-CHN-087 : L'utilisateur connecté peut supprimer un utilisateur d’un canal privé dont il est le propriétaire.
     */
    public boolean leaveChannel(Channel channel, User user) {
        if (channel == null || user == null) return false;
        
        channel.removeUser(user);
        mDataManager.sendChannel(channel);
        return true;
    }

    @Override
    /**
     * Récupère l'ensemble de tous les canaux disponibles.
     * Inclut les canaux publics et ceux auxquels l'utilisateur a accès.
     * SRS-MAP-USR-001 (Canaux) : L'utilisateur connecté peut consulter la liste des canaux enregistrés.
     */
    public Set<Channel> getAllChannels() {
        return mDataManager.getChannels();
    }

    @Override
    /**
     * Ajoute un observateur pour être notifié des changements de la liste des canaux.
     */
    public void addObserver(IChannelControllerObserver observer) {
        mObservers.add(observer);
    }

    @Override
    /**
     * Retire un observateur de la liste des notifications.
     */
    public void removeObserver(IChannelControllerObserver observer) {
        mObservers.remove(observer);
    }

    /**
     * Notifie tous les observateurs enregistrés qu'une mise à jour de la liste des canaux a eu lieu.
     */
    private void notifyObservers() {
        for (IChannelControllerObserver observer : mObservers) {
            observer.onChannelListUpdated();
        }
    }

    // IDatabaseObserver implementation

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        // Not interested
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        // Not interested
    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        // Not interested
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        // Not interested
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        // Not interested
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        // Not interested
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        notifyObservers();
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        notifyObservers();
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        notifyObservers();
    }
}
