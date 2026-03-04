package com.message.core;

import com.message.common.Constants;
import com.message.core.database.EntityManager;
import com.message.core.database.IDatabase;
import com.message.core.database.IDatabaseObserver;
import com.message.core.directory.IWatchableDirectory;
import com.message.core.directory.WatchableDirectory;
import com.message.datamodel.Channel;
import com.message.datamodel.IMessageRecipient;
import com.message.datamodel.Message;
import com.message.datamodel.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe permettant de manipuler les données de l'application.
 *
 * @author S.Lucas
 */
public class DataManager implements IDatabaseObserver {

	/**
	 * Base de donnée de l'application.
	 */
	protected final IDatabase mDatabase;

	/**
	 * Gestionnaire des entités contenu de la base de données.
	 */
	protected final EntityManager mEntityManager;

	/**
	 * Classe de surveillance de répertoire
	 */
	protected IWatchableDirectory mWatchableDirectory;

	/**
	 * Liste des observateurs.
	 */
	protected final Set<IDatabaseObserver> mObservers = new HashSet<>();

	/**
	 * Constructeur.
	 */
	public DataManager(IDatabase database, EntityManager entityManager) {
		mDatabase = database;
		mEntityManager = entityManager;
		mDatabase.addObserver(this);
	}

	/**
	 * Ajoute un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
	public void addObserver(IDatabaseObserver observer) {
		mObservers.add(observer);

		// Notification pour le nouvel observateur
		for (Message message : this.getMessages()) {
			observer.notifyMessageAdded(message);
		}

		for (User user : this.getUsers()) {
			// Pas de notification pour l'utilisateur inconnu
			if (!user.getUuid().equals(Constants.UNKNONWN_USER_UUID)) {
				observer.notifyUserAdded(user);
			}
		}

		for (Channel channel : this.getChannels()) {
			observer.notifyChannelAdded(channel);
		}
	}

	/**
	 * Supprime un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
    public void removeObserver(IDatabaseObserver observer) {
		mObservers.remove(observer);
	}

	/**
	 * Retourne la liste des Utilisateurs.
	 */
	public Set<User> getUsers() {
		return this.mDatabase.getUsers();
	}

	/**
	 * Retourne la liste des Messages.
	 */
	public Set<Message> getMessages() {
		return this.mDatabase.getMessages();
	}

	/**
	 * Retourne la liste des Canaux.
	 */
	public Set<Channel> getChannels() {
		return this.mDatabase.getChannels();
	}

	/**
	 * Ecrit un message.
	 *
	 * @param message
	 */
	public void sendMessage(Message message) {
		// Ecrit un message
		this.mEntityManager.writeMessageFile(message);
	}

	/**
	 * Ecrit un Utilisateur.
	 *
	 * @param user
	 */
	public void sendUser(User user) {
		// Ecrit un utilisateur
		this.mEntityManager.writeUserFile(user);
	}

    /**
     * Met à jour un utilisateur.
     * @param user
     */
    public void updateUser(User user) {
        this.sendUser(user);
    }

    /**
     * Supprime le compte d'un utilisateur.
     * @param user
     */
    public void deleteAccount(User user) {
        this.mEntityManager.deleteUserAccount(user);
    }

	/**
	 * Ecrit un Canal.
	 *
	 * @param channel
	 */
	public void sendChannel(Channel channel) {
		// Ecrit un canal
		this.mEntityManager.writeChannelFile(channel);
	}

    public void updateUserOnlineStatus(User user) {
        this.mEntityManager.updateUserOnlineStatus(user);
    }

	/**
	 * Retourne tous les Messages d'un utilisateur.
	 *
	 * @param user utilisateur dont les messages sont à rechercher.
	 */
	public Set<Message> getMessagesFrom(User user) {
		Set<Message> userMessages = new HashSet<>();

		// Parcours de tous les messages de la base
		for (Message message : this.getMessages()) {
			// Si le message est celui recherché
			if (message.getSender().equals(user)) {
				userMessages.add(message);
			}
		}

		return userMessages;
	}

	/**
	 * Retourne tous les Messages d'un utilisateur addressé à un autre.
	 *
	 * @param sender utilisateur dont les messages sont à rechercher.
	 * @param recipient destinataire des messages recherchés.
	 */
	public Set<Message> getMessagesFrom(User sender, IMessageRecipient recipient) {
		Set<Message> userMessages = new HashSet<>();

		// Parcours de tous les messages de l'utilisateur
		for (Message message : this.getMessagesFrom(sender)) {
			// Si le message est celui recherché
			if (message.getRecipient().equals(recipient.getUuid())) {
				userMessages.add(message);
			}
		}

		return userMessages;
	}

	/**
	 * Retourne tous les Messages adressés à un utilisateur.
	 *
	 * @param user utilisateur dont les messages sont à rechercher.
	 */
	public Set<Message> getMessagesTo(User user) {
		Set<Message> userMessages = new HashSet<>();

		// Parcours de tous les messages de la base
		for (Message message : this.getMessages()) {
			// Si le message est celui recherché
			if (message.getSender().equals(user)) {
				userMessages.add(message);
			}
		}

		return userMessages;
	}

	/**
	 * Assignation du répertoire d'échange.
	 * 
	 * @param directoryPath
	 */
	public void setExchangeDirectory(String directoryPath) {
		mEntityManager.setExchangeDirectory(directoryPath);

		mWatchableDirectory = new WatchableDirectory(directoryPath);
		mWatchableDirectory.initWatching();
		mWatchableDirectory.addObserver(mEntityManager);
	}

	@Override
	public void notifyMessageAdded(Message addedMessage) {
		System.out.println("Message ajouté : " + addedMessage.getUuid());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageAdded(addedMessage);
		}
	}

	@Override
	public void notifyMessageDeleted(Message deletedMessage) {
		System.out.println("Message supprimé : " + deletedMessage.getUuid());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageDeleted(deletedMessage);
		}
	}

	@Override
	public void notifyMessageModified(Message modifiedMessage) {
		System.out.println("Message modifié : " + modifiedMessage.getUuid());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyMessageModified(modifiedMessage);
		}
	}

	@Override
	public void notifyUserAdded(User addedUser) {
		System.out.println("User ajouté : " + addedUser.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserAdded(addedUser);
		}
	}

	@Override
	public void notifyUserDeleted(User deletedUser) {
		System.out.println("User supprimé : " + deletedUser.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserDeleted(deletedUser);
		}
	}

	@Override
	public void notifyUserModified(User modifiedUser) {
		System.out.println("User modifié : " + modifiedUser.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyUserModified(modifiedUser);
		}
	}

	@Override
	public void notifyChannelAdded(Channel addedChannel) {
		System.out.println("Channel ajouté : " + addedChannel.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyChannelAdded(addedChannel);
		}
	}

	@Override
	public void notifyChannelDeleted(Channel deletedChannel) {
		System.out.println("Channel supprimé : " + deletedChannel.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyChannelDeleted(deletedChannel);
		}
	}

	@Override
	public void notifyChannelModified(Channel modifiedChannel) {
		System.out.println("Channel modifié : " + modifiedChannel.getName());
		for (IDatabaseObserver observer : mObservers) {
			observer.notifyChannelModified(modifiedChannel);
		}
	}
}
