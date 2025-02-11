/*
 *  Copyright (c) 2020 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 */
package com.telus.cis.common.amdocs;

public interface AmdocsInvocationCallback  {

	/**
	 * Gets called by <code>AmdocsTemplate.execute</code> with an active AmdocsTransactionContext
	 * Does not need to care about removing the Amdocs statefull beans allocated with the 
	 * <code>AmdocsTransactionContext.createBean()</code> method: this will all be
	 * handled by AmdocsTemplate.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. 
	 *
	 * @param transactionContext - active AmdocsTransactionContext
	 * @return a result object, or <code>null</code> if none
	 * @throws Exception 
	 */	
	void doInTransaction(AmdocsTransaction transaction) throws Exception;
	
}