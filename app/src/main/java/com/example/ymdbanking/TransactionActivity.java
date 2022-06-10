package com.example.ymdbanking;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.TransactionAdapter;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Transaction;
import com.example.ymdbanking.model.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class TransactionActivity extends AppCompatActivity
{
	//Enums
	public enum TransactionTypeFilter
	{
		ALL_TRANSACTIONS(0),
		PAYMENTS(1),
		TRANSFERS(2),
		DEPOSITS(3),
		LOANS(4),
		CASH_DEPOSITS(5);

		private final int transFilterID;

		TransactionTypeFilter(int transFilterID)
		{
			this.transFilterID = transFilterID;
		}

		public TransactionTypeFilter getTransFilter(int index)
		{
			for (TransactionTypeFilter filter : TransactionTypeFilter.values())
			{
				if (filter.transFilterID == index)
				{
					return filter;
				}
			}
			return null;
		}
	}

	public enum DateFilter
	{
		OLDEST_NEWEST(0),
		NEWEST_OLDEST(1);

		private final int dateFilterID;

		DateFilter(int dateFilterID)
		{
			this.dateFilterID = dateFilterID;
		}

		public DateFilter getDateFilter(int index)
		{
			for (DateFilter filter : DateFilter.values())
			{
				if (filter.dateFilterID == index)
				{
					return filter;
				}
			}
			return null;
		}
	}

	//Inner class
	class TransactionComparator implements Comparator<Transaction>
	{
		public int compare(Transaction transOne, Transaction transTwo)
		{

			Date dateOne = null;
			Date dateTwo = null;

			try
			{
				dateOne = Transaction.DATE_FORMAT.parse(transOne.getTimestamp());
				dateTwo = Transaction.DATE_FORMAT.parse(transTwo.getTimestamp());
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			if (dateOne.compareTo(dateTwo) > 0)
			{
				return (1);
			} else if (dateOne.compareTo(dateTwo) < 0)
			{
				return (-1);
			} else if (dateOne.compareTo(dateTwo) == 0)
			{
				return (1);
			}
			return (1);
		}
	}

	//Fields
	private TextView txtAccountName;
	private TextView txtAccountBalance;

	private TextView txtTransactionMsg;
	private TextView txtTransfersMsg;
	private TextView txtPaymentsMsg;
	private TextView txtDepositMsg;

	private Spinner spnAccounts;
	private Spinner spnTransactionTypeFilter;
	private Spinner spnDateFilter;

	private TransactionTypeFilter transFilter;
	private DateFilter dateFilter;

	private ListView lstTransactions;
	private Customer customer;
	private int selectedAccountIndex;
	private SessionManager sessionManager;

	Spinner.OnItemSelectedListener spnClickListener = new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
		{
			if (adapterView.getId() == spnAccounts.getId())
			{
				selectedAccountIndex = i;
				txtAccountName.setText("Account: " +
				                       customer.getAccounts().get(selectedAccountIndex).toTransactionString());
				txtAccountBalance.setText("Balance: $" +
				                          String.format(Locale.getDefault(), "%.2f", customer.getAccounts().get(selectedAccountIndex).getAccountBalance()));
			}
			else if (adapterView.getId() == spnTransactionTypeFilter.getId())
			{
				transFilter = transFilter.getTransFilter(i);
			}
			else if (adapterView.getId() == spnDateFilter.getId())
			{
				dateFilter = dateFilter.getDateFilter(i);
			}

			setupTransactionAdapter(selectedAccountIndex, transFilter, dateFilter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView)
		{

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transaction);
		sessionManager = new SessionManager(TransactionActivity.this, "AccountView");
		selectedAccountIndex = sessionManager.userSession.getInt("SelectedAccount", 0);
		this.setTitle("Transactions");

		txtAccountName = findViewById(R.id.txt_account_name);
		txtAccountBalance = findViewById(R.id.txt_account_balance);

		txtTransactionMsg = findViewById(R.id.txt_no_transactions);
		txtPaymentsMsg = findViewById(R.id.txt_no_payments);
		txtTransfersMsg = findViewById(R.id.txt_no_transfers);
		txtDepositMsg = findViewById(R.id.txt_no_deposits);

		spnAccounts = findViewById(R.id.spn_accounts);
		spnTransactionTypeFilter = findViewById(R.id.spn_type_filter);
		spnDateFilter = findViewById(R.id.spn_date_filter);

		lstTransactions = findViewById(R.id.lst_transactions);

		setValues();
	}

	/**
	 * method used to setup the values for the views and fields
	 */
	private void setValues()
	{

		sessionManager = new SessionManager(getApplicationContext(), SessionManager.USER_SESSION);
		customer = sessionManager.getCustomerObjFromSession();

		transFilter = TransactionTypeFilter.ALL_TRANSACTIONS;
		dateFilter = DateFilter.OLDEST_NEWEST;

		setupTransactionAdapter(selectedAccountIndex, transFilter, dateFilter);

		setupSpinners();
		spnAccounts.setSelection(selectedAccountIndex);

		txtAccountName.setText("Account: " +
		                       customer.getAccounts().get(selectedAccountIndex).toTransactionString());
		txtAccountBalance.setText("Balance: $" +
		                          String.format(Locale.getDefault(), "%.2f", customer.getAccounts().get(selectedAccountIndex).getAccountBalance()));
	}

	private void setupSpinners()
	{

		ArrayAdapter<Account> accountAdapter = new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, customer.getAccounts());
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnAccounts.setAdapter(accountAdapter);

		ArrayAdapter<String> transTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.transaction_filters));
		transTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnTransactionTypeFilter.setAdapter(transTypeAdapter);

		ArrayAdapter<String> dateFilterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.date_filters));
		dateFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDateFilter.setAdapter(dateFilterAdapter);

		spnAccounts.setOnItemSelectedListener(spnClickListener);
		spnTransactionTypeFilter.setOnItemSelectedListener(spnClickListener);
		spnDateFilter.setOnItemSelectedListener(spnClickListener);

	}

	/**
	 * method used to setup the adapters
	 */
	private void setupTransactionAdapter(int selectedAccountIndex, TransactionTypeFilter transFilter, DateFilter dateFilter)
	{
		try
		{
			ArrayList<Transaction> transactions = customer.getAccounts().get(selectedAccountIndex).getTransactions();

			txtDepositMsg.setVisibility(GONE);
			txtTransfersMsg.setVisibility(GONE);
			txtPaymentsMsg.setVisibility(GONE);

			try
			{
				if(transactions.size() > 0)
				{

					txtTransactionMsg.setVisibility(GONE);
					lstTransactions.setVisibility(VISIBLE);

					if(dateFilter == DateFilter.OLDEST_NEWEST)
					{
						Collections.sort(transactions,new TransactionComparator());
					}
					else if(dateFilter == DateFilter.NEWEST_OLDEST)
					{
						Collections.sort(transactions,Collections.reverseOrder(new TransactionComparator()));
					}

					if(transFilter == TransactionTypeFilter.ALL_TRANSACTIONS)
					{
						TransactionAdapter transactionAdapter = new TransactionAdapter(this,R.layout.lst_transactions,transactions);
						lstTransactions.setAdapter(transactionAdapter);
					}
					else if(transFilter == TransactionTypeFilter.PAYMENTS)
					{
						displayPayments(transactions);
					}
					else if(transFilter == TransactionTypeFilter.TRANSFERS)
					{
						displayTransfers(transactions);
					}
					else if(transFilter == TransactionTypeFilter.DEPOSITS)
					{
						displayDeposits(transactions);
					}
					else if(transFilter == TransactionTypeFilter.LOANS)
					{
						displayLoans(transactions);
					}
					else if(transFilter == TransactionTypeFilter.CASH_DEPOSITS)
					{
						displayCashDeposits(transactions);
					}

				}
				else
				{
					txtTransactionMsg.setVisibility(VISIBLE);
					lstTransactions.setVisibility(GONE);
				}
			}
			catch(NullPointerException exception)
			{
				transactions = new ArrayList<>(0);
				txtTransactionMsg.setVisibility(VISIBLE);
				lstTransactions.setVisibility(GONE);
				Toast.makeText(this,"No transaction for this account",Toast.LENGTH_SHORT).show();
			}
		}
		catch(NullPointerException exception)
		{
			customer.setAccounts(new ArrayList<>(0));
			Toast.makeText(this,"There's no account for this user yet",Toast.LENGTH_SHORT).show();
		}
	}

	private void displayCashDeposits(ArrayList<Transaction> transactions)
	{
		ArrayList<Transaction> cash_deposits = new ArrayList<>();

		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.CASH_DEPOSIT)
			{
				cash_deposits.add(transactions.get(i));
			}
		}
		if (cash_deposits.size() == 0)
		{
			txtPaymentsMsg.setVisibility(VISIBLE);
			lstTransactions.setVisibility(GONE);
		} else
		{
			lstTransactions.setVisibility(VISIBLE);
			TransactionAdapter transactionAdapter = new TransactionAdapter(this,R.layout.lst_transactions,cash_deposits);
			lstTransactions.setAdapter(transactionAdapter);
		}
	}

	private void displayLoans(ArrayList<Transaction> transactions)
	{
		ArrayList<Transaction> loans = new ArrayList<>();

		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.LOAN)
			{
				loans.add(transactions.get(i));
			}
		}
		if (loans.size() == 0)
		{
			txtPaymentsMsg.setVisibility(VISIBLE);
			lstTransactions.setVisibility(GONE);
		} else
		{
			lstTransactions.setVisibility(VISIBLE);
			TransactionAdapter transactionAdapter = new TransactionAdapter(this, R.layout.lst_transactions, loans);
			lstTransactions.setAdapter(transactionAdapter);
		}
	}

	private void displayPayments(ArrayList<Transaction> transactions)
	{
		ArrayList<Transaction> payments = new ArrayList<>();

		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.PAYMENT)
			{
				payments.add(transactions.get(i));
			}
		}
		if (payments.size() == 0)
		{
			txtPaymentsMsg.setVisibility(VISIBLE);
			lstTransactions.setVisibility(GONE);
		} else
		{
			lstTransactions.setVisibility(VISIBLE);
			TransactionAdapter transactionAdapter = new TransactionAdapter(this, R.layout.lst_transactions, payments);
			lstTransactions.setAdapter(transactionAdapter);
		}
	}

	private void displayTransfers(ArrayList<Transaction> transactions)
	{
		ArrayList<Transaction> transfers = new ArrayList<>();

		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.TRANSFER)
			{
				transfers.add(transactions.get(i));
			}
		}
		if (transfers.size() == 0)
		{
			txtTransfersMsg.setVisibility(VISIBLE);
			lstTransactions.setVisibility(GONE);
		} else
		{
			lstTransactions.setVisibility(VISIBLE);
			TransactionAdapter transactionAdapter = new TransactionAdapter(this, R.layout.lst_transactions, transfers);
			lstTransactions.setAdapter(transactionAdapter);
		}
	}

	private void displayDeposits(ArrayList<Transaction> transactions)
	{
		ArrayList<Transaction> deposits = new ArrayList<>();

		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.DEPOSIT)
			{
				deposits.add(transactions.get(i));
			}
		}
		if (deposits.size() == 0)
		{
			txtDepositMsg.setVisibility(VISIBLE);
			lstTransactions.setVisibility(GONE);
		} else
		{
			lstTransactions.setVisibility(VISIBLE);
			TransactionAdapter transactionAdapter = new TransactionAdapter(this, R.layout.lst_transactions, deposits);
			lstTransactions.setAdapter(transactionAdapter);
		}
	}

}
