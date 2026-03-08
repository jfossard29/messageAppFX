package com.message.datamodel;

import java.util.*;

/**
 * Classe du modèle représentant un canal.
 *
 * @author S.Lucas
 */
public class Channel extends AbstractMessageAppObject implements IMessageRecipient {

	/**
	 * Créateur du canal.
	 */
	protected final User mCreator;

	/**
	 * Nom du canal.
	 */
	protected final String mName;

	/**
	 * Statut privé ou public du canal.
	 */
	protected boolean mPrivate;

	/**
	 * Liste des Utilisateurs du canal.
	 */
	protected final Set<User> mUsers = new HashSet<User>();

	/**
	 * Constructeur.
	 *
	 * @param sender utilisateur à l'origine du canal.
	 * @param name   Nom du canal.
	 */
	public Channel(User creator, String name) {
		this(UUID.randomUUID(), creator, name);
	}

	/**
	 * Constructeur.
	 *
	 * @param channelUuid identifiant du canal.
	 * @param sender      utilisateur à l'origine du canal.
	 * @param name        Nom du canal.
	 */
	public Channel(UUID channelUuid, User creator, String name) {
		super(channelUuid);
		mCreator = creator;
		mName = name;
	}

	/**
	 * Constructeur pour un canal privé.
	 *
	 * @param sender utilisateur à l'origine du canal.
	 * @param name   Nom du canal.
	 */
	public Channel(User creator, String name, List<User> users) {
		this(UUID.randomUUID(), creator, name, users);
	}

	/**
	 * Constructeur pour un canal privé.
	 *
	 * @param channelUuid identifiant du canal.
	 * @param sender      utilisateur à l'origine du canal.
	 * @param name        Nom du canal.
	 * @param users       Liste des utilisateurs du canal privé.
	 * 
	 */
	public Channel(UUID messageUuid, User creator, String name, List<User> users) {
		this(messageUuid, creator, name);
		if (!users.isEmpty()) {
			mPrivate = true;
			mUsers.addAll(users);
		}
	}

	/**
	 * @return l'utilisateur source du canal.
	 */
	public User getCreator() {
		return mCreator;
	}

	/**
	 * @return le corps du message.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return la liste des utilisateurs de ce canal.
	 */
	public List<User> getUsers() {
		return new ArrayList<User>(mUsers);
	}

    /**
     * Ajoute des utilisateurs au canal.
     * @param users Liste des utilisateurs à ajouter.
     */
    public void addUsers(List<User> users) {
        if (users != null && !users.isEmpty()) {
            mUsers.addAll(users);
            mPrivate = true; // Devient privé (ou le reste) si on ajoute des utilisateurs spécifiques
        }
    }

    /**
     * Retire un utilisateur du canal.
     * @param user Utilisateur à retirer.
     */
    public void removeUser(User user) {
        if (user != null) {
            mUsers.removeIf(u -> u.getUuid().equals(user.getUuid()));
        }
    }

    /**
     * @return true si le canal est privé.
     */
    public boolean isPrivate() {
        return mPrivate;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[");
		sb.append(this.getClass().getName());
		sb.append("] : ");
		sb.append(this.getUuid());
		sb.append(" {");
		sb.append(this.getName());
		sb.append("}");

		return sb.toString();
	}

}
