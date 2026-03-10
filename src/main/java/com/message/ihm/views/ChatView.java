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

import java.util.ArrayList;
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
    private Button mManageButton;
    private VBox mMessagesBox;
    private ScrollPane mScrollPane;
    private TextField mSearchMessagesField;

    private List<Message> mCurrentChannelMessages = new ArrayList<>();

    private static final int MAX_CHARS = 200;

    public ChatView(ISessionController sessionController, IChatController chatController, IChannelController channelController) {
        this.mSessionController = sessionController;
        this.mChatController = chatController;
        this.mChannelController = channelController;
        this.mSessionController.addObserver(this);
        initGui();
    }

    private void initGui() {

        this.setStyle("-fx-background-color: rgb(54,57,63);");

        /* ================= HEADER ================= */

        mChannelTitle = new Label("Sélectionnez un canal");
        mChannelTitle.setTextFill(Color.rgb(220, 221, 222));
        mChannelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        mChannelTitle.setPadding(new Insets(15));

        mSearchMessagesField = new TextField();
        mSearchMessagesField.setPromptText("Rechercher message (3 min)...");
        mSearchMessagesField.setStyle("-fx-background-color: rgb(32,34,37); -fx-text-fill: white; -fx-prompt-text-fill: gray;");
        mSearchMessagesField.setVisible(false);
        mSearchMessagesField.setManaged(false);
        mSearchMessagesField.textProperty().addListener((obs, oldVal, newVal) -> applyMessageFilter());

        // Bouton Inviter
        mInviteButton = new Button("Inviter...");
        mInviteButton.setStyle("-fx-background-color: rgb(88,101,242); -fx-text-fill: white; -fx-font-weight: bold;");
        mInviteButton.setVisible(false);
        mInviteButton.setManaged(false);
        mInviteButton.setOnAction(e -> openInviteDialog());

        // Bouton Gérer
        mManageButton = new Button("Gérer");
        mManageButton.setStyle("-fx-background-color: rgb(79, 84, 92); -fx-text-fill: white; -fx-font-weight: bold;");
        mManageButton.setVisible(false);
        mManageButton.setManaged(false);
        mManageButton.setOnAction(e -> openManageDialog());

        // Bouton Quitter
        mLeaveButton = new Button("Quitter");
        mLeaveButton.setStyle("-fx-background-color: rgb(237,66,69); -fx-text-fill: white; -fx-font-weight: bold;");
        mLeaveButton.setVisible(false);
        mLeaveButton.setManaged(false);
        mLeaveButton.setOnAction(e -> confirmLeaveChannel());

        // Bouton Supprimer
        mDeleteButton = new Button("Supprimer");
        mDeleteButton.setStyle("-fx-background-color: rgb(237,66,69); -fx-text-fill: white; -fx-font-weight: bold;");
        mDeleteButton.setVisible(false);
        mDeleteButton.setManaged(false);
        mDeleteButton.setOnAction(e -> confirmDeleteChannel());

        HBox header = new HBox(10, mChannelTitle, mSearchMessagesField, new Region(), mInviteButton, mManageButton, mLeaveButton, mDeleteButton);
        HBox.setHgrow(header.getChildren().get(2), Priority.ALWAYS);
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

        TextField mInputField = new TextField();
        mInputField.setPromptText("Écrire un message...");
        mInputField.setStyle("-fx-background-color: rgb(64,68,75); -fx-text-fill: rgb(220,221,222); -fx-prompt-text-fill: gray;");

        Label mCharCounter = new Label("0/" + MAX_CHARS);
        mCharCounter.setTextFill(Color.GRAY);
        mCharCounter.setStyle("-fx-font-size: 10px;");

        Button mSendButton = new Button("Envoyer");
        mSendButton.setStyle("-fx-background-color: rgb(88,101,242); -fx-text-fill: white; -fx-font-weight: bold;");
        mSendButton.setDisable(true);

        mInputField.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            mCharCounter.setText(length + "/" + MAX_CHARS);
            boolean isTooLong = length > MAX_CHARS;
            boolean isEmpty = length == 0;
            mCharCounter.setTextFill(isTooLong ? Color.RED : Color.GRAY);
            mSendButton.setDisable(isEmpty || isTooLong);
        });

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
            resetUI();

            if (channel == null) {
                mChannelTitle.setText("Sélectionnez un canal");
                mSearchMessagesField.setVisible(false);
                mSearchMessagesField.setManaged(false);
                mSearchMessagesField.clear();
                return;
            }

            mSearchMessagesField.setVisible(true);
            mSearchMessagesField.setManaged(true);
            User currentUser = mSessionController.getCurrentUser();
            
            updateHeader(channel);
            updateButtons(channel, currentUser);
            loadAndDisplayMessages(channel);
        });
    }

    private void resetUI() {
        mMessagesBox.getChildren().clear();
        mInviteButton.setVisible(false);
        mInviteButton.setManaged(false);
        mManageButton.setVisible(false);
        mManageButton.setManaged(false);
        mLeaveButton.setVisible(false);
        mLeaveButton.setManaged(false);
        mDeleteButton.setVisible(false);
        mDeleteButton.setManaged(false);
    }

    private void updateHeader(Channel channel) {
        if (channel.isDirectMessage()) {
            mChannelTitle.setText("@ " + channel.getName());
        } else {
            mChannelTitle.setText("# " + channel.getName());
        }
    }

    private void updateButtons(Channel channel, User currentUser) {
        if (currentUser == null || channel.isDirectMessage()) return;

        boolean isCreator = channel.getCreator() != null && channel.getCreator().getUuid().equals(currentUser.getUuid());
        boolean isPrivate = channel.isPrivate();

        if (isCreator) {
            mDeleteButton.setVisible(true);
            mDeleteButton.setManaged(true);
            
            if (isPrivate) {
                mInviteButton.setVisible(true);
                mInviteButton.setManaged(true);
                mManageButton.setVisible(true);
                mManageButton.setManaged(true);
            }
        } else if (isPrivate) {
            mLeaveButton.setVisible(true);
            mLeaveButton.setManaged(true);
        }
    }

    private void loadAndDisplayMessages(Channel channel) {
        Set<Message> messagesSet = mChatController.getMessagesForChannel(channel);
        this.mCurrentChannelMessages = messagesSet.stream()
                .sorted(Comparator.comparingLong(Message::getEmissionDate))
                .collect(Collectors.toList());
        
        applyMessageFilter();
    }

    private void applyMessageFilter() {
        User currentUser = mSessionController.getCurrentUser();
        String query = mSearchMessagesField.getText();
        List<Message> messagesToDisplay;

        if (query == null || query.length() < 3) {
            messagesToDisplay = mCurrentChannelMessages;
        } else {
            String lowerCaseQuery = query.toLowerCase();
            messagesToDisplay = mCurrentChannelMessages.stream()
                    .filter(msg -> msg.getText().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        mMessagesBox.getChildren().clear();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        mMessagesBox.getChildren().add(spacer);

        for (Message msg : messagesToDisplay) {
            boolean isCurrentUser = currentUser != null &&
                    msg.getSender().getUserTag().equals(currentUser.getUserTag());

            MessageView messageView = new MessageView(msg, isCurrentUser, mChatController);
            mMessagesBox.getChildren().add(messageView);
        }

        scrollToBottom();
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

    private void openManageDialog() {
        Channel currentChannel = mSessionController.getSelectedChannel();
        User currentUser = mSessionController.getCurrentUser();
        if (currentChannel != null && currentUser != null) {
            ManageChannelDialog dialog = new ManageChannelDialog(
                    (Stage) getScene().getWindow(),
                    mChannelController,
                    currentChannel,
                    currentUser
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

    @Override
    public void onMessageReceived(Message message) {
        // Pour une mise à jour en temps réel, on pourrait recharger les messages
        // et ré-appliquer le filtre si un nouveau message arrive.
        Channel selectedChannel = mSessionController.getSelectedChannel();
        if (selectedChannel != null) {
            // On vérifie si le message appartient au canal actuel
            boolean isDM = selectedChannel.isDirectMessage();
            User currentUser = mSessionController.getCurrentUser();

            if (isDM && currentUser != null) {
                boolean messageIsForMe = message.getRecipient().equals(currentUser.getUuid());
                boolean selectedUserIsSender = selectedChannel.getUsers().stream()
                        .anyMatch(u -> u.getUuid().equals(message.getSender().getUuid()));
                if (messageIsForMe && selectedUserIsSender) {
                    loadAndDisplayMessages(selectedChannel);
                }
            } else if (!isDM) {
                if (message.getRecipient().equals(selectedChannel.getUuid())) {
                    loadAndDisplayMessages(selectedChannel);
                }
            }
        }
    }
}
