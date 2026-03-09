package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.core.session.ISession;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    public boolean inviteUsers(Channel channel, List<User> users) {
        if (channel == null || users == null || users.isEmpty()) {
            return false;
        }

        channel.addUsers(users);
        mDataManager.sendChannel(channel); // Met à jour le canal existant
        return true;
    }

    @Override
    public boolean deleteChannel(Channel channel) {
        if (channel == null) return false;
        mDataManager.deleteChannel(channel);
        return true;
    }

    @Override
    public boolean leaveChannel(Channel channel, User user) {
        if (channel == null || user == null) return false;
        
        channel.removeUser(user);
        mDataManager.sendChannel(channel);
        return true;
    }

    @Override
    public Set<Channel> getAllChannels() {
        return mDataManager.getChannels();
    }

    @Override
    public void addObserver(IChannelControllerObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(IChannelControllerObserver observer) {
        mObservers.remove(observer);
    }

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
