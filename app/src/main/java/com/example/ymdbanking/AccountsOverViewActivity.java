package com.example.ymdbanking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ymdbanking.adapters.AccountAdapter;
import com.example.ymdbanking.db.ApplicationDB;
import com.example.ymdbanking.model.Account;
import com.example.ymdbanking.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AccountsOverViewActivity extends AppCompatActivity
{
	private FloatingActionButton fab;
	private Dialog accountDialog;
	private EditText edtAccountName;
	private EditText edtInitAccountBalance;
	private TextView txtTitleMessage;
	private TextView txtDetailMessage;
	private ListView lstAccounts;
	private Button btnCancel;
	private Button btnAddAccount;
	private String accountName, accountBalance;
	private Customer customer;
	private int selectedAccountIndex;
	private SessionManager sessionManager;
	private static final double DEPOSIT_MIN_LIMIT = 100;
	private static final double LOAN_MIN_LIMIT = 1000;

	private View.OnClickListener addAccountClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnCancel.getId())
			{
				accountDialog.dismiss();
				Toast.makeText(AccountsOverViewActivity.this,"Account Creation Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnAddAccount.getId())
			{
				addAccount();
			}
		}
	};

	public static double getLoanMinLimit()
	{
		return LOAN_MIN_LIMIT;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accounts_over_view);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		txtTitleMessage = findViewById(R.id.txt_title_msg);
		txtDetailMessage = findViewById(R.id.txt_details_msg);
		lstAccounts = findViewById(R.id.lst_accounts);
		fab = findViewById(R.id.btn_add_clerk_overview);

		SessionManager sessionManager = new SessionManager(this,SessionManager.USER_SESSION);
		customer = sessionManager.getCustomerObjFromSession();
		customer.setAccounts(sessionManager.getAccountsObjFromSession());
		setValuesForCustomer();
		selectedAccountIndex = 0;

		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(customer.getAccounts().size() >= 10)
				{
					Toast.makeText(AccountsOverViewActivity.this,"You have reached the maximum amount of accounts (10)",Toast.LENGTH_SHORT).show();
				}
				else
				{
					displayAccountDialog();
				}
			}
		});
	}

	private void setValuesForCustomer()
	{
		customer.setAccounts(new ArrayList<>());
		Customer tempCustomer = new Customer();
		tempCustomer.setAccounts(new ArrayList<>(0));
		FirebaseDatabase.getInstance().getReference("Accounts").child(customer.getId()).get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							tempCustomer.getAccounts().add(ds.getValue(Account.class));
						//Checking if there's any mismatch on customer's accounts between phone's memory and DB
						if(customer.getAccounts().size() != tempCustomer.getAccounts().size())
							//If there's a mismatch than we'll take the data from DB
							customer.setAccounts(tempCustomer.getAccounts());
						sessionManager = new SessionManager(getApplicationContext(),SessionManager.USER_SESSION);
						sessionManager.saveCustomerObjForSession(customer);
						sessionManager.saveAccountsObjForSession(customer.getAccounts());

						AccountAdapter adapter = new AccountAdapter(AccountsOverViewActivity.this,R.layout.lst_accounts,customer.getAccounts());
						lstAccounts.setAdapter(adapter);

						lstAccounts.setOnItemClickListener(new AdapterView.OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> adapterView,View view,int i,long l)
							{
								selectedAccountIndex = i;
								viewAccount();
							}
						});
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

	private void displayAccountDialog()
	{

		accountDialog = new Dialog(AccountsOverViewActivity.this);
		accountDialog.setContentView(R.layout.account_dialog);

		accountDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		accountDialog.setCanceledOnTouchOutside(true);
		accountDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(AccountsOverViewActivity.this,"Account Creation Cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		edtAccountName = accountDialog.findViewById(R.id.edt_name_account_dialog);
		edtInitAccountBalance = accountDialog.findViewById(R.id.edt_init_bal_account_dialog);

		btnCancel = accountDialog.findViewById(R.id.btn_cancel_account_dialog);
		btnAddAccount = accountDialog.findViewById(R.id.btn_success_account_dialog);

		btnCancel.setOnClickListener(addAccountClickListener);
		btnAddAccount.setOnClickListener(addAccountClickListener);

		accountDialog.show();

	}

	private void addAccount()
	{
		String balance = edtInitAccountBalance.getText().toString();
		boolean isNum = false;
		double initDepositAmount = 0;

		if(!(edtAccountName.getText().toString().equals("")))
		{
			try
			{
				initDepositAmount = Double.parseDouble(edtInitAccountBalance.getText().toString());
				isNum = true;
			} catch(Exception e)
			{
				if(!edtInitAccountBalance.getText().toString().equals(""))
				{
					Toast.makeText(this,"Please enter a valid amount",Toast.LENGTH_SHORT).show();
					edtInitAccountBalance.getText().clear();
				}
			}

			if(edtAccountName.getText().toString().length() > 10)
			{
				Toast.makeText(this,R.string.account_name_exceeds_char,Toast.LENGTH_SHORT).show();
				edtAccountName.getText().clear();

			}
			else if((isNum) || balance.equals(""))
			{
				boolean match = false;

				for(int i = 0; i < customer.getAccounts().size(); i++)
				{
					if(edtAccountName.getText().toString().equalsIgnoreCase(customer.getAccounts().get(i).getAccountName()))
					{
						match = true;
					}
				}

				if(!match)
				{
					if(!balance.equals(""))
					{
						if(isNum)
						{
							if(initDepositAmount >= DEPOSIT_MIN_LIMIT)
							{
								ApplicationDB applicationDb = new ApplicationDB(AccountsOverViewActivity.this);
								customer.addAccount(edtAccountName.getText().toString(),initDepositAmount);
								applicationDb.saveNewAccount(customer,customer.getAccounts().get(
										customer.getAccounts().size() - 1));
								Toast.makeText(this,R.string.acc_saved_successfully,Toast.LENGTH_SHORT).show();
							}
							else
							{
								Toast.makeText(getApplicationContext(),
										"There's a minimum deposit limit of " +
										DEPOSIT_MIN_LIMIT,Toast.LENGTH_SHORT).show();
							}
						}
					}
					if(customer.getAccounts().size() >= 1)
					{
						//txtTitleMessage.setText("Select an Account to view Transactions");
						AccountAdapter adapter = new AccountAdapter(AccountsOverViewActivity.this,R.layout.lst_accounts,customer.getAccounts());
						lstAccounts.setAdapter(adapter);
						txtDetailMessage.setVisibility(View.VISIBLE);
						lstAccounts.setVisibility(View.VISIBLE);
					}
					SessionManager sessionManager = new SessionManager(this,SessionManager.USER_SESSION);
					Gson gson = new Gson();
					String json = gson.toJson(customer);
					sessionManager.editor.putString(SessionManager.SESSION_OBJ,json);

					accountDialog.dismiss();
				}
				else
				{
					Toast.makeText(this,R.string.account_name_error,Toast.LENGTH_SHORT).show();
					edtAccountName.getText().clear();
				}
			}
		}
		else
		{
			Toast.makeText(this,"Please enter an account name",Toast.LENGTH_SHORT).show();
		}
	}

	public void viewAccount()
	{
		sessionManager = new SessionManager(AccountsOverViewActivity.this,"AccountView");
		sessionManager.editor.putInt("SelectedAccount",selectedAccountIndex);
		startActivity(new Intent(AccountsOverViewActivity.this,TransactionActivity.class));
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public String getAccountBalance()
	{
		return accountBalance;
	}

	public void setAccountBalance(String accountBalance)
	{
		this.accountBalance = accountBalance;
	}

	public static double getDepositMinLimit()
	{
		return DEPOSIT_MIN_LIMIT;
	}
}