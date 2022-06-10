package com.example.ymdbanking;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.db.ApplicationDB;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Payee;
import com.example.ymdbanking.model.Account;
import com.example.ymdbanking.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity
{
	private Spinner spnSelectAccount;
	private TextView txtNoPayeesMsg;
	private Spinner spnSelectPayee;
	private EditText edtPaymentAmount;
	private Button btnMakePayment;
	private FloatingActionButton btnAddPayee;

	private Dialog payeeDialog;
	private EditText edtPayeeName;
	private Button btnCancel;
	private Button btnConfirmAddPayee;

	private SessionManager sessionManager;
	private Customer customer;

	private ArrayList<Account> accounts;
	private ArrayAdapter<Account> accountAdapter;
	private ArrayList<Payee> payees;
	private ArrayAdapter<Payee> payeeAdapter;

	private View.OnClickListener addPayeeClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnCancel.getId())
			{
				payeeDialog.dismiss();
				Toast.makeText(PaymentActivity.this,"Payee Creation Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnConfirmAddPayee.getId())
			{
				addPayee();
			}
		}
	};

	View.OnClickListener buttonClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnMakePayment.getId())
			{
				makePayment();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		spnSelectAccount = findViewById(R.id.spn_select_acc);
		txtNoPayeesMsg = findViewById(R.id.txt_no_payees);
		spnSelectPayee = findViewById(R.id.spn_select_payee);
		edtPaymentAmount = findViewById(R.id.edt_payment_amount);
		btnMakePayment = findViewById(R.id.btn_make_payment);
		btnAddPayee = findViewById(R.id.floating_action_btn);

		sessionManager = new SessionManager(PaymentActivity.this,SessionManager.USER_SESSION);
		customer = sessionManager.getCustomerObjFromSession();
		customer.setPayees(new ArrayList<>());

		getPayees();
//		setValues();
	}

	/**
	 * method used to setup the values for the views and fields
	 */
	private void setValues()
	{
		btnMakePayment.setOnClickListener(buttonClickListener);

		btnAddPayee.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				displayPayeeDialog();
			}
		});

		accounts = customer.getAccounts();
		accountAdapter = new ArrayAdapter<Account>(PaymentActivity.this,android.R.layout.simple_spinner_item,accounts);
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnSelectAccount.setAdapter(accountAdapter);

		payees = customer.getPayees();

		payeeAdapter = new ArrayAdapter<Payee>(PaymentActivity.this,android.R.layout.simple_spinner_item,payees);
		payeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnSelectPayee.setAdapter(payeeAdapter);

		checkPayeeInformation();
	}

	private void displayPayeeDialog()
	{

		payeeDialog = new Dialog(PaymentActivity.this);
		payeeDialog.setContentView(R.layout.add_payee_dialog);

		payeeDialog.setCanceledOnTouchOutside(true);
		payeeDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(PaymentActivity.this,"Payee Addition Cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		edtPayeeName = payeeDialog.findViewById(R.id.edt_account_name);

		btnCancel = payeeDialog.findViewById(R.id.btn_cancel_account_dlg);
		btnConfirmAddPayee = payeeDialog.findViewById(R.id.btn_add_account_dlg);

		btnCancel.setOnClickListener(addPayeeClickListener);
		btnConfirmAddPayee.setOnClickListener(addPayeeClickListener);

		payeeDialog.show();
	}

	/**
	 * method that checks the information of the payee
	 */
	private void checkPayeeInformation()
	{
		if(customer.getPayees().size() == 0)
		{
			txtNoPayeesMsg.setVisibility(VISIBLE);

			spnSelectPayee.setVisibility(GONE);
			edtPaymentAmount.setVisibility(GONE);
			btnMakePayment.setVisibility(GONE);
		}
		else
		{
			txtNoPayeesMsg.setVisibility(GONE);

			spnSelectPayee.setVisibility(VISIBLE);
			edtPaymentAmount.setVisibility(VISIBLE);
			btnMakePayment.setVisibility(VISIBLE);
		}
	}

	/**
	 * method that makes a payment
	 */
	private void makePayment()
	{

		boolean isNum = false;
		double paymentAmount = 0;

		try
		{
			paymentAmount = Double.parseDouble(edtPaymentAmount.getText().toString());
			if(Double.parseDouble(edtPaymentAmount.getText().toString()) >= 0.01)
			{
				isNum = true;
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}

		if(isNum)
		{

			int selectedAccountIndex = spnSelectAccount.getSelectedItemPosition();

			if(paymentAmount >
			   customer.getAccounts().get(selectedAccountIndex).getAccountBalance())
			{
				Toast.makeText(PaymentActivity.this,"You do not have sufficient funds to make this payment",Toast.LENGTH_SHORT).show();
			}
			else
			{

				int selectedPayeeIndex = spnSelectPayee.getSelectedItemPosition();
				customer.getAccounts().get(selectedAccountIndex).addPaymentTransaction(customer.getPayees().get(selectedPayeeIndex),paymentAmount);

				accounts = customer.getAccounts();
				spnSelectAccount.setAdapter(accountAdapter);
				spnSelectAccount.setSelection(selectedAccountIndex);

				ApplicationDB applicationDb = new ApplicationDB(PaymentActivity.this);
				applicationDb.saveNewTransaction(customer,customer.getAccounts().get(selectedAccountIndex).getTransactions()
						                                          .get(customer.getAccounts().get(selectedAccountIndex).getTransactions().size() - 1));
				applicationDb.overwriteAccount(customer,customer.getAccounts().get(selectedAccountIndex));
				sessionManager.saveCustomerObjForSession(customer);

				Toast.makeText(PaymentActivity.this,
						"Payment of $" + String.format(Locale.getDefault(),"%.2f",paymentAmount) +
						" successfully made",Toast.LENGTH_SHORT).show();
				edtPaymentAmount.getText().clear();
			}
		}
		else
		{
			Toast.makeText(PaymentActivity.this,"Please enter a valid number, greater than $0.01",Toast.LENGTH_SHORT).show();
			edtPaymentAmount.getText().clear();
		}
	}

	/**
	 * method that adds a payee
	 */
	private void addPayee()
	{
		if(!(edtPayeeName.getText().toString().equals("")))
		{

			boolean match = false;
			for(int i = 0; i < customer.getPayees().size(); i++)
			{
				if(edtPayeeName.getText().toString().equalsIgnoreCase(customer.getPayees().get(i).getPayeeName()))
				{
					match = true;
				}
			}

			if(!match)
			{
				customer.addPayee(edtPayeeName.getText().toString());

				edtPayeeName.setText("");

				txtNoPayeesMsg.setVisibility(GONE);
				spnSelectPayee.setVisibility(VISIBLE);
				edtPaymentAmount.setVisibility(VISIBLE);
				btnMakePayment.setVisibility(VISIBLE);

				payees = customer.getPayees();
				spnSelectPayee.setAdapter(payeeAdapter);
				spnSelectPayee.setSelection(customer.getPayees().size() - 1);

				ApplicationDB applicationDb = new ApplicationDB(PaymentActivity.this.getApplicationContext());
				applicationDb.saveNewPayee(customer,customer.getPayees().get(
						customer.getPayees().size() - 1));
				sessionManager.saveCustomerObjForSession(customer);

				Toast.makeText(PaymentActivity.this,"Payee Added Successfully",Toast.LENGTH_SHORT).show();

				payeeDialog.dismiss();

			}
			else
			{
				Toast.makeText(PaymentActivity.this,"A Payee with that name already exists",Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void getPayees()
	{
		FirebaseDatabase.getInstance().getReference("Payees").child(customer.getId()).get()
			.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					customer.getPayees().add(ds.getValue(Payee.class));

				setValues();
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(PaymentActivity.this,"Can't get list of payees for customer " + customer.getUsername(),Toast.LENGTH_SHORT).show();
				Log.d("GET_PAYEES_ERROR",e.toString());
			}
		});
	}

	public void getAccountsFromCurrentCustomer(String customerID)
	{
		FirebaseDatabase.getInstance().getReference("Accounts").child(customerID)
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
//					accountHM.put(ds.getKey(),ds.getValue(Account.class));
//					accounts.add(accountHM.get(ds.getKey()));
					customer.getAccounts().add(new Account(
							ds.child("accountName").getValue(String.class),
							ds.child("accountNo").getValue(String.class),
							ds.child("accountBalance").getValue(Double.class)
					));
					customer.getAccounts().get(customer.getAccounts().size() - 1).getTransactions()
							.addAll(getTransactionsForAccount(customer.getAccounts().get(
									customer.getAccounts().size() - 1)));
				}

				//Setting adapter for customers accounts after pulling data from DB
				//Account adapter
				accountAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,customer.getAccounts());
				accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spnSelectAccount.setAdapter(accountAdapter);
				//Payee adapter
				payeeAdapter = new ArrayAdapter<>(PaymentActivity.this,android.R.layout.simple_spinner_item,customer.getPayees());
				payeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spnSelectPayee.setAdapter(payeeAdapter);
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(getApplicationContext(),"ERROR - Can't get customer's accounts from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
	}

	public ArrayList<Transaction> getTransactionsForAccount(Account account)
	{
		ArrayList<Transaction> transactions = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Accounts").child(customer.getId()).child(account.getAccountNo())
			.child("transactions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
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
				Toast.makeText(getApplicationContext(),"ERROR - Can't get transactions from DB for this account",Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
		return transactions;
	}
}
