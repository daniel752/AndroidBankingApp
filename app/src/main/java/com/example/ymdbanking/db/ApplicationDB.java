package com.example.ymdbanking.db;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.ymdbanking.model.Account;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Payee;
import com.example.ymdbanking.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationDB
{
	//Fields
//	private DatabaseReference dbRef;
	private FirebaseDatabase database;
	//	private FirebaseFirestore firestore;
	private Context context;

	//Users Collection
	private final String USERS = "Users";

	//Users Collection Fields
	private static final String EMAIL = "email";
	private static final String FULL_NAME = "fullName";
	private static final String ID = "id";
	private static final String PASSWORD = "password";
	private static final String PHONE = "phone";
	private static final String USERNAME = "username";
	private static final String KEY_TRANSACTIONS = "transactions";
	private static final String ACCOUNTS = "accounts";
	private static final String KEY_PAYEES = "payees";
	private static final String KEY_PAYEE_ID = "payee_id";
	private static final String KEY_PAYEE_NAME = "payee_name";

	//Account Collection Fields
	private final String KEY_ACCOUNT_NAME = "account_name";
	private static final String KEY_ACCOUNT_NUM = "account_number";
	private final String KEY_ACCOUNT_BALANCE = "balance";

	//Clerks Collection
	private final String CLERKS = "Clerks";


	/**
	 * Constructor
	 */
	public ApplicationDB(Context context)
	{
		this.database = FirebaseDatabase.getInstance();
//		firestore = FirebaseFirestore.getInstance();
		this.context = context;
	}

	//Methods

	/**
	 * Method gets customer and assigns him to a clerk
	 *
	 * @param customer - customer object
	 * @param clerk    - clerk object
	 */
	public void saveCustomerToClerkList(Customer customer,Clerk clerk)
	{
		database.getReference("ClerkCustomers").child(clerk.getId())
				.child(customer.getId()).setValue(customer);
	}

	/**
	 * Method to save transaction to database
	 *
	 * @param sendingCustomer - sending customer
	 * @param transaction     - transaction object to hold all info about the transaction
	 */
	public void saveNewTransaction(Customer sendingCustomer,Transaction transaction)
	{
		if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.TRANSFER)
		{
			HashMap<String, Transaction> tran = new HashMap<>();
			tran.put(transaction.getTransactionID(),transaction);
			database.getReference("Accounts").child(sendingCustomer.getId())
					.child(transaction.getSendingAccount()).setValue(tran);

		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.PAYMENT)
		{
			database.getReference("Payees").child(sendingCustomer.getId()).child(transaction.getPayeeId())
					.child(transaction.getTransactionID()).setValue(transaction);
		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.DEPOSIT ||
		        transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.LOAN ||
		        transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.CASH_DEPOSIT)
		{
			HashMap<String, Transaction> tran = new HashMap<>();
			tran.put(transaction.getTransactionID(),transaction);
			database.getReference("Accounts").child(sendingCustomer.getId())
					.child(transaction.getDestinationAccount()).child("transactions").setValue(tran);
		}
	}

	/**
	 * Method to save new account to user's account collection in database
	 *
	 * @param customer - customer object to add the account
	 * @param account  - account object to be added to customer
	 */
	public void saveNewAccount(Customer customer,Account account)
	{
		database.getReference("Accounts").child(customer.getId()).child(account.getAccountNo())
				.setValue(account);
	}

	/**
	 * Method to overwrite existing account from customer accounts
	 *
	 * @param customer - customer object
	 * @param account  - account object
	 */
	public void overwriteAccount(Customer customer,Account account)
	{
		HashMap<String, Object> newAccount = new HashMap<>();
		newAccount.put("accountBalance",account.getAccountBalance());
		newAccount.put("accountName",account.getAccountName());
		newAccount.put("accountNo",account.getAccountNo());
		newAccount.put("transactions",account.getTransactions());

		database.getReference("Accounts").child(customer.getId()).child(account.getAccountNo())
				.updateChildren(newAccount);
	}

	public void saveNewLoan(Clerk clerk,Customer customer,Transaction loan)
	{
		database.getReference("PendingTransactions").child("Loans").child(clerk.getId()).child(customer.getId())
				.child(loan.getTransactionID()).setValue(loan);
	}

	public void saveNewPayee(Customer customer,Payee payee)
	{
		database.getReference("Payees").child(customer.getId()).child(payee.getPayeeID()).setValue(payee);
	}

	/**
	 * Method to get all admins from database
	 *
	 * @return - ArrayList of admins
	 */
	public ArrayList<Admin> getAllAdmins()
	{
		final ArrayList<Admin> admins = new ArrayList<>();
		database.getReference("Admins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					admins.add(ds.getValue(Admin.class));
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get all admins from DB: " +
						                       e.toString(),Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return admins;
	}

	/**
	 * Method to get all clerks from database
	 *
	 * @return - ArrayList of clerks
	 */
	public ArrayList<Clerk> getAllClerks()
	{
		ArrayList<Clerk> clerks = new ArrayList<>();
		database.getReference("Clerks").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					clerks.add(ds.getValue(Clerk.class));
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get all clerks from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});
		return clerks;
	}

	public ArrayList<Customer> getClerkCustomers(Clerk clerk)
	{
		ArrayList<Customer> customers = new ArrayList<>();
		database.getReference("Clerks").child(clerk.getUsername()).child("customers").get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							customers.add(ds.getValue(Customer.class));
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get clerk's customers from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return customers;
	}

	public ArrayList<Customer> getAllCustomers()
	{
		ArrayList<Customer> customers = new ArrayList<>();
		database.getReference("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					customers.add(ds.getValue(Customer.class));
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get all customers from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return customers;
	}

	public ArrayList<Customer> getAllCustomersForTransfer(String customerID)
	{
		ArrayList<Customer> customers = new ArrayList<>();
		database.getReference("Accounts").get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							if(!ds.getKey().equals(customerID))
								customers.add(ds.getValue(Customer.class));
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get receiving customers from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return customers;
	}

	public ArrayList<Account> getAllAccountsForTransfer(String customerID)
	{
		ArrayList<Account> accounts = new ArrayList<>();
		database.getReference("Accounts").get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							if(!ds.getKey().equals(customerID))
								accounts.add(ds.child(customerID).getValue(Account.class));
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get receiving customers accounts from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return accounts;
	}

	public ArrayList<Account> getAccountsFromCurrentCustomer(String customerID)
	{
		HashMap<String, Account> accountHM = new HashMap<>();
		ArrayList<Account> accounts = new ArrayList<>();
		database.getReference("Accounts").child(customerID)
				.addValueEventListener(new ValueEventListener()
				{
					@Override
					public void onDataChange(@NonNull DataSnapshot snapshot)
					{
						for(DataSnapshot ds : snapshot.getChildren())
						{
//					accountHM.put(ds.getKey(),ds.getValue(Account.class));
//					accounts.add(accountHM.get(ds.getKey()));
							accounts.add(new Account(
									ds.child("accountName").getValue(String.class),
									ds.child("accountNo").getValue(String.class),
									ds.child("accountBalance").getValue(Double.class)
							));
							accounts.get(accounts.size() - 1).getTransactions()
									.addAll(getTransactionsFromCurrentAccount(customerID,accounts.get(
											accounts.size() - 1).getAccountNo()));
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError error)
					{
						Toast.makeText(context,"ERROR - Can't get customer's accounts from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",error.toString());
					}
				});

		return accounts;
	}

	public Customer getCustomerByID(String customerID)
	{
		Customer[] customer = new Customer[1];

		database.getReference(USERS).child(customerID).get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						DataSnapshot ds = task.getResult();
						customer[0] = ds.getValue(Customer.class);
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get customer from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});


//		database.getReference().child(USERS).child(customerID)
//				.addListenerForSingleValueEvent(new ValueEventListener()
//				{
//					@Override
//					public void onDataChange(@NonNull DataSnapshot snapshot)
//					{
//						customer[0] = snapshot.child(customerID).getValue(Customer.class);
//					}
//					@Override
//					public void onCancelled(@NonNull DatabaseError error)
//					{
//						Toast.makeText(context, "ERROR - Can't get customer from DB", Toast.LENGTH_SHORT).show();
//						Log.d("DB_ERROR",error.toString());
//					}
//				});
		return customer[0];
	}

	public ArrayList<Payee> getPayeesFromCurrentCustomer(long customerID)
	{
		ArrayList<Payee> payees = new ArrayList<>();
		database.getReference(USERS).child(String.valueOf(customerID)).child(KEY_PAYEES).get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							payees.add(ds.getValue(Payee.class));
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get payees from DB for customer",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return payees;
	}

	public ArrayList<Transaction> getTransactionsFromCurrentAccount(String customerID,String accountNo)
	{
		ArrayList<Transaction> transactions = new ArrayList<>();
		database.getReference("Accounts").child(customerID).child(accountNo)
				.child(KEY_TRANSACTIONS).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					transactions.add(ds.getValue(Transaction.class));
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(context,"ERROR - Can't get transactions from DB for this account",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});

		return transactions;
	}
}
