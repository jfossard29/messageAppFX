package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IChannelController;
import com.message.ihm.controllers.interfaces.IChatController;
import com.message.ihm.controllers.interfaces.IEasterEggObservable;
import com.message.ihm.controllers.interfaces.IEasterEggObserver;
import com.message.ihm.controllers.interfaces.ISessionController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatView extends BorderPane implements ISessionController.ISessionControllerObserver, IEasterEggObservable {

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
    private final List<IEasterEggObserver> easterEggObservers = new ArrayList<>();

    private static final int MAX_CHARS = 200;

    public ChatView(ISessionController sessionController, IChatController chatController, IChannelController channelController) {
        this.mSessionController = sessionController;
        this.mChatController = chatController;
        this.mChannelController = channelController;
        this.mSessionController.addObserver(this);
        initGui();
    }

    /**
     * Initialise la vue de discussion et configure ses composants graphiques.
     * Cette méthode construit l'interface utilisateur complète (en-tête, zone de messages, zone de saisie).
     */
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
            // SRS-MAP-MSG-008 : Le texte d'un message ne dépasse pas 200 caractères.
            mSendButton.setDisable(isEmpty || isTooLong);
        });

        Runnable sendAction = () -> {
            String text = mInputField.getText();
            // SRS-MAP-MSG-008 : Le texte d'un message ne dépasse pas 200 caractères.
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
    /**
     * Méthode appelée lorsqu'un canal est sélectionné via le contrôleur de session.
     * Met à jour l'interface pour afficher les messages du canal sélectionné.
     */
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

    /**
     * Réinitialise l'interface utilisateur en masquant les éléments spécifiques au canal précédent.
     * Vide la liste des messages affichés.
     */
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

    /**
     * Met à jour le titre de l'en-tête en fonction du type de canal (public ou message direct).
     */
    private void updateHeader(Channel channel) {
        if (channel.isDirectMessage()) {
            mChannelTitle.setText("@ " + channel.getName());
        } else {
            mChannelTitle.setText("# " + channel.getName());
        }
    }

    /**
     * Met à jour la visibilité des boutons d'action (inviter, gérer, quitter, supprimer) 
     * selon les droits de l'utilisateur sur le canal.
     */
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

    /**
     * Charge les messages du canal depuis le contrôleur et les affiche dans la zone de discussion.
     * Trie les messages par date d'émission.
     */
    private void loadAndDisplayMessages(Channel channel) {
        Set<Message> messagesSet = mChatController.getMessagesForChannel(channel);
        this.mCurrentChannelMessages = messagesSet.stream()
                .sorted(Comparator.comparingLong(Message::getEmissionDate))
                .collect(Collectors.toList());
        
        applyMessageFilter();
    }

    /**
     * Filtre les messages affichés en fonction du texte saisi dans la barre de recherche.
     * Si la recherche est vide, tous les messages du canal sont affichés.
     * SRS-MAP-MSG-005 : L'utilisateur connecté peut rechercher un message dans un canal.
     */
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

    /**
     * Ouvre une boîte de dialogue pour inviter des utilisateurs au canal actuel.
     */
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

    /**
     * Ouvre une boîte de dialogue pour gérer les paramètres du canal (ex: renommer).
     */
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

    /**
     * Demande confirmation à l'utilisateur avant de quitter le canal.
     * Si confirmé, l'utilisateur est retiré du canal.
     */
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

    /**
     * Demande confirmation à l'utilisateur avant de supprimer le canal.
     * Si confirmé, le canal est définitivement supprimé pour tous.
     */
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

    /**
     * Fait défiler la zone de messages vers le bas pour afficher le dernier message reçu.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> mScrollPane.setVvalue(1.0));
    }

    @Override
    public void onUsersUpdated() {
        // Géré ailleurs
    }

    @Override
    /**
     * Méthode appelée lorsqu'un nouveau message est reçu.
     * Si le message concerne le canal actif, l'affichage est mis à jour.
     * SRS-MAP-MSG-010 : Une notification avertit l'utilisateur connecté lorsqu'un utilisateur lui envoie un message direct ou lorsqu’il est mentionné dans un canal.
     */
    public void onMessageReceived(Message message) {
        Channel selectedChannel = mSessionController.getSelectedChannel();
        if (selectedChannel != null) {
            boolean isDM = selectedChannel.isDirectMessage();
            User currentUser = mSessionController.getCurrentUser();

            if (isDM && currentUser != null) {
                boolean messageIsForMe = message.getRecipient().equals(currentUser.getUuid());
                boolean selectedUserIsSender = selectedChannel.getUsers().stream()
                        .anyMatch(u -> u.getUuid().equals(message.getSender().getUuid()));
                if (messageIsForMe && selectedUserIsSender) {
                    loadAndDisplayMessages(selectedChannel);
                    if (message.getText().startsWith("/")) {
                        notifyEasterEgg(message.getText());
                    }
                }
            } else if (!isDM) {
                if (message.getRecipient().equals(selectedChannel.getUuid())) {
                    loadAndDisplayMessages(selectedChannel);
                    if (message.getText().startsWith("/")) {
                        notifyEasterEgg(message.getText());
                    }
                }
            }
        }
    }

    public List<Node> getActiveCells() {
        if (mMessagesBox.getChildren().size() > 1) {
            return new ArrayList<>(mMessagesBox.getChildren().subList(1, mMessagesBox.getChildren().size()));
        }
        return new ArrayList<>();
    }

    @Override
    public void addEasterEggObserver(IEasterEggObserver observer) {
        easterEggObservers.add(observer);
    }

    @Override
    public void removeEasterEggObserver(IEasterEggObserver observer) {
        easterEggObservers.remove(observer);
    }

    @Override
    public void notifyEasterEgg(String command) {
        for (IEasterEggObserver observer : easterEggObservers) {
            observer.onEasterEggTriggered(command);
        }
    }
}
