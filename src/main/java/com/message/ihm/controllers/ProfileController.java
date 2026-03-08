package com.message.ihm.controllers;

import com.message.common.Constants;
import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.core.session.ISession;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProfileController implements IProfileController, IDatabaseObserver {

    private final DataManager mDataManager;
    private final ISession mSession;
    private final List<IProfileControllerObserver> mObservers = new ArrayList<>();

    public ProfileController(DataManager dataManager, ISession session) {
        this.mDataManager = dataManager;
        this.mSession = session;
        this.mDataManager.addObserver(this);
    }

    @Override
    public void updateDisplayName(String newName) {
        User currentUser = mSession.getConnectedUser();
        if (currentUser != null && newName != null && !newName.trim().isEmpty()) {
            currentUser.setName(newName);
            mDataManager.updateUser(currentUser);
            // notifyObservers(); // Handled by IDatabaseObserver
        }
    }

    @Override
    public void deleteAccount() {
        User currentUser = mSession.getConnectedUser();
        if (currentUser != null) {
            // 1. Récupérer tous les messages de l'utilisateur
            Set<Message> userMessages = mDataManager.getMessagesFrom(currentUser);

            // 2. Remplacer l'expéditeur par UNKNOWN_USER
            for (Message message : userMessages) {
                // Créer un nouveau message avec l'expéditeur inconnu, mais en gardant le même UUID, date, texte, etc.
                Message anonymizedMessage = new Message(
                    message.getUuid(),
                    Constants.UNKNOWN_USER,
                    message.getRecipient(),
                    message.getEmissionDate(),
                    message.getText()
                );
                mDataManager.sendMessage(anonymizedMessage); // Sauvegarder le message modifié (écrasera l'ancien car même UUID)
            }

            // 3. Déconnecter d'abord (cela va écrire dans le fichier pour mettre Online=false)
            // On garde une référence vers l'utilisateur car disconnect() va mettre mConnectedUser à null
            User userToDelete = currentUser;
            mSession.disconnect();

            // 4. Supprimer le fichier de l'utilisateur
            mDataManager.deleteAccount(userToDelete);
            // notifyObservers(); // Handled by IDatabaseObserver
        }
    }

    @Override
    public User getCurrentUser() {
        return mSession.getConnectedUser();
    }

    @Override
    public void addObserver(IProfileControllerObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(IProfileControllerObserver observer) {
        mObservers.remove(observer);
    }

    private void notifyObservers() {
        for (IProfileControllerObserver observer : mObservers) {
            observer.onProfileUpdated();
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
        // Check if the modified user is the current user
        User currentUser = mSession.getConnectedUser();
        if (currentUser != null && modifiedUser.getUuid().equals(currentUser.getUuid())) {
            notifyObservers();
        }
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        // Not interested
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        // Not interested
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        // Not interested
    }
}
