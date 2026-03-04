package com.message.core.session;

import com.message.core.DataManager;
import com.message.datamodel.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Session de l'application.
 *
 * @author S.Lucas
 */
public class Session implements ISession {

	/**
	 * Utilisateur connecté
	 */
	protected User mConnectedUser;

	/**
	 * Liste des observateurs de la session.
	 */
	protected List<ISessionObserver> mObservers = new ArrayList<>();

	/**
	 * Gestionnaire de données.
	 */
	protected DataManager mDataManager;

	/**
	 * Constructeur.
	 * @param dataManager
	 */
	public Session(DataManager dataManager) {
		this.mDataManager = dataManager;
	}

	@Override
	public void addObserver(ISessionObserver observer) {
		this.mObservers.add(observer);
	}

	@Override
	public void removeObserver(ISessionObserver observer) {
		this.mObservers.remove(observer);
	}

	@Override
	public User getConnectedUser() {
		return mConnectedUser;
	}

	@Override
	public void connect(User connectedUser) {
		this.mConnectedUser = connectedUser;
        this.mConnectedUser.setOnline(true);
		this.mDataManager.updateUserOnlineStatus(this.mConnectedUser);

		for (ISessionObserver observer : mObservers) {
			observer.notifyLogin(connectedUser);
		}
	}

	@Override
	public void disconnect() {
        this.mConnectedUser.setOnline(false);
		this.mDataManager.updateUserOnlineStatus(this.mConnectedUser);
		this.mConnectedUser = null;

		for (ISessionObserver observer : mObservers) {
			observer.notifyLogout();
		}
	}
}
