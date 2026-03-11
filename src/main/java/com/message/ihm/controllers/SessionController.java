package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.ISessionController;
import javafx.application.Platform;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SessionController implements ISessionController, IDatabaseObserver {

    private final DataManager mDataManager;
    private final MessageAppController mMainController;
    private Channel mSelectedChannel;
    private final Set<ISessionControllerObserver> mObservers = new HashSet<>();

    /**
     * Manages the user session and related data.
     * @param dataManager The data manager.
     * @param mainController The main application controller.
     */
    public SessionController(DataManager dataManager, MessageAppController mainController) {
        this.mDataManager = dataManager;
        this.mMainController = mainController;
        this.mDataManager.addObserver(this);
    }

    /**
     * Logs out the current user.
     * SRS-MAP-USR-005 : L'utilisateur connecté peut se déconnecter de l'application.
     */
    @Override
    public void logout() {
        this.mMainController.logout();
    }

    /**
     * Gets the currently logged-in user.
     * @return The current user.
     */
    @Override
    public User getCurrentUser() {
        return this.mMainController.getCurrentUser();
    }

    /**
     * Gets all users from the data manager.
     * SRS-MAP-USR-007 : L'utilisateur connecté peut consulter la liste des utilisateurs enregistrés.
     * @return A set of all users.
     */
    @Override
    public Set<User> getAllUsers() {
        return this.mDataManager.getUsers();
    }

    /**
     * Selects a channel and notifies observers.
     * @param channel The channel to select.
     */
    @Override
    public void selectChannel(Channel channel) {
        this.mSelectedChannel = channel;
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onChannelSelected(channel);
            }
        });
    }

    /**
     * Gets the currently selected channel.
     * @return The selected channel.
     */
    @Override
    public Channel getSelectedChannel() {
        return this.mSelectedChannel;
    }

    /**
     * Adds an observer to the session controller.
     * @param observer The observer to add.
     */
    @Override
    public void addObserver(ISessionControllerObserver observer) {
        mObservers.add(observer);
    }

    /**
     * Removes an observer from the session controller.
     * @param observer The observer to remove.
     */
    @Override
    public void removeObserver(ISessionControllerObserver observer) {
        mObservers.remove(observer);
    }

    // ---- Notifications ----

    /**
     * Checks if a message is relevant to the current channel.
     * @param message The message to check.
     * @return True if the message is relevant, false otherwise.
     */
    private boolean isMessageRelevantForCurrentChannel(Message message) {
        if (mSelectedChannel == null) return false;

        // Cas 1 : Canal normal (public ou groupe privé)
        if (!mSelectedChannel.isDirectMessage()) {
            return message.getRecipient().equals(mSelectedChannel.getUuid());
        }

        // Cas 2 : DM (Message Direct)
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        // On cherche l'autre participant du DM
        Optional<User> otherUserOpt = mSelectedChannel.getUsers().stream()
                .filter(u -> !u.getUuid().equals(currentUser.getUuid()))
                .findFirst();

        if (otherUserOpt.isPresent()) {
            User otherUser = otherUserOpt.get();
            UUID otherUserUuid = otherUser.getUuid();
            UUID currentUserUuid = currentUser.getUuid();
            
            UUID senderUuid = message.getSender().getUuid();
            UUID recipientUuid = message.getRecipient();

            // Le message est pertinent si :
            // - Je l'envoie à l'autre (Sender=Moi, Recipient=Lui)
            boolean sentByMeToHim = senderUuid.equals(currentUserUuid) && recipientUuid.equals(otherUserUuid);
            // - L'autre me l'envoie (Sender=Lui, Recipient=Moi)
            boolean sentByHimToMe = senderUuid.equals(otherUserUuid) && recipientUuid.equals(currentUserUuid);
            
            return sentByMeToHim || sentByHimToMe;
        }
        
        return false;
    }

    /**
     * Notifies observers when a message is added.
     * SRS-MAP-CHN-009 : La présence d’un nouveau message dans un canal est signalée par un indicateur graphique.
     * @param addedMessage The added message.
     */
    @Override
    public void notifyMessageAdded(Message addedMessage) {
        // Notification globale pour les alertes
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onMessageReceived(addedMessage);
            }
        });

        // Notification spécifique pour le canal courant
        if (isMessageRelevantForCurrentChannel(addedMessage)) {
            Platform.runLater(() -> {
                for (ISessionControllerObserver observer : mObservers) {
                    observer.onChannelSelected(mSelectedChannel);
                }
            });
        }
    }

    /**
     * Notifies observers when a message is deleted.
     * @param deletedMessage The deleted message.
     */
    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        if (isMessageRelevantForCurrentChannel(deletedMessage)) {
            Platform.runLater(() -> {
                for (ISessionControllerObserver observer : mObservers) {
                    observer.onChannelSelected(mSelectedChannel);
                }
            });
        }
    }

    /**
     * Notifies observers when a message is modified.
     * @param modifiedMessage The modified message.
     */
    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        if (isMessageRelevantForCurrentChannel(modifiedMessage)) {
            Platform.runLater(() -> {
                for (ISessionControllerObserver observer : mObservers) {
                    observer.onChannelSelected(mSelectedChannel);
                }
            });
        }
    }

    /**
     * Notifies observers when a user is added.
     * @param addedUser The added user.
     */
    @Override
    public void notifyUserAdded(User addedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    /**
     * Notifies observers when a user is deleted.
     * @param deletedUser The deleted user.
     */
    @Override
    public void notifyUserDeleted(User deletedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    /**
     * Notifies observers when a user is modified.
     * @param modifiedUser The modified user.
     */
    @Override
    public void notifyUserModified(User modifiedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    /**
     * Notifies observers when a channel is added.
     * @param addedChannel The added channel.
     */
    @Override
    public void notifyChannelAdded(Channel addedChannel) {
    }

    /**
     * Notifies observers when a channel is deleted.
     * @param deletedChannel The deleted channel.
     */
    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        if (mSelectedChannel != null && deletedChannel.getUuid().equals(mSelectedChannel.getUuid())) {
            this.mSelectedChannel = null;
            Platform.runLater(() -> {
                for (ISessionControllerObserver observer : mObservers) {
                    observer.onChannelSelected(null);
                }
            });
        }
    }

    /**
     * Notifies observers when a channel is modified.
     * @param modifiedChannel The modified channel.
     */
    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        if (mSelectedChannel != null && modifiedChannel.getUuid().equals(mSelectedChannel.getUuid())) {
            this.mSelectedChannel = modifiedChannel;
            Platform.runLater(() -> {
                for (ISessionControllerObserver observer : mObservers) {
                    observer.onChannelSelected(mSelectedChannel);
                }
            });
        }
    }
}
