package com.example.ymdbanking.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for Transaction
 * transaction can be a payment,transfer,deposit
 */
public class Transaction
{

	public enum TRANSACTION_TYPE
	{
		PAYMENT,
		TRANSFER,
		DEPOSIT,
		CASH_DEPOSIT,
		LOAN
	}

	public enum STATUS
	{
		APPROVED,
		PENDING,
		DENIED
	}

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd - hh:mm a");
	private String transactionID;
	private String timestamp;
	private String sendingAccount;
	private String destinationCustomerId;
	private String destinationAccount;
	private String payeeId;
	private String payeeName;
	private double amount;
	private TRANSACTION_TYPE transactionType;
	private STATUS status;

	public Transaction()
	{
		//Empty constructor
	}

	/**
	 * Transaction constructors for payment
	 *
	 * @param transactionID - transaction ID
	 * @param payeeId         - receiving side of payment
	 * @param amount        - amount to transfer to payee
	 */
	public Transaction(double amount,String transactionID,String payeeId,String payeeName)
	{
		this.transactionID = transactionID;
		timestamp = DATE_FORMAT.format(new Date());
		this.payeeId = payeeId;
		this.payeeName = payeeName;
		this.amount = amount;
		transactionType = TRANSACTION_TYPE.PAYMENT;
	}

	/**
	 * Transaction constructor for deposit
	 *
	 * @param transactionID - transaction ID
	 * @param amount        - amount to deposit to account
	 */
	public Transaction(String transactionID,double amount,Account destinationAccount,String destinationCustomerId)
	{
		this.transactionID = transactionID;
		timestamp = DATE_FORMAT.format(new Date());
		this.amount = amount;
		transactionType = TRANSACTION_TYPE.DEPOSIT;
		this.destinationAccount = destinationAccount.getAccountNo();
		this.destinationCustomerId = destinationCustomerId;
	}

	/**
	 * Transaction constructor for cash deposit
	 */
	public Transaction(double amount,Account destinationAccount,String destinationCustomerId,String transactionID)
	{
		this.transactionID = transactionID;
		timestamp = DATE_FORMAT.format(new Date());
		this.amount = amount;
		transactionType = TRANSACTION_TYPE.CASH_DEPOSIT;
		this.destinationAccount = destinationAccount.getAccountNo();
		this.destinationCustomerId = destinationCustomerId;
		status = STATUS.PENDING;
	}

	/**
	 * Transaction constructors for loan
	 *
	 * @param transactionID      - transaction ID
	 * @param amount             - amount of the loan
	 * @param DestinationAccount - receiving account for loan
	 */
	public Transaction(String transactionID,Account DestinationAccount,double amount,String destinationCustomerId)
	{
		this.transactionID = transactionID;
		timestamp = DATE_FORMAT.format(new Date());
		this.amount = amount;
		this.destinationAccount = DestinationAccount.getAccountNo();
		transactionType = TRANSACTION_TYPE.LOAN;
		status = STATUS.PENDING;
		this.destinationCustomerId = destinationCustomerId;
	}

	/**
	 * Transaction constructors for transfer
	 *
	 * @param transactionID      - transaction ID
	 * @param sendingAccount     - sending account for transfer
	 * @param destinationAccount - receiving account
	 * @param amount             - amount to transfer to destinationAccount
	 */
	public Transaction(String transactionID,String sendingAccount,String destinationAccount,double amount)
	{
		this.transactionID = transactionID;
		this.timestamp = DATE_FORMAT.format(new Date());
		this.sendingAccount = sendingAccount;
		this.destinationAccount = destinationAccount;
		this.amount = amount;
		transactionType = TRANSACTION_TYPE.TRANSFER;
		this.destinationCustomerId = destinationCustomerId;
	}

	/**
	 * getters used to access the private fields of the transaction
	 */
	public String getTransactionID() { return transactionID; }

	public String getTimestamp() { return timestamp; }

	public String getSendingAccount()
	{
		return sendingAccount;
	}

	public String getDestinationAccount()
	{
		return destinationAccount;
	}

	public String getPayeeId() { return payeeId; }

	public double getAmount()
	{
		return amount;
	}

	public TRANSACTION_TYPE getTransactionType()
	{
		return transactionType;
	}

	public STATUS getStatus() {return status;}

	public String getDestinationCustomerId() {return destinationCustomerId;}

	public String getPayeeName() {return payeeName;}

	public void setPayeeName(String payeeName) {this.payeeName = payeeName;}

	public void setStatus(STATUS status) {this.status = status;}
}
