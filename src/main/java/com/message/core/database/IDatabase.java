package com.message.core.database;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;

import java.util.Set;

/**
 * Interface de la base de données de l'application.
 *
 * @author S.Lucas
 */
public interface IDatabase {

	/**
	 * Ajoute un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
	void addObserver(IDatabaseObserver observer);

	/**
	 * Supprime un observateur sur les modifications de la base de données.
	 *
	 * @param observer
	 */
	void removeObserver(IDatabaseObserver observer);

	/**
	 * Retourne la liste des utilisateurs.
	 */
	Set<User> getUsers();

	/**
	 * Retourne la liste des messages.
	 */
	Set<Message> getMessages();

	/**
	 * Retourne la liste des cannaux.
	 */
	Set<Channel> getChannels();
}
