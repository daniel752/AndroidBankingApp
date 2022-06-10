package com.example.ymdbanking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.TransactionAdapter;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PendingTransactionsActivity extends AppCompatActivity
{
	private ListView lstPendingCashDeposits;
	private ListView lstPendingLoans;
	private Clerk clerk;
	private int selectedTransactionIndex;
	private ArrayList<Transaction> pendingLoans;
	private ArrayAdapter<Transaction> pendingTransactionsAdapter;
	private ArrayList<Transaction> pendingDeposits;
	private Dialog pendingLoanDlg;
	private Dialog pendingCashDepositDlg;
	private TextView txtAmount;
	private TextView txtDestAccount;
	private TextView txtTransactionTime;
	private TextView txtStatus;
	private Button btnApproveLoan;
	private Button btnDenyLoan;

	private View.OnClickListener btnSelectClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.btn_select_cash_deposits)
			{
				showPendingDepositsDialog();
			}
			else if(v.getId() == R.id.btn_select_loans)
			{
				showPendingLoansDialog();
			}
		}
	};

	private View.OnClickListener PendingLoanClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.btn_approve_pending_loan_dialog)
			{
				approveLoan();
			}
			else if(v.getId() == R.id.btn_deny_pending_loan_dialog)
			{
				denyLoan();
			}
		}
	};

	private View.OnClickListener PendingDepositClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.btn_approve_pending_loan_dialog)
			{
				approveDeposit();
			}
			else if(v.getId() == R.id.btn_deny_pending_loan_dialog)
			{
				denyDeposit();
			}
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_loans);

		Button btnSelectDeposits = findViewById(R.id.btn_select_cash_deposits);
		Button btnSelectLoans = findViewById(R.id.btn_select_loans);

		SessionManager sessionManager = new SessionManager(PendingTransactionsActivity.this,SessionManager.USER_SESSION);
		clerk = sessionManager.getClerkObjFromSession();

		btnSelectLoans.setOnClickListener(btnSelectClickListener);
		btnSelectDeposits.setOnClickListener(btnSelectClickListener);
		setValues();
	}

	private void showPendingLoansDialog()
	{
//		pendingLoanDlg = new Dialog(PendingTransactionsActivity.this);
//		pendingLoanDlg.setContentView(R.layout.pending_loan_dialog);
		pendingLoanDlg.setCanceledOnTouchOutside(true);
		pendingLoanDlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Pending loans view cancelled",Toast.LENGTH_SHORT).show();
			}
		});

//		txtTitle = pendingLoanDlg.findViewById(R.id.txt_title_pending_loans);
//		lstPendingLoans = pendingLoanDlg.findViewById(R.id.lst_pending_loans);

		//Setting click listener for list view
		lstPendingLoans.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int i, long id)
			{
				selectedTransactionIndex = i;
				if(pendingLoans.get(selectedTransactionIndex).getStatus() == Transaction.STATUS.DENIED)
					Toast.makeText(PendingTransactionsActivity.this,"You've already denied this loan",Toast.LENGTH_SHORT).show();
				else
					viewLoanDetail();
			}
		});

		pendingLoanDlg.show();
	}

	private void showPendingDepositsDialog()
	{
//		pendingCashDepositDlg = new Dialog(PendingTransactionsActivity.this);
//		pendingCashDepositDlg.setContentView(R.layout.pending_cash_deposit_dialog);
		pendingCashDepositDlg.setCanceledOnTouchOutside(true);
		pendingCashDepositDlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Pending cash deposits view cancelled",Toast.LENGTH_SHORT).show();
			}
		});

//		txtTitle = pendingCashDepositDlg.findViewById(R.id.txt_title_pending_cash_deposits);
//		lstPendingCashDeposits = pendingCashDepositDlg.findViewById(R.id.lst_pending_cash_deposits);

		//Setting click listener for list view
		lstPendingCashDeposits.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int i, long id)
			{
				selectedTransactionIndex = i;
				if(pendingDeposits.get(selectedTransactionIndex).getStatus() == Transaction.STATUS.DENIED)
					Toast.makeText(PendingTransactionsActivity.this,"You've already denied this deposit",Toast.LENGTH_SHORT).show();
				else
					viewCashDepositDetail();
			}
		});

		pendingCashDepositDlg.show();
	}

	private void setValues()
	{
		selectedTransactionIndex = 0;
		pendingLoans = new ArrayList<>();
		pendingDeposits = new ArrayList<>();

		setValuesForDialogs();
		getPendingTransactions();
	}

	private void setValuesForDialogs()
	{
		pendingCashDepositDlg = new Dialog(PendingTransactionsActivity.this);
		pendingCashDepositDlg.setContentView(R.layout.pending_cash_deposit_dialog);
		lstPendingCashDeposits = pendingCashDepositDlg.findViewById(R.id.lst_pending_cash_deposits);

		pendingLoanDlg = new Dialog(PendingTransactionsActivity.this);
		pendingLoanDlg.setContentView(R.layout.pending_loan_dialog);
		lstPendingLoans = pendingLoanDlg.findViewById(R.id.lst_pending_loans);
	}

	private void denyLoan()
	{
		Transaction loan = pendingLoans.get(selectedTransactionIndex);
		//Setting status field in DB to DENIED
		FirebaseDatabase.getInstance().getReference("PendingTransactions").child("Loans").child(clerk.getId())
			.child(loan.getDestinationCustomerId()).child(loan.getTransactionID()).child("status").setValue(Transaction.STATUS.DENIED.toString());
		Toast.makeText(PendingTransactionsActivity.this,"Loan denied on account " + loan.getDestinationAccount(),Toast.LENGTH_SHORT).show();
		pendingLoans.remove(selectedTransactionIndex);
		pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this,R.layout.lst_transactions,pendingLoans);
		lstPendingLoans.setAdapter(pendingTransactionsAdapter);
		pendingLoanDlg.dismiss();
		setValues();
	}

	private void approveLoan()
	{
		Transaction loan = pendingLoans.get(selectedTransactionIndex);

		//Adding loan amount to customer's account
		FirebaseDatabase.getInstance().getReference("Accounts").child(loan.getDestinationCustomerId())
			.child(loan.getDestinationAccount()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				//Getting original account's balance from DB
				DataSnapshot ds = task.getResult();
				double newbalance = ds.child("accountBalance").getValue(double.class);
				//Adding loan amount to account current balance
				newbalance += loan.getAmount();
				loan.setStatus(Transaction.STATUS.APPROVED);

				//Setting account balance in DB to new balance
				FirebaseDatabase.getInstance().getReference("Accounts").child(loan.getDestinationCustomerId())
					.child(loan.getDestinationAccount()).child("accountBalance").setValue(newbalance).addOnCompleteListener(new OnCompleteListener<Void>()
				{
					@Override
					public void onComplete(@NonNull Task<Void> task)
					{
						//Setting status field in DB to APPROVED
						FirebaseDatabase.getInstance().getReference("PendingTransactions").child("Loans").child(clerk.getId())
								.child(loan.getDestinationCustomerId()).child(loan.getTransactionID()).child("status").setValue(Transaction.STATUS.APPROVED.toString());

						FirebaseDatabase.getInstance().getReference("Accounts").child(loan.getDestinationCustomerId())
								.child(loan.getDestinationAccount()).child("transactions").child(String.valueOf(selectedTransactionIndex))
								.child("status").setValue(Transaction.STATUS.APPROVED);

						Toast.makeText(PendingTransactionsActivity.this,"Loan applied on account " + loan.getDestinationAccount(),Toast.LENGTH_SHORT).show();
						pendingLoans.remove(selectedTransactionIndex);
						pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this,R.layout.lst_transactions,pendingLoans);
						lstPendingLoans.setAdapter(pendingTransactionsAdapter);
						pendingLoanDlg.dismiss();
						setValues();
					}
				})
					.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(PendingTransactionsActivity.this,"Loan applied on account " + loan.getDestinationAccount(),Toast.LENGTH_SHORT).show();
						Log.d("SET BALANCE ERROR",e.toString());
					}
				});
			}
		})
			.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Could not apply loan on account " + loan.getDestinationAccount(),Toast.LENGTH_SHORT).show();
				Log.d("FETCH ACCOUNT ERROR",e.toString());
			}
		});
	}

	private void viewLoanDetail()
	{
		pendingLoanDlg = new Dialog(PendingTransactionsActivity.this);
		pendingLoanDlg.setContentView(R.layout.loan_detail_dialog);
		pendingLoanDlg.setCanceledOnTouchOutside(true);
		pendingLoanDlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Loan view cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		txtAmount = pendingLoanDlg.findViewById(R.id.txt_amount_pending_loan_dialog);
		txtDestAccount = pendingLoanDlg.findViewById(R.id.txt_dst_acc_pending_loan_dialog);
		txtTransactionTime = pendingLoanDlg.findViewById(R.id.txt_time_pending_loan_dialog);
		txtStatus = pendingLoanDlg.findViewById(R.id.txt_status_loan_dialog);
		btnApproveLoan = pendingLoanDlg.findViewById(R.id.btn_approve_pending_loan_dialog);
		btnDenyLoan = pendingLoanDlg.findViewById(R.id.btn_deny_pending_loan_dialog);

		Transaction loan = pendingLoans.get(selectedTransactionIndex);
		txtAmount.setText(String.valueOf((int) loan.getAmount()));
		txtDestAccount.setText(loan.getDestinationAccount());
		txtTransactionTime.setText(loan.getTimestamp());
		txtStatus.setText(loan.getStatus().toString());

		btnApproveLoan.setOnClickListener(PendingLoanClickListener);
		btnDenyLoan.setOnClickListener(PendingLoanClickListener);

		pendingLoanDlg.show();
	}

	private void viewCashDepositDetail()
	{
		pendingCashDepositDlg = new Dialog(PendingTransactionsActivity.this);
		pendingCashDepositDlg.setContentView(R.layout.loan_detail_dialog);
		pendingCashDepositDlg.setCanceledOnTouchOutside(true);
		pendingCashDepositDlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Loan view cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		txtAmount = pendingCashDepositDlg.findViewById(R.id.txt_amount_pending_loan_dialog);
		txtDestAccount = pendingCashDepositDlg.findViewById(R.id.txt_dst_acc_pending_loan_dialog);
		txtTransactionTime = pendingCashDepositDlg.findViewById(R.id.txt_time_pending_loan_dialog);
		txtStatus = pendingCashDepositDlg.findViewById(R.id.txt_status_loan_dialog);
		btnApproveLoan = pendingCashDepositDlg.findViewById(R.id.btn_approve_pending_loan_dialog);
		btnDenyLoan = pendingCashDepositDlg.findViewById(R.id.btn_deny_pending_loan_dialog);

		Transaction deposit = pendingDeposits.get(selectedTransactionIndex);
		txtAmount.setText(String.valueOf((int) deposit.getAmount()));
		txtDestAccount.setText(deposit.getDestinationAccount());
		txtTransactionTime.setText(deposit.getTimestamp());
		txtStatus.setText(deposit.getStatus().toString());

		btnApproveLoan.setOnClickListener(PendingDepositClickListener);
		btnApproveLoan.setText("Approve Deposit");
		btnDenyLoan.setOnClickListener(PendingDepositClickListener);
		btnDenyLoan.setText("Deny Deposit");

		pendingCashDepositDlg.show();
	}

	private void denyDeposit()
	{
		Transaction loan = pendingDeposits.get(selectedTransactionIndex);
		//Setting status field in DB to DENIED
		FirebaseDatabase.getInstance().getReference("PendingTransactions").child("CashDeposits").child(clerk.getId())
				.child(loan.getDestinationCustomerId()).child(loan.getTransactionID()).child("status").setValue(Transaction.STATUS.DENIED.toString());
		Toast.makeText(PendingTransactionsActivity.this,"Loan denied on account " + loan.getDestinationAccount(),Toast.LENGTH_SHORT).show();
		pendingDeposits.remove(selectedTransactionIndex);
		pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this,R.layout.lst_transactions,pendingDeposits);
		lstPendingCashDeposits.setAdapter(pendingTransactionsAdapter);
		pendingCashDepositDlg.dismiss();
		setValues();
	}

	private void approveDeposit()
	{
		Transaction deposit = pendingDeposits.get(selectedTransactionIndex);

		//Adding loan amount to customer's account
		FirebaseDatabase.getInstance().getReference("Accounts").child(deposit.getDestinationCustomerId())
				.child(deposit.getDestinationAccount()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				//Getting original account's balance from DB
				DataSnapshot ds = task.getResult();
				double newbalance = ds.child("accountBalance").getValue(double.class);
				//Adding loan amount to account current balance
				newbalance += deposit.getAmount();

				//Setting account balance in DB to new balance
				FirebaseDatabase.getInstance().getReference("Accounts").child(deposit.getDestinationCustomerId())
					.child(deposit.getDestinationAccount()).child("accountBalance").setValue(newbalance).addOnCompleteListener(new OnCompleteListener<Void>()
				{
					@Override
					public void onComplete(@NonNull Task<Void> task)
					{
						//Setting status field in DB to APPROVED
						FirebaseDatabase.getInstance().getReference("PendingTransactions").child("CashDeposits").child(clerk.getId())
								.child(deposit.getDestinationCustomerId()).child(deposit.getTransactionID()).child("status").setValue(Transaction.STATUS.APPROVED.toString());

						Toast.makeText(PendingTransactionsActivity.this,"Cash deposit applied on account " + deposit.getDestinationAccount(),Toast.LENGTH_SHORT).show();
						pendingDeposits.remove(selectedTransactionIndex);
						pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this,R.layout.lst_transactions,pendingDeposits);
						lstPendingCashDeposits.setAdapter(pendingTransactionsAdapter);
						pendingCashDepositDlg.dismiss();
						setValues();
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(PendingTransactionsActivity.this,"Loan applied on account " + deposit.getDestinationAccount(),Toast.LENGTH_SHORT).show();
						Log.d("SET BALANCE ERROR",e.toString());
					}
				});
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Could not apply loan on account " + deposit.getDestinationAccount(),Toast.LENGTH_SHORT).show();
				Log.d("FETCH ACCOUNT ERROR",e.toString());
			}
		});
	}

	private void getPendingTransactions()
	{
		FirebaseDatabase.getInstance().getReference("PendingTransactions").child("Loans").child(clerk.getId()).get()
			.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
					for(DataSnapshot dsa : ds.getChildren())
					{
						if(dsa.getValue(Transaction.class).getStatus() == Transaction.STATUS.PENDING)
							pendingLoans.add(dsa.getValue(Transaction.class));
					}
				}

				pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this, R.layout.lst_transactions,pendingLoans);
				lstPendingLoans.setAdapter(pendingTransactionsAdapter);
			}
		})
			.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Can't get pending loans from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_GET_LOANS_ERROR",e.toString());
			}
		});

		FirebaseDatabase.getInstance().getReference("PendingTransactions").child("CashDeposits").child(clerk.getId()).get()
			.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
					for(DataSnapshot dsa : ds.getChildren())
					{
						if(dsa.getValue(Transaction.class).getStatus() == Transaction.STATUS.PENDING)
							pendingDeposits.add(dsa.getValue(Transaction.class));
					}
				}

				pendingTransactionsAdapter = new TransactionAdapter(PendingTransactionsActivity.this, R.layout.lst_transactions,pendingDeposits);
				lstPendingCashDeposits.setAdapter(pendingTransactionsAdapter);
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(PendingTransactionsActivity.this,"Can't get pending loans from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_GET_DEPOSITS_ERROR",e.toString());
			}
		});
	}
}
