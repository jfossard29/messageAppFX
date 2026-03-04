package com.message.ihm.controllers;

import com.message.datamodel.Channel;
import com.message.datamodel.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.Set;

public class SidebarController implements ISessionController.ISessionControllerObserver {

    @FXML private ListView<Channel> channelListView;
    @FXML private ListView<User> userListView;
    @FXML private TextField searchField;
    @FXML private Label currentUserLabel;
    @FXML private Button addChannelButton;
    @FXML private Button profileButton;

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IProfileController mProfileController;

    private ObservableList<Channel> channels = FXCollections.observableArrayList();
    private ObservableList<User> users = FXCollections.observableArrayList();

    public SidebarController(ISessionController sessionController, IChannelController channelController, IProfileController profileController) {
        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mProfileController = profileController;
        this.mSessionController.addObserver(this);
    }

    @FXML
    public void initialize() {
        channelListView.setItems(channels);
        userListView.setItems(users);

        updateLists();
        updateCurrentUser();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterLists(newValue));

        channelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mSessionController.selectChannel(newValue);
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
        // Sélectionner le canal dans la liste si ce n'est pas déjà fait
        if (channel != null && !channel.equals(channelListView.getSelectionModel().getSelectedItem())) {
            channelListView.getSelectionModel().select(channel);
        }
    }

    @Override
    public void onUsersUpdated() {
        updateLists();
    }
}
