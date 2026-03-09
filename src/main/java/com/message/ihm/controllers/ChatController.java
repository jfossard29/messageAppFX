package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.session.ISession;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatController implements IChatController, ISessionController.ISessionControllerObserver {

    @FXML private Label channelTitle;
    @FXML private ListView<Message> messageListView;
    @FXML private TextField messageField;

    private final DataManager mDataManager;
    private final ISession mSession;
    private Channel mCurrentChannel;

    private ObservableList<Message> messages = FXCollections.observableArrayList();

    public ChatController(DataManager dataManager, ISession session) {
        this.mDataManager = dataManager;
        this.mSession = session;
    }

    @FXML
    public void initialize() {
        messageListView.setItems(messages);
        // TODO: Ajouter un CellFactory pour personnaliser l'affichage des messages
    }

    @FXML
    public void onSendMessage() {
        String text = messageField.getText();
        if (text != null && !text.isEmpty() && mCurrentChannel != null) {
            sendMessage(text, mCurrentChannel);
            messageField.clear();
        }
    }

    @Override
    public void sendMessage(String text, Channel channel) {
        User sender = mSession.getConnectedUser();
        if (sender == null || channel == null) return;

        UUID recipientUuid;
        
        if (channel.isDirectMessage()) {
            // Pour un DM, le destinataire est l'autre utilisateur.
            Optional<User> otherUser = channel.getUsers().stream()
                    .filter(u -> !u.getUuid().equals(sender.getUuid()))
                    .findFirst();
            
            if (otherUser.isPresent()) {
                recipientUuid = otherUser.get().getUuid();
            } else {
                return; // Ne peut pas envoyer de message dans un DM sans autre participant
            }
        } else {
            // Pour un canal normal, le destinataire est le canal lui-même.
            recipientUuid = channel.getUuid();
        }

        Message newMessage = new Message(sender, recipientUuid, text);
        mDataManager.sendMessage(newMessage);
    }

    @Override
    public void deleteMessage(Message message) {
        if (message != null) {
            message.setText("Message supprimé.");
            mDataManager.sendMessage(message);
        }
    }

    @Override
    public Set<Message> getMessagesForChannel(Channel channel) {
        if (channel == null) {
            return Set.of();
        }

        // Cas 1 : Le canal est un Message Direct (DM)
        if (channel.isDirectMessage()) {
            final User currentUser = mSession.getConnectedUser();
            if (currentUser == null) return Set.of();

            // On identifie l'autre participant du DM
            final Optional<User> otherUserOpt = channel.getUsers().stream()
                    .filter(u -> !u.getUuid().equals(currentUser.getUuid()))
                    .findFirst();

            if (otherUserOpt.isEmpty()) return Set.of(); // Pas d'autre participant, pas de messages.
            
            final UUID otherUserUuid = otherUserOpt.get().getUuid();
            final UUID currentUserUuid = currentUser.getUuid();

            // On filtre tous les messages de la base de données
            return mDataManager.getMessages().stream()
                    .filter(message -> {
                        UUID senderUuid = message.getSender().getUuid();
                        UUID recipientUuid = message.getRecipient();

                        // Le message est pertinent s'il est de moi vers l'autre
                        boolean meToOther = senderUuid.equals(currentUserUuid) && recipientUuid.equals(otherUserUuid);
                        // Ou de l'autre vers moi
                        boolean otherToMe = senderUuid.equals(otherUserUuid) && recipientUuid.equals(currentUserUuid);

                        return meToOther || otherToMe;
                    })
                    .collect(Collectors.toSet());
        } 
        // Cas 2 : Le canal est un canal normal (public ou groupe privé)
        else {
            final UUID channelUuid = channel.getUuid();
            return mDataManager.getMessages().stream()
                    .filter(m -> m.getRecipient().equals(channelUuid))
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void onChannelSelected(Channel channel) {
        this.mCurrentChannel = channel;
        if (channel != null) {
            // La logique d'affichage du titre est dans ChatView, ici on met juste à jour les messages
            messages.setAll(getMessagesForChannel(channel));
        } else {
            messages.clear();
        }
    }

    @Override
    public void onUsersUpdated() {
        // Pas d'action directe dans la vue de chat
    }

    @Override
    public void onMessageReceived(Message message) {
        // Pas d'action nécessaire ici, géré par onChannelSelected si pertinent
    }
}
