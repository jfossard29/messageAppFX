package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
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

    public SessionController(DataManager dataManager, MessageAppController mainController) {
        this.mDataManager = dataManager;
        this.mMainController = mainController;
        this.mDataManager.addObserver(this);
    }

    @Override
    public void logout() {
        this.mMainController.logout();
    }

    @Override
    public User getCurrentUser() {
        return this.mMainController.getCurrentUser();
    }

    @Override
    public Set<User> getAllUsers() {
        return this.mDataManager.getUsers();
    }

    @Override
    public void selectChannel(Channel channel) {
        this.mSelectedChannel = channel;
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onChannelSelected(channel);
            }
        });
    }

    @Override
    public Channel getSelectedChannel() {
        return this.mSelectedChannel;
    }

    @Override
    public void addObserver(ISessionControllerObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(ISessionControllerObserver observer) {
        mObservers.remove(observer);
    }

    // ---- Notifications ----

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

    @Override
    public void notifyUserAdded(User addedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        Platform.runLater(() -> {
            for (ISessionControllerObserver observer : mObservers) {
                observer.onUsersUpdated();
            }
        });
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
    }

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
