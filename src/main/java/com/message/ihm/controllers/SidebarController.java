package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SidebarController {

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IProfileController mProfileController;

    public SidebarController(ISessionController sessionController, IChannelController channelController, IProfileController profileController) {
        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mProfileController = profileController;
    }

    public Set<Channel> getChannels() {
        return mChannelController.getAllChannels();
    }

    public Set<User> getUsers() {
        return mSessionController.getAllUsers();
    }

    public User getCurrentUser() {
        return mSessionController.getCurrentUser();
    }

    public void selectChannel(Channel channel) {
        mSessionController.selectChannel(channel);
    }

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

    public void logout() {
        mSessionController.logout();
    }

    // Expose controllers for observation if needed, or add wrapper methods
    public ISessionController getSessionController() {
        return mSessionController;
    }

    public IChannelController getChannelController() {
        return mChannelController;
    }

    public IProfileController getProfileController() {
        return mProfileController;
    }
}
