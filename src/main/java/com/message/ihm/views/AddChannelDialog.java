package com.message.ihm.views;

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

import java.util.List;
import java.util.Set;

public class AddChannelDialog extends Stage {

    private final IChannelController mController;
    private final Set<User> mAvailableUsers;

    private TextField mNameField;
    private CheckBox mPrivateCheckBox;
    private ListView<User> mUsersList;

    public AddChannelDialog(Stage owner, IChannelController controller, Set<User> availableUsers) {

        this.mController = controller;
        this.mAvailableUsers = availableUsers;

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

        /* ===== Liste utilisateurs ===== */

        mUsersList = new ListView<>();
        mUsersList.setItems(FXCollections.observableArrayList(mAvailableUsers));
        mUsersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mUsersList.setVisible(false);
        mUsersList.setManaged(false);

        mUsersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getUserTag());
                }
            }
        });

        mUsersList.setStyle("""
                -fx-control-inner-background: rgb(47,49,54);
                -fx-text-fill: rgb(185,187,190);
                """);

        mPrivateCheckBox.setOnAction(e -> toggleUserList(mPrivateCheckBox.isSelected()));

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
                mUsersList,
                buttonBox
        );

        Scene scene = new Scene(root, 400, 400);
        setScene(scene);
    }

    private void toggleUserList(boolean show) {
        mUsersList.setVisible(show);
        mUsersList.setManaged(show);
    }

    private void createChannel() {

        String name = mNameField.getText();
        boolean isPrivate = mPrivateCheckBox.isSelected();
        boolean success;

        if (isPrivate) {
            List<User> selectedUsers = mUsersList.getSelectionModel().getSelectedItems();
            success = mController.createChannel(name, selectedUsers);
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