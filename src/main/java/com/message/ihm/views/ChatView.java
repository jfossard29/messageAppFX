package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChannelController;
import com.message.ihm.controllers.IChatController;
import com.message.ihm.controllers.ISessionController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatView extends BorderPane implements ISessionController.ISessionControllerObserver {

    private final ISessionController mSessionController;
    private final IChatController mChatController;
    private final IChannelController mChannelController;

    private Label mChannelTitle;
    private Button mInviteButton;
    private Button mLeaveButton;
    private Button mDeleteButton;
    private VBox mMessagesBox;
    private ScrollPane mScrollPane;
    private TextField mInputField;
    private Label mCharCounter;
    private Button mSendButton;

    private static final int MAX_CHARS = 200;

    public ChatView(ISessionController sessionController, IChatController chatController, IChannelController channelController) {
        this.mSessionController = sessionController;
        this.mChatController = chatController;
        this.mChannelController = channelController;
        this.mSessionController.addObserver(this);
        initGui();
    }

    private void initGui() {

        // Couleurs
        Color COLOR_MAIN = Color.rgb(54, 57, 63);
        Color COLOR_HEADER = Color.rgb(47, 49, 54);
        Color COLOR_TEXT = Color.rgb(220, 221, 222);

        this.setStyle("-fx-background-color: rgb(54,57,63);");

        /* ================= HEADER ================= */

        mChannelTitle = new Label("Sélectionnez un canal");
        mChannelTitle.setTextFill(COLOR_TEXT);
        mChannelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        mChannelTitle.setPadding(new Insets(15));

        // Bouton Inviter
        mInviteButton = new Button("Inviter...");
        mInviteButton.setStyle("""
                -fx-background-color: rgb(88,101,242);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);
        mInviteButton.setVisible(false);
        mInviteButton.setManaged(false);
        mInviteButton.setOnAction(e -> openInviteDialog());

        // Bouton Quitter
        mLeaveButton = new Button("Quitter");
        mLeaveButton.setStyle("""
                -fx-background-color: rgb(237,66,69);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);
        mLeaveButton.setVisible(false);
        mLeaveButton.setManaged(false);
        mLeaveButton.setOnAction(e -> confirmLeaveChannel());

        // Bouton Supprimer
        mDeleteButton = new Button("Supprimer");
        mDeleteButton.setStyle("""
                -fx-background-color: rgb(237,66,69);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);
        mDeleteButton.setVisible(false);
        mDeleteButton.setManaged(false);
        mDeleteButton.setOnAction(e -> confirmDeleteChannel());

        HBox header = new HBox(10, mChannelTitle, new Region(), mInviteButton, mLeaveButton, mDeleteButton);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setStyle("-fx-background-color: rgb(47,49,54);");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 15, 0, 0));

        this.setTop(header);

        /* ================= MESSAGES ================= */

        mMessagesBox = new VBox(10);
        mMessagesBox.setPadding(new Insets(10));
        mMessagesBox.setFillWidth(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        mMessagesBox.getChildren().add(spacer);

        mScrollPane = new ScrollPane(mMessagesBox);
        mScrollPane.setFitToWidth(true);
        mScrollPane.setStyle("-fx-background: rgb(54,57,63);");

        this.setCenter(mScrollPane);

        /* ================= INPUT ================= */

        mInputField = new TextField();
        mInputField.setPromptText("Écrire un message...");
        mInputField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: rgb(220,221,222);
                -fx-prompt-text-fill: gray;
                """);

        mCharCounter = new Label("0/" + MAX_CHARS);
        mCharCounter.setTextFill(Color.GRAY);
        mCharCounter.setStyle("-fx-font-size: 10px;");

        mSendButton = new Button("Envoyer");
        mSendButton.setStyle("""
                -fx-background-color: rgb(88,101,242);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);
        mSendButton.setDisable(true); // Initialement désactivé car champ vide

        // Listener pour le compteur et la validation (MAX 200)
        mInputField.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            mCharCounter.setText(length + "/" + MAX_CHARS);

            boolean isTooLong = length > MAX_CHARS;
            boolean isEmpty = length == 0;

            if (isTooLong) {
                mCharCounter.setTextFill(Color.RED);
            } else {
                mCharCounter.setTextFill(Color.GRAY);
            }

            mSendButton.setDisable(isEmpty || isTooLong);
        });

        // Action d'envoi
        Runnable sendAction = () -> {
            String text = mInputField.getText();
            if (!text.isEmpty() && text.length() <= MAX_CHARS) {
                mChatController.sendMessage(text, mSessionController.getSelectedChannel());
                mInputField.clear();
            }
        };

        mInputField.setOnAction(e -> sendAction.run());
        mSendButton.setOnAction(e -> sendAction.run());

        HBox inputBox = new HBox(10, mInputField, mCharCounter, mSendButton);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.setPadding(new Insets(15));
        HBox.setHgrow(mInputField, Priority.ALWAYS);

        this.setBottom(inputBox);
    }

    @Override
    public void onChannelSelected(Channel channel) {

        Platform.runLater(() -> {
            mMessagesBox.getChildren().clear();

            // Reset buttons
            mInviteButton.setVisible(false);
            mInviteButton.setManaged(false);
            mLeaveButton.setVisible(false);
            mLeaveButton.setManaged(false);
            mDeleteButton.setVisible(false);
            mDeleteButton.setManaged(false);

            if (channel == null) {
                mChannelTitle.setText("Sélectionnez un canal");
                return;
            }

            mChannelTitle.setText("# " + channel.getName());

            // Gestion des boutons
            User currentUser = mSessionController.getCurrentUser();
            boolean isCreator = currentUser != null && 
                                channel.getCreator() != null && 
                                channel.getCreator().getUuid().equals(currentUser.getUuid());
            
            boolean isPrivate = channel.isPrivate();
            
            // Inviter : Créateur + Privé
            if (isCreator && isPrivate) {
                mInviteButton.setVisible(true);
                mInviteButton.setManaged(true);
            }

            // Supprimer : Créateur (Privé ou Public)
            if (isCreator) {
                mDeleteButton.setVisible(true);
                mDeleteButton.setManaged(true);
            }

            // Quitter : Membre (non créateur) + Privé
            if (!isCreator && isPrivate) {
                // Vérifier si membre (normalement oui si on voit le canal)
                mLeaveButton.setVisible(true);
                mLeaveButton.setManaged(true);
            }

            Set<Message> messagesSet = mChatController.getMessagesForChannel(channel);

            List<Message> sortedMessages = messagesSet.stream()
                    .sorted(Comparator.comparingLong(Message::getEmissionDate))
                    .collect(Collectors.toList());

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            mMessagesBox.getChildren().add(spacer);

            for (Message msg : sortedMessages) {

                boolean isCurrentUser = currentUser != null &&
                        msg.getSender().getUserTag().equals(currentUser.getUserTag());

                MessageView messageView = new MessageView(msg, isCurrentUser);
                mMessagesBox.getChildren().add(messageView);
            }

            scrollToBottom();
        });
    }

    private void openInviteDialog() {
        Channel currentChannel = mSessionController.getSelectedChannel();
        if (currentChannel != null) {
            Set<User> allUsers = mSessionController.getAllUsers();
            InviteUserDialog dialog = new InviteUserDialog(
                    (Stage) getScene().getWindow(),
                    mChannelController,
                    allUsers,
                    currentChannel
            );
            dialog.showAndWait();
        }
    }

    private void confirmLeaveChannel() {
        Channel currentChannel = mSessionController.getSelectedChannel();
        User currentUser = mSessionController.getCurrentUser();
        
        if (currentChannel != null && currentUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Quitter le canal");
            alert.setHeaderText("Voulez-vous vraiment quitter ce canal ?");
            alert.setContentText("Vous ne pourrez plus y accéder à moins d'être réinvité.");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    mChannelController.leaveChannel(currentChannel, currentUser);
                    // La sélection du canal sera mise à jour automatiquement via les observers ou on peut forcer un reset
                    mSessionController.selectChannel(null);
                }
            });
        }
    }

    private void confirmDeleteChannel() {
        Channel currentChannel = mSessionController.getSelectedChannel();
        
        if (currentChannel != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le canal");
            alert.setHeaderText("Voulez-vous vraiment supprimer ce canal ?");
            alert.setContentText("Cette action est irréversible et supprimera le canal pour tous les utilisateurs.");

            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    mChannelController.deleteChannel(currentChannel);
                    mSessionController.selectChannel(null);
                }
            });
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> mScrollPane.setVvalue(1.0));
    }

    @Override
    public void onUsersUpdated() {
        // Géré ailleurs
    }
}