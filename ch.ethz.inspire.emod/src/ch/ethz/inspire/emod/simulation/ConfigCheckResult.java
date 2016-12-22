/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.simulation;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Implements the bundle of result state and result message of a config test
 * 
 * @author simon
 *
 */
public class ConfigCheckResult {
	private static Logger logger = Logger.getLogger(ConfigCheckResult.class.getName());
	
	private ArrayList<MessageBundle> messages;
	private ConfigState status;
	
	/**
	 * ConfigCheckResult
	 */
	public ConfigCheckResult(){
		messages = new ArrayList<ConfigCheckResult.MessageBundle>();
		status = ConfigState.OK;
	}
	
	/**
	 * Write the results to the Logger
	 */
	public void loggger(){
		for(MessageBundle mb: messages)
			switch(mb.getState()){
			case OK:
				logger.info(mb.getOrigin()+": "+mb.getMessage());
				break;
			case WARNING:
				logger.warning(mb.getOrigin()+": "+mb.getMessage());
				break;
			case ERROR:
				logger.severe(mb.getOrigin()+": "+mb.getMessage());
				break;
			}
	}
	
	/**
	 * Add a new message
	 * @param mb
	 */
	public void add(MessageBundle mb){
		messages.add(mb);
		// New status = max status
		status = ConfigState.values()[Math.max(mb.getState().ordinal(), status.ordinal())];
	}
	
	/**
	 * Adds a new message and updates the global status
	 * @param state
	 * @param origin
	 * @param message
	 */
	public void add(ConfigState state, String origin, String message){
		add(new MessageBundle(state, origin, message));
	}
	
	/**
	 * Add all messages of an other ConfigCheckResult
	 * @param ccr
	 */
	public void addAll(ConfigCheckResult ccr){
		for(MessageBundle mb: ccr.getMessages())
			add(mb);
	}
	
	/**
	 * @return the messages
	 */
	public ArrayList<MessageBundle> getMessages() {
		return messages;
	}

	/**
	 * @return the status
	 */
	public ConfigState getStatus() {
		return status;
	}

	/**
	 * Implementation of a single configuraiton message
	 * @author simon
	 *
	 */
	public class MessageBundle {
		private String origin;
		private String message;
		private ConfigState state;
		
		/**
		 * MessageBundle
		 * @param state 
		 * @param origin 
		 * @param message 
		 */
		public MessageBundle(ConfigState state, String origin, String message){
			this.state = state;
			this.origin = origin;
			this.message = message ;
		}

		/**
		 * @return the origin
		 */
		public String getOrigin() {
			return origin;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return the state
		 */
		public ConfigState getState() {
			return state;
		}
		
		
		
		
	}
}
