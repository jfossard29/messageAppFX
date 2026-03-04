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

import java.util.Set;
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
        if (sender != null) {
            Message newMessage = new Message(sender, channel.getUuid(), text);
            mDataManager.sendMessage(newMessage);
        }
    }

    @Override
    public Set<Message> getMessagesForChannel(Channel channel) {
        if (channel == null) {
            return Set.of();
        }
        return mDataManager.getMessages().stream()
                .filter(m -> m.getRecipient().equals(channel.getUuid()))
                .collect(Collectors.toSet());
    }

    @Override
    public void onChannelSelected(Channel channel) {
        this.mCurrentChannel = channel;
        if (channel != null) {
            channelTitle.setText("# " + channel.getName());
            messages.setAll(getMessagesForChannel(channel));
        } else {
            channelTitle.setText("Sélectionnez un canal");
            messages.clear();
        }
    }

    @Override
    public void onUsersUpdated() {
        // Pas d'action directe dans la vue de chat
    }
}
