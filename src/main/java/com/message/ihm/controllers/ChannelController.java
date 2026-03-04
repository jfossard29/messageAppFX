package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.session.ISession;
import com.message.datamodel.Channel;
import com.message.datamodel.User;

import java.util.List;
import java.util.Set;

public class ChannelController implements IChannelController {

    private final DataManager mDataManager;
    private final ISession mSession;

    public ChannelController(DataManager dataManager, ISession session) {
        this.mDataManager = dataManager;
        this.mSession = session;
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

        Channel newChannel = new Channel(creator, name, users);
        mDataManager.sendChannel(newChannel);
        return true;
    }

    @Override
    public Set<Channel> getAllChannels() {
        return mDataManager.getChannels();
    }
}
