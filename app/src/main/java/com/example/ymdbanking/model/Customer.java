package com.example.ymdbanking.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class Profile for profile users
 * extended by User Class
 */
public class Customer extends User
{
	private final static int typeID = 3;
	private ArrayList<Account> accounts;
	private ArrayList<Payee> payees;
	private ArrayList<Message> messages;

	public Customer()
	{
		//Empty constructor
	}

	public Customer(String email,String fullName,String id,String password,String phone,String username,String country)
	{
		super(email,fullName,id,password,phone,username,country,typeID);

		accounts = new ArrayList<>(0);
		payees = new ArrayList<>(0);
		messages = new ArrayList<>(0);
	}

	public Customer(String email,String fullName,String id,String password,String phone,String username,
	                String country,int typeID,ArrayList<Account> accounts,ArrayList<Payee> payees,ArrayList<Message> messages)
	{
		super(email,fullName,id,password,phone,username,country,typeID);
		this.accounts = accounts;
		this.payees = payees;
		this.messages = messages;
	}

	/**
	 * Method to add account to this profile user
	 *
	 * @param accountName    - account of the profile
	 * @param accountBalance - account balance (current amount in account)
	 */
	public void addAccount(String accountName,double accountBalance)
	{
		String accNo = "A" + (accounts.size() + 1);
		Account account = new Account(accountName,accNo,accountBalance);
		account.setTransactions(new ArrayList<Transaction>(0));
		accounts.add(account);
	}

	/**
	 * Method to implement the transfer logic
	 * and adds the transfer to profile's transactions
	 *
	 * @param sendingAcc     - account of sending profile
	 * @param receivingAcc   - account of receiving profile
	 * @param transferAmount - amount to transfer to receiving account
	 */
	public void addTransferTransaction(Account sendingAcc,Account receivingAcc,double transferAmount)
	{
		if(transferAmount <= sendingAcc.getAccountBalance())
		{
			sendingAcc.setAccountBalance(sendingAcc.getAccountBalance() - transferAmount);
			receivingAcc.setAccountBalance(receivingAcc.getAccountBalance() + transferAmount);

			int sendingAccTransferCount = 0;
			int receivingAccTransferCount = 0;
			for(int i = 0; i < sendingAcc.getTransactions().size(); i++)
			{
				if(sendingAcc.getTransactions().get(i).getTransactionType() ==
				   Transaction.TRANSACTION_TYPE.TRANSFER)
				{
					sendingAccTransferCount++;
				}
			}
			for(int i = 0; i < receivingAcc.getTransactions().size(); i++)
			{
				if(receivingAcc.getTransactions().get(i).getTransactionType() ==
				   Transaction.TRANSACTION_TYPE.TRANSFER)
				{
					receivingAccTransferCount++;
				}
			}

			sendingAcc.getTransactions().add(new Transaction(
					"T" + (sendingAcc.getTransactions().size() + 1) + "-T" +
					(sendingAccTransferCount +
					 1),sendingAcc.toTransactionString(),receivingAcc.toTransactionString(),transferAmount));
			receivingAcc.getTransactions().add(new Transaction(
					"T" + (receivingAcc.getTransactions().size() + 1) + "-T" +
					(receivingAccTransferCount +
					 1),sendingAcc.toTransactionString(),receivingAcc.toTransactionString(),transferAmount));

		}
	}

	/**
	 * Add payee to profile's list of payees
	 *
	 * @param payeeName
	 */
	public void addPayee(String payeeName)
	{
		String payeeID = "P" + (payees.size() + 1);
		Payee payee = new Payee(payeeID,payeeName);
		payees.add(payee);
	}

	public void addMessage(String receivingUser,String msg)
	{
		String messageID = "M" + (messages.size() + 1);
		Message message = new Message(getUsername(),receivingUser,msg,messageID);
		messages.add(message);
	}

	/**
	 * getters used to access the private fields of the customer
	 */
	public ArrayList<Account> getAccounts() { return accounts; }

	public void setAccounts(ArrayList<Account> accounts) {this.accounts = accounts;}

	public ArrayList<Payee> getPayees() { return payees; }

	public void setPayees(ArrayList<Payee> payees)
	{
		this.payees = payees;
	}

	public ArrayList<Message> getMessages() {return messages;}

	public void setMessages(ArrayList<Message> messages) {this.messages = messages;}

	@Override
	public String toString()
	{
		return getUsername();
	}
}
