package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SidebarController implements ISessionController.ISessionControllerObserver {

    @FXML private ListView<Channel> channelListView;
    @FXML private ListView<User> userListView;
    @FXML private TextField searchField;
    @FXML private Label currentUserLabel;
    @FXML private Button addChannelButton;
    @FXML private Button profileButton;

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    // mProfileController supprimé car inutilisé

    private ObservableList<Channel> channels = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();

    public SidebarController(ISessionController sessionController, IChannelController channelController, IProfileController profileController) {
        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        // mProfileController ignoré
        this.mSessionController.addObserver(this);
    }

    @FXML
    public void initialize() {
        channelListView.setItems(channels);
        userListView.setItems(users);

        updateLists();
        updateCurrentUser();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterLists(newValue));

        // Gère la sélection d'un canal public ou de groupe
        channelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                userListView.getSelectionModel().clearSelection();
                mSessionController.selectChannel(newValue);
            }
        });

        // Gère le clic sur un utilisateur pour ouvrir un chat privé
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
            if (selectedUser != null) {
                channelListView.getSelectionModel().clearSelection();
                User currentUser = mSessionController.getCurrentUser();
                if (currentUser != null && !currentUser.getUuid().equals(selectedUser.getUuid())) {
                    
                    // Création d'un canal "fantôme" pour le DM
                    List<User> participants = new ArrayList<>();
                    participants.add(currentUser);
                    participants.add(selectedUser);
                    
                    // Le nom est celui de l'autre utilisateur, pour l'affichage dans le header du chat
                    Channel phantomChannel = new Channel(currentUser, selectedUser.getName(), participants);
                    phantomChannel.setDirectMessage(true);
                    
                    mSessionController.selectChannel(phantomChannel);
                }
            }
        });

        // Personnalise l'affichage des noms de canaux
        channelListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Channel channel, boolean empty) {
                super.updateItem(channel, empty);
                if (empty || channel == null) {
                    setText(null);
                } else {
                    // Pour les canaux normaux, on affiche #nom
                    setText("# " + channel.getName());
                }
            }
        });
    }

    @FXML
    public void onAddChannel() {
        // TODO: Ouvrir une boîte de dialogue pour créer un canal
        System.out.println("Ajouter un canal");
    }

    @FXML
    public void onProfile() {
        // TODO: Ouvrir une boîte de dialogue pour le profil
        System.out.println("Ouvrir le profil");
    }

    @FXML
    public void onLogout() {
        mSessionController.logout();
    }

    private void updateLists() {
        Set<Channel> allChannels = mChannelController.getAllChannels();
        Set<User> allUsers = mSessionController.getAllUsers();

        // La liste des canaux n'affiche que les canaux persistants (non-DM)
        channels.setAll(allChannels);
        
        users.setAll(allUsers);
    }

    private void updateCurrentUser() {
        User currentUser = mSessionController.getCurrentUser();
        if (currentUser != null) {
            currentUserLabel.setText(currentUser.getUserTag());
        }
    }

    private void filterLists(String query) {
        if (query == null || query.isEmpty()) {
            updateLists();
            return;
        }

        String lowerQuery = query.toLowerCase();

        Set<Channel> allChannels = mChannelController.getAllChannels();
        channels.setAll(allChannels.stream()
                .filter(c -> c.getName().toLowerCase().contains(lowerQuery))
                .toList());

        Set<User> allUsers = mSessionController.getAllUsers();
        users.setAll(allUsers.stream()
                .filter(u -> u.getUserTag().toLowerCase().contains(lowerQuery) || u.getName().toLowerCase().contains(lowerQuery))
                .toList());
    }

    @Override
    public void onChannelSelected(Channel channel) {
        if (channel == null) {
            channelListView.getSelectionModel().clearSelection();
            userListView.getSelectionModel().clearSelection();
            return;
        }

        if (channel.isDirectMessage()) {
            channelListView.getSelectionModel().clearSelection();
            
            User currentUser = mSessionController.getCurrentUser();
            if (currentUser != null) {
                Optional<User> otherUser = channel.getUsers().stream()
                        .filter(u -> !u.getUuid().equals(currentUser.getUuid()))
                        .findFirst();
                otherUser.ifPresent(user -> {
                    if (!user.equals(userListView.getSelectionModel().getSelectedItem())) {
                        userListView.getSelectionModel().select(user);
                    }
                });
            }
        } else {
            userListView.getSelectionModel().clearSelection();
            if (!channel.equals(channelListView.getSelectionModel().getSelectedItem())) {
                channelListView.getSelectionModel().select(channel);
            }
        }
    }

    @Override
    public void onUsersUpdated() {
        updateLists();
    }

    @Override
    public void onMessageReceived(Message message) {
        // Pas d'action nécessaire ici, géré par SidebarView
    }
}
