package com.message.ihm.views;

import com.message.common.Constants;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IChannelController;
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
import java.util.stream.Collectors;

public class AddChannelDialog extends Stage {

    private final IChannelController mController;
    private final Set<User> mAvailableUsers;
    private final User mCurrentUser;
    private final Set<User> mSelectedUsers = new HashSet<>();

    private TextField mNameField;
    private CheckBox mPrivateCheckBox;
    private ListView<User> mUsersList;
    private VBox mUserSelectionBox;

    public AddChannelDialog(Stage owner, IChannelController controller, Set<User> availableUsers, User currentUser) {

        this.mController = controller;
        this.mAvailableUsers = availableUsers;
        this.mCurrentUser = currentUser;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Créer un canal");

        initGui();
    }

    private void initGui() {

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgb(54,57,63);");

        /* ===== Label ===== */

        Label nameLabel = new Label("NOM DU CANAL");
        nameLabel.setTextFill(Color.rgb(185, 187, 190));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        /* ===== Input ===== */

        mNameField = new TextField();
        mNameField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: white;
                """);

        /* ===== Checkbox ===== */

        mPrivateCheckBox = new CheckBox("Canal privé");
        mPrivateCheckBox.setTextFill(Color.WHITE);
        mPrivateCheckBox.setOnAction(e -> toggleUserList(mPrivateCheckBox.isSelected()));

        /* ===== User Selection Area ===== */

        mUserSelectionBox = new VBox(10);
        mUserSelectionBox.setVisible(false);
        mUserSelectionBox.setManaged(false);

        TextField mSearchUserField = new TextField();
        mSearchUserField.setPromptText("Rechercher un utilisateur...");
        mSearchUserField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: white;
                """);
        mSearchUserField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        mUsersList = new ListView<>();
        mUsersList.setPrefHeight(150);
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
                    
                    // Update checkbox state based on selection set
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

        // Initial population
        filterUsers("");

        mUserSelectionBox.getChildren().addAll(mSearchUserField, mUsersList);

        /* ===== Boutons ===== */

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> close());

        Button createBtn = new Button("Créer");
        createBtn.setStyle("""
                -fx-background-color: rgb(88,101,242);
                -fx-text-fill: white;
                -fx-padding: 8 20 8 20;
                """);

        createBtn.setOnAction(e -> createChannel());

        HBox buttonBox = new HBox(10, cancelBtn, createBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        /* ===== Layout ===== */

        root.getChildren().addAll(
                nameLabel,
                mNameField,
                mPrivateCheckBox,
                mUserSelectionBox,
                buttonBox
        );

        Scene scene = new Scene(root, 400, 500);
        setScene(scene);
    }

    private void toggleUserList(boolean show) {
        mUserSelectionBox.setVisible(show);
        mUserSelectionBox.setManaged(show);
        // Resize window if needed, or let layout handle it
        sizeToScene();
    }

    private void filterUsers(String query) {
        String lowerQuery = query == null ? "" : query.toLowerCase();
        
        List<User> filtered = mAvailableUsers.stream()
                .filter(u -> {
                    // Exclude current user
                    if (mCurrentUser != null && u.getUserTag().equals(mCurrentUser.getUserTag())) {
                        return false;
                    }
                    // Exclude unknown user
                    if (u.getUuid().equals(Constants.UNKNONWN_USER_UUID)) {
                        return false;
                    }

                    return u.getUserTag().toLowerCase().contains(lowerQuery) ||
                           (u.getName() != null && u.getName().toLowerCase().contains(lowerQuery));
                })
                .collect(Collectors.toList());
        
        mUsersList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void createChannel() {

        String name = mNameField.getText();
        boolean isPrivate = mPrivateCheckBox.isSelected();
        boolean success;

        if (isPrivate) {
            // Use the set of selected users
            List<User> selectedUsersList = new ArrayList<>(mSelectedUsers);
            success = mController.createChannel(name, selectedUsersList);
        } else {
            success = mController.createChannel(name);
        }

        if (success) {
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la création (nom vide ou existant).");
            alert.showAndWait();
        }
    }
}