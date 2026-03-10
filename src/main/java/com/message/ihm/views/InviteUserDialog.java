package com.message.ihm.views;

import com.message.common.Constants;
import com.message.datamodel.Channel;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChannelController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class InviteUserDialog extends Stage {

    private final IChannelController mController;
    private final Set<User> mAvailableUsers;
    private final Channel mCurrentChannel;
    private final Set<User> mSelectedUsers = new HashSet<>();

    private ListView<User> mUsersList;

    public InviteUserDialog(Stage owner, IChannelController controller, Set<User> allUsers, Channel currentChannel) {

        this.mController = controller;
        this.mCurrentChannel = currentChannel;
        
        // Filtrer les utilisateurs déjà dans le canal
        Set<UUID> existingMemberIds = currentChannel.getUsers().stream()
                .map(User::getUuid)
                .collect(Collectors.toSet());

        this.mAvailableUsers = allUsers.stream()
                .filter(u -> !existingMemberIds.contains(u.getUuid()))
                .collect(Collectors.toSet());

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Inviter des amis dans " + currentChannel.getName());

        initGui();
    }

    private void initGui() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgb(54,57,63);");

        /* ===== Label ===== */

        Label titleLabel = new Label("INVITER DES AMIS");
        titleLabel.setTextFill(Color.rgb(185, 187, 190));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        /* ===== Search ===== */

        TextField mSearchUserField = new TextField();
        mSearchUserField.setPromptText("Rechercher un utilisateur...");
        mSearchUserField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: white;
                """);
        mSearchUserField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        /* ===== List ===== */

        mUsersList = new ListView<>();
        mUsersList.setPrefHeight(250);
        mUsersList.setStyle("""
                -fx-control-inner-background: rgb(47,49,54);
                -fx-background-color: rgb(47,49,54);
                """);

        mUsersList.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setText(user.getUserTag());
                    checkBox.setTextFill(Color.rgb(185, 187, 190));
                    
                    checkBox.setSelected(mSelectedUsers.contains(user));
                    
                    checkBox.setOnAction(e -> {
                        if (checkBox.isSelected()) {
                            mSelectedUsers.add(user);
                        } else {
                            mSelectedUsers.remove(user);
                        }
                    });
                    
                    setGraphic(checkBox);
                    setText(null);
                }
            }
        });

        filterUsers("");

        /* ===== Boutons ===== */

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> close());

        Button inviteBtn = new Button("Inviter");
        inviteBtn.setStyle("""
                -fx-background-color: rgb(88,101,242);
                -fx-text-fill: white;
                -fx-padding: 8 20 8 20;
                """);

        inviteBtn.setOnAction(e -> inviteUsers());

        HBox buttonBox = new HBox(10, cancelBtn, inviteBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        /* ===== Layout ===== */

        root.getChildren().addAll(
                titleLabel,
                mSearchUserField,
                mUsersList,
                buttonBox
        );

        Scene scene = new Scene(root, 400, 450);
        setScene(scene);
    }

    private void filterUsers(String query) {
        String lowerQuery = query == null ? "" : query.toLowerCase();
        
        List<User> filtered = mAvailableUsers.stream()
                .filter(u -> {
                    if (u.getUuid().equals(Constants.UNKNONWN_USER_UUID)) {
                        return false;
                    }
                    return u.getUserTag().toLowerCase().contains(lowerQuery) ||
                           (u.getName() != null && u.getName().toLowerCase().contains(lowerQuery));
                })
                .collect(Collectors.toList());
        
        mUsersList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void inviteUsers() {
        if (mSelectedUsers.isEmpty()) {
            close();
            return;
        }

        boolean success = mController.inviteUsers(mCurrentChannel, new ArrayList<>(mSelectedUsers));

        if (success) {
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'invitation.");
            alert.showAndWait();
        }
    }
}