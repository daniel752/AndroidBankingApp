package com.example.ymdbanking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ymdbanking.api.CurrencyConverter;
import com.example.ymdbanking.db.ApplicationDB;
import com.example.ymdbanking.model.Account;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.view.Change;


import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

	static final float END_SCALE = 0.7f;
	private static final double DEPOSIT_MIN_LIMIT = AccountsOverViewActivity.getDepositMinLimit();
	private static final double LOAN_MIN_LIMIT = AccountsOverViewActivity.getLoanMinLimit();

	//Drawer menu
	DrawerLayout drawerLayout;
	NavigationView navigationView;
	ImageView menuIcon;
	LinearLayout contentView;
	TextView disp_username, disp_phone;
	Button testB;
	FirebaseAuth mAuth;
	HashMap<String, String> userDetails;

	//Dialogs
	private Dialog depositDialog;
	private EditText edtDepositAmount;
	private Button btnCancel;
	private Button btnSuccess;

	private Spinner accounts;

	private Dialog transferDialog;
	private TextInputEditText transfer_amount;
	private Button btnApprove, btnAbort;
	private Spinner sendingAccount, receivingAccount;

	public String mInput;

	private Spinner spnAccounts;
	private String accountName, depositAmount;
	private Customer customer;
	private Clerk clerk;
	private Admin admin;
	private ArrayAdapter<Account> accountAdapter;
	private SessionManager sessionManager;

	private String TAG = "DashboardActivity";
	private Dialog loanDialog;
	private Spinner topSpinner;
	private Spinner bottomSpinner;
	private EditText edtLoanAmount;
	private ArrayList<Clerk> clerks;
	private String sessionId;

	private Spinner spnSendingAccount;
	private Spinner spnReceivingCustomer;
	private Spinner spnReceivingAccount;
	private double transferAmount;
	private ArrayList<Customer> customersForTransfer;
	private ArrayAdapter<Customer> customerAdapter;
	private ArrayList<Account> accountsToTransfer;
	private ArrayAdapter<Account> accountsToTransferAdapter;

	private final String[] depositMethods = {"Credit","Cash"};
	private ArrayAdapter<String> depositMethodAdapter;
	private Spinner spnDepositMethod;
	private ProgressBar pbDepositDialog;
	private ImageView imgTime;
	private TextView txtWelcome;

	private boolean flag;
	private Clerk customerClerk;

	private View.OnClickListener depositClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnCancel.getId())
			{
				depositDialog.dismiss();
				Toast.makeText(DashboardActivity.this,"Deposit Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnSuccess.getId())
			{
				makeDeposit();
			}
		}
	};

	private View.OnClickListener loanClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnCancel.getId())
			{
				loanDialog.dismiss();
				Toast.makeText(DashboardActivity.this,"Loan Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnSuccess.getId())
			{
				makeLoan();
			}
		}
	};

	private View.OnClickListener transferClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnAbort.getId())
			{
				transferDialog.dismiss();
				Toast.makeText(DashboardActivity.this,"Transfer Cancelled",Toast.LENGTH_SHORT).show();
			}
			if(view.getId() == btnApprove.getId())
			{
				getConversionRate();
			}
		}
	};
	private Dialog helpDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashbord);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//Hooks
		menuIcon = findViewById(R.id.menu_icon);
		imgTime = findViewById(R.id.img_time);
		txtWelcome = findViewById(R.id.txt_welcome);

		//Menu Hooks
		drawerLayout = findViewById(R.id.drawer_layout);
		navigationView = findViewById(R.id.navigation_view);
		contentView = findViewById(R.id.content);

		mAuth = FirebaseAuth.getInstance();

		sessionManager = new SessionManager(this,SessionManager.USER_SESSION);
//		sessionId = sessionManager.userSession.getString(SessionManager.KEY_TYPE_ID,null);
		if(LoginActivity.getUserTypeID() == 1)
			admin = sessionManager.getAdminObjFromSession();
		else if(LoginActivity.getUserTypeID() == 2)
			clerk = sessionManager.getClerkObjFromSession();
		else if(LoginActivity.getUserTypeID() == 3)
		{
			customer = sessionManager.getCustomerObjFromSession();
			setValuesForCustomer();
			setViewForTransfer();
			setValuesForTransfer();
			checkIfHasClerk();
		}
		setViews();
		navigationDrawer();
	}

	private void setViews()
	{
		StringBuilder welcomeString = new StringBuilder();

		Calendar calendar = Calendar.getInstance();

		int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

		if(timeOfDay >= 5 && timeOfDay < 12)
		{
			welcomeString.append(getString(R.string.good_morning));
			imgTime.setImageResource(R.drawable.morning_icon_96);
		}
		else if(timeOfDay >= 12 && timeOfDay < 17)
		{
			welcomeString.append(getString(R.string.good_afternoon));
			imgTime.setImageResource(R.drawable.day_icon_96);
		}
		else
		{
			welcomeString.append(getString(R.string.good_evening));
			imgTime.setImageResource(R.drawable.night_icon_96);
		}
		// if admin user
		if(LoginActivity.getUserTypeID() == 1)
		{
			welcomeString.append(", ")
					.append(admin.getFullName())
					.append(" Welcome to the Bank App Demo ")
					.append(getString(R.string.happy))
					.append(" ");

		}
		else if(LoginActivity.getUserTypeID() == 2)
		{
			welcomeString.append(", ")
					.append(clerk.getFullName())
					.append(" Welcome to the Bank App ")
					.append(getString(R.string.happy))
					.append(" ");
		}
		// if regular user
		else if(LoginActivity.getUserTypeID() == 3)
		{
			welcomeString.append(", ")
					.append(customer.getFullName())
					.append(" Welcome to the Bank App ")
					.append(getString(R.string.happy))
					.append(" ");
		}
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		String[] days = getResources().getStringArray(R.array.days);
		String dow = "";

		switch(day)
		{
			case Calendar.SUNDAY:
				dow = days[0];
				break;
			case Calendar.MONDAY:
				dow = days[1];
				break;
			case Calendar.TUESDAY:
				dow = days[2];
				break;
			case Calendar.WEDNESDAY:
				dow = days[3];
				break;
			case Calendar.THURSDAY:
				dow = days[4];
				break;
			case Calendar.FRIDAY:
				dow = days[5];
				break;
			case Calendar.SATURDAY:
				dow = days[6];
				break;
			default:
				break;
		}

		welcomeString.append(dow)
				.append(".");

		txtWelcome.setText(welcomeString.toString());
	}

	private void setValuesForCustomer()
	{
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

						TextView customerEmail = findViewById(R.id.disp_email);
						customerEmail.setText(customer.getEmail());
						sessionManager.saveCustomerObjForSession(customer);
						sessionManager.editor.putString("NumAccounts",String.valueOf(customer.getAccounts().size()));
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

		//Getting all clerks from DB
		clerks = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Users")
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 2)
					{
						clerks.add(ds.getValue(Clerk.class));
						clerks.get(clerks.size() - 1).setLoansToApprove(getLoansForClerk(clerks.get(
								clerks.size() - 1)));
					}

				sessionManager.saveClerksForSession(clerks);
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(getApplicationContext(),"ERROR - Can't get all clerks from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});
	}

	private ArrayList<Transaction> getLoansForClerk(Clerk clerk)
	{
		ArrayList<Transaction> loans = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Loans").child(clerk.getId()).get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							loans.add(ds.getValue(Transaction.class));
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(DashboardActivity.this,"Can't get loans for clerk " +
						                                      clerk.getFullName(),Toast.LENGTH_SHORT).show();
						Log.d("GET_LOANS_ERROR",e.toString());
					}
				});

		return loans;
	}

	/**
	 * Function to get every account's transactions from DB
	 *
	 * @param account - account object to retrieve it's transactions
	 * @return - account's transactions
	 */
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

	//Navigation Drawer Functions
	private void navigationDrawer()
	{
		//Navigation Drawer

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this,drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();
		navigationView.bringToFront();
		navigationView.setNavigationItemSelectedListener(this);

		drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener()
		{
			@Override
			public void onDrawerSlide(@NonNull View drawerView,float slideOffset)
			{
			}

			@Override
			public void onDrawerOpened(@NonNull View drawerView)
			{

				Log.i(TAG,"onDrawerOpened");

			}

			@Override
			public void onDrawerClosed(@NonNull View drawerView)
			{

				Log.i(TAG,"onDrawerClosed");
			}

			@Override
			public void onDrawerStateChanged(int newState)
			{
//                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                drawerLayout.closeDrawers();
			}
		});
		disp_phone = findViewById(R.id.display_phone);

		//User Session Details
		SessionManager sessionManager = new SessionManager(DashboardActivity.this,SessionManager.USER_SESSION);
		userDetails = sessionManager.getUserDetailFromSession();
		String phone = userDetails.get(SessionManager.KEY_PHONE);
		View headerView = navigationView.getHeaderView(0);
		disp_username = headerView.findViewById(R.id.menu_userName);
		disp_username.setText(userDetails.get(SessionManager.KEY_USERNAME));

		//If user is customer
		if(LoginActivity.getUserTypeID() == 3)
		{
			//User's navigation drawer
//            headerView = navigationView.getHeaderView(0);
//            disp_username = headerView.findViewById(R.id.menu_userName);
//            disp_username.setText(userDetails.get(SessionManager.KEY_USERNAME));
			navigationView.getMenu().findItem(R.id.nav_clerks).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_users).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_pending_transactions).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_customers).setVisible(false);
			sessionManager.saveCustomerObjForSession(customer);
		}
		//If user is clerk
		else if(LoginActivity.getUserTypeID() == 2)
		{
			navigationView.getMenu().findItem(R.id.nav_clerks).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_transfer).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_accounts).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_payment).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_deposit).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_change_clerk).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_loan).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_messages).setVisible(false);
			sessionManager.saveClerkObjForSession(clerk);

		}
		//If user is admin
		else if(LoginActivity.getUserTypeID() == 1)
		{
			navigationView.getMenu().findItem(R.id.nav_transfer).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_loan).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_pending_transactions).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_accounts).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_payment).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_deposit).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_transaction).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_messages).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_customers).setVisible(false);

			sessionManager.saveAdminObjForSession(admin);
		}
		disp_phone.setText(phone);

		menuIcon.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(drawerLayout.isDrawerVisible(GravityCompat.START))
					drawerLayout.closeDrawer(GravityCompat.START);
				else
					drawerLayout.openDrawer(GravityCompat.START);
			}
		});

		animateNavigationDrawer();

	}

	private void animateNavigationDrawer()
	{

		drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener()
		{
			@Override
			public void onDrawerSlide(View drawerView,float slideOffset)
			{

				// Scale the View based on current slide offset
				final float diffScaledOffset = slideOffset * (1 - END_SCALE);
				final float offsetScale = 1 - diffScaledOffset;
				contentView.setScaleX(offsetScale);
				contentView.setScaleY(offsetScale);

				// Translate the View, accounting for the scaled width
				final float xOffset = drawerView.getWidth() * slideOffset;
				final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
				final float xTranslation = xOffset - xOffsetDiff;
				contentView.setTranslationX(xTranslation);
			}
		});


	}

	@Override
	public void onBackPressed()
	{

		if(drawerLayout.isDrawerVisible(GravityCompat.START))
			drawerLayout.closeDrawer(GravityCompat.START);
		else
			super.onBackPressed();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu,menu);

		return true;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		int id = item.getItemId();

		if(id == R.id.nav_clerks)
			startActivity(new Intent(DashboardActivity.this,ClerkOverviewActivity.class));
		else if(id == R.id.nav_profile)
			startActivity(new Intent(DashboardActivity.this,UserProfileActivity.class));
		else if(id == R.id.nav_accounts)
			startActivity(new Intent(DashboardActivity.this,AccountsOverViewActivity.class));
		else if(id == R.id.nav_deposit)
		{
			if(customer.getAccounts().size() > 0)
				displayDepositDialog();
			else
				Toast.makeText(getApplicationContext(),"There are no accounts to deposit to",Toast.LENGTH_SHORT).show();
		}
		else if(id == R.id.nav_payment)
		{
			startActivity(new Intent(DashboardActivity.this,PaymentActivity.class));
		}
		else if(id == R.id.nav_transfer)
		{
			if(customer.getAccounts().size() > 0)
				displayTransferDialog();
			else
				Toast.makeText(getApplicationContext(),"There are no accounts to transfer from",Toast.LENGTH_SHORT).show();
		}
		else if(id == R.id.nav_loan)
		{
			if(customer.getAccounts().size() > 0)
				displayLoanDialog();
			else
				Toast.makeText(getApplicationContext(),"There are no accounts to loan to",Toast.LENGTH_SHORT).show();
		}
		else if(id == R.id.nav_pending_transactions)
		{
			startActivity(new Intent(DashboardActivity.this,PendingTransactionsActivity.class));
		}
		else if(id == R.id.nav_change_clerk)
		{
			if(LoginActivity.getUserTypeID() == 1)
			{
				startActivity(new Intent(DashboardActivity.this,ChangeClerkActivity.class));
			}
			else if(LoginActivity.getUserTypeID() == 3)
			{
				if(flag)
				{
					displayChangeClerkDialog();
				}
				else
					Toast.makeText(DashboardActivity.this,
							"You can't change your clerk because you don't have one",Toast.LENGTH_SHORT).show();
			}
		}
		else if(id == R.id.nav_messages)
			startActivity(new Intent(DashboardActivity.this,ShowMessagesActivity.class));
		else if(id == R.id.nav_users)
			startActivity(new Intent(DashboardActivity.this,ShowUsersActivity.class));
		else if(id == R.id.nav_customers)
			startActivity(new Intent(DashboardActivity.this,CustomerOverviewActivity.class));
		else if(id == R.id.nav_help)
		{
			displayHelpDialog();
		}
		else if(id == R.id.nav_logout)
		{
			mAuth.signOut();
			startActivity(new Intent(DashboardActivity.this,LoginActivity.class));
		}
		return true;
	}

	private void displayChangeClerkDialog()
	{
		//Change clerk dialog fields
		Dialog changeClerkDlg = new Dialog(DashboardActivity.this);
		changeClerkDlg.setContentView(R.layout.change_clerk_dialog);
		changeClerkDlg.setCanceledOnTouchOutside(true);
		changeClerkDlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(DashboardActivity.this,"Change clerk action canceled",Toast.LENGTH_SHORT).show();
			}
		});

		TextView txtCurrentClerkTitle = changeClerkDlg.findViewById(R.id.txt_current_clerk_title);
		TextView txtCurrentClerkName = changeClerkDlg.findViewById(R.id.txt_clerk_name);
		TextView txtChangeClerkTitle = changeClerkDlg.findViewById(R.id.txt_change_clerk_title);
		ListView lstClerks = changeClerkDlg.findViewById(R.id.lst_change_clerk);

		ArrayAdapter<Clerk> clerkArrayAdapter = new ArrayAdapter<Clerk>(DashboardActivity.this,android.R.layout.simple_spinner_item,clerks);
		lstClerks.setAdapter(clerkArrayAdapter);

		lstClerks.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				changeClerk(customerClerk,clerks.get(position));
				changeClerkDlg.dismiss();
			}
		});

		txtCurrentClerkName.setText(customerClerk.getFullName());

		changeClerkDlg.show();
	}

	private void changeClerk(Clerk oldClerk,Clerk newClerk)
	{
		FirebaseDatabase.getInstance().getReference("ClerkCustomers")
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
					if(ds.getKey().equals(oldClerk.getId()) && ds.child(customer.getId()).exists())
					{
						HashMap<String, String> customerHashMap = new HashMap<>();
						customerHashMap.put("email",customer.getEmail());
						customerHashMap.put("fullName",customer.getFullName());
						customerHashMap.put("id",customer.getId());
						customerHashMap.put("password",customer.getPassword());
						customerHashMap.put("phone",customer.getPhone());
						customerHashMap.put("typeID",String.valueOf(customer.getTypeID()));
						customerHashMap.put("username",customer.getUsername());
						ds.child(customer.getId()).getRef().removeValue();
						ds.getRef().getRoot().child("ClerkCustomers").child(newClerk.getId()).child(customer.getId()).setValue(customerHashMap);

						Toast.makeText(DashboardActivity.this,"Your clerk has been changed",Toast.LENGTH_SHORT).show();
					}
				}
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(DashboardActivity.this,"Can't change clerks",Toast.LENGTH_SHORT).show();
						Log.d("CHANGE CLERK ERROR",e.toString());
					}
				});
	}

	private void checkIfHasClerk()
	{
		FirebaseDatabase.getInstance().getReference("ClerkCustomers")
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child(customer.getId()).exists())
					{
						flag = true;
						for(Clerk clerk : clerks)
						{
							if(clerk.getId().equals(ds.getKey()))
								customerClerk = clerk;
						}
						break;
					}
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						flag = false;
					}
				});
	}

	private void setViewForTransfer()
	{
		transferDialog = new Dialog(this);
		transferDialog.setContentView(R.layout.transfer_dialog);

		transferDialog.setCanceledOnTouchOutside(true);

		transferDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(DashboardActivity.this,"Transfer Cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		spnSendingAccount = transferDialog.findViewById(R.id.spn_select_customer_acc);
		spnReceivingCustomer = transferDialog.findViewById(R.id.spn_receiving_customer);
		spnReceivingAccount = transferDialog.findViewById(R.id.spn_receiving_acc);
	}

	private void setValuesForTransfer()
	{
		//Setting adapter to current customer's accounts (sending accounts)
		accountAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,customer.getAccounts());
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spnSendingAccount.setAdapter(customerAccountsAdapter);

		//Getting all customers besides the current one (receiving customers)
		customersForTransfer = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Users").get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							if(!ds.getKey().equals(customer.getId()) &&
							   ds.child("typeID").getValue(int.class) == 3)
							{
								customersForTransfer.add(ds.getValue(Customer.class));
								customersForTransfer.get(customersForTransfer.size() -
								                         1).setAccounts(getAccountsFromCurrentCustomer(ds.getKey()));
							}

						//Setting adapter for customers list after pulling data from DB
						customerAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,customersForTransfer);
						customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spnReceivingCustomer.setAdapter(customerAdapter);
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(getApplicationContext(),"ERROR - Can't get receiving customers from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_ERROR",e.toString());
					}
				});
	}

	private void displayDepositDialog()
	{
		depositDialog = new Dialog(this);
		depositDialog.setContentView(R.layout.deposit_dialog);

		depositDialog.setCanceledOnTouchOutside(true);
		depositDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(DashboardActivity.this,"Deposit Cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		TextView txtSelectAccountDeposit = depositDialog.findViewById(R.id.txt_deposit_title);
		TextView txtSelectDepositMethod = depositDialog.findViewById(R.id.txt_select_deposit_method);
		spnAccounts = depositDialog.findViewById(R.id.spn_accounts_deposit_dialog);
		spnDepositMethod = depositDialog.findViewById(R.id.spn_method_deposit_dialog);
		pbDepositDialog = depositDialog.findViewById(R.id.pb_deposit_dialog);
		pbDepositDialog.setVisibility(View.INVISIBLE);

		accountAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,customer.getAccounts());
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnAccounts.setAdapter(accountAdapter);
		spnAccounts.setSelection(0);

		depositMethodAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,depositMethods);
		depositMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDepositMethod.setAdapter(depositMethodAdapter);
		spnDepositMethod.setSelection(0);

		edtDepositAmount = depositDialog.findViewById(R.id.edt_deposit_amount);

		btnCancel = depositDialog.findViewById(R.id.btn_cancel_deposit);
		btnSuccess = depositDialog.findViewById(R.id.btn_deposit);

		btnCancel.setOnClickListener(depositClickListener);
		btnSuccess.setOnClickListener(depositClickListener);

		depositDialog.show();
	}

	private void makeDeposit()
	{
		int selectedAccountIndex = spnAccounts.getSelectedItemPosition();
		int selectedDepositMethod = spnDepositMethod.getSelectedItemPosition();
		double depositAmount = 0;
		boolean isNum = false;

		try
		{
			depositAmount = Double.parseDouble(edtDepositAmount.getText().toString());
			isNum = true;
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		if(!isNum)
		{
			Toast.makeText(this,"Please enter a valid amount",Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(depositAmount < DEPOSIT_MIN_LIMIT)
			{
				Toast.makeText(getApplicationContext(),"There's a minimum deposit limit of " +
				                                       DEPOSIT_MIN_LIMIT,Toast.LENGTH_SHORT).show();
			}
			else
			{
				String selectedMethod = depositMethodAdapter.getItem(selectedDepositMethod);
				customer.getAccounts().get(selectedAccountIndex).addDepositTransaction(customer.getId(),depositAmount,selectedMethod);
				sessionManager.saveCustomerObjForSession(customer);

				//If customer chose cash deposit
				if(selectedMethod.equals(depositMethods[1]))
				{
					if(customerClerk != null)
					{
						Toast.makeText(DashboardActivity.this,"The delivery guy is on his way to you",Toast.LENGTH_SHORT).show();
						try
						{
							pbDepositDialog.setVisibility(View.VISIBLE);
							Thread.sleep(1000);
							Toast.makeText(DashboardActivity.this,"Express cash deposit delivery is on it's wat to us",Toast.LENGTH_SHORT).show();

							final String[] clerkID = new String[1];
							//Finding which clerk is this customer's clerk by looping through the ID's and matching to this customer
							FirebaseDatabase.getInstance().getReference("ClerkCustomers").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
							{
								@Override
								public void onComplete(@NonNull Task<DataSnapshot> task)
								{
									for(DataSnapshot ds : task.getResult().getChildren())
									{
										if(ds.child(customer.getId()).exists())
											clerkID[0] = ds.getKey();
									}
									Transaction transaction = customer.getAccounts().get(selectedAccountIndex).getTransactions().get(
											customer.getAccounts().get(selectedAccountIndex).getTransactions().size() -
											1);

									FirebaseDatabase.getInstance().getReference("PendingTransactions").child("CashDeposits")
											.child(clerkID[0]).child(customer.getId()).child(transaction.getTransactionID()).setValue(transaction);
									Toast.makeText(DashboardActivity.this,"Express cash deposit is now pending for approval by your clerk",Toast.LENGTH_SHORT).show();
									pbDepositDialog.setVisibility(View.INVISIBLE);
								}
							})
									.addOnFailureListener(new OnFailureListener()
									{
										@Override
										public void onFailure(@NonNull Exception e)
										{
											Toast.makeText(DashboardActivity.this,"Can't add cash deposit to pending transactions",Toast.LENGTH_SHORT).show();
											Log.d("CASH DEPOSIT ERROR",e.toString());
										}
									});
						} catch(InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						Toast.makeText(DashboardActivity.this,"Can't make cash deposit because" +
						                                      " you don't have a clerk assigned to you yet",Toast.LENGTH_SHORT).show();
					}
				}
				//If customer chose credit
				else
				{
					ApplicationDB applicationDb = new ApplicationDB(getApplicationContext());
					applicationDb.overwriteAccount(customer,customer.getAccounts().get(selectedAccountIndex));
					Toast.makeText(this,"Deposit of $" +
					                    String.format(Locale.getDefault(),"%.2f",depositAmount) +
					                    " " + "made successfully",Toast.LENGTH_SHORT).show();
				}

				accountAdapter = new ArrayAdapter<Account>(this,android.R.layout.simple_spinner_item,customer.getAccounts());
				accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spnAccounts.setAdapter(accountAdapter);

				depositDialog.dismiss();
				drawerLayout.closeDrawers();
				//manualNavigation(manualNavID.ACCOUNTS_ID, null);
				startActivity(new Intent(DashboardActivity.this,DashboardActivity.class));
			}
		}
	}

	private void displayTransferDialog()
	{
		if(accountsToTransfer.size() != 0 && customersForTransfer.size() != 0)
		{
			//sending account
			sendingAccount = transferDialog.findViewById(R.id.spn_select_customer_acc);
			accountAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,customer.getAccounts());
			accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			sendingAccount.setAdapter(accountAdapter);
			sendingAccount.setSelection(0);

			transfer_amount = transferDialog.findViewById(R.id.transfer_amount);

			btnApprove = transferDialog.findViewById(R.id.transfer_btn);
			btnAbort = transferDialog.findViewById(R.id.btn_cancel_transfer);
			//receiving account

			btnApprove.setOnClickListener(transferClickListener);
			btnAbort.setOnClickListener(transferClickListener);

			transferDialog.show();
		}
		else
		{
			Toast.makeText(DashboardActivity.this,"Can't make transfers yet because you don't have anyone to send to",Toast.LENGTH_SHORT).show();
		}
	}

	private void makeTransfer()
	{
		int receivingProfIndex = spnReceivingCustomer.getSelectedItemPosition();
		int receivingAccIndex = spnReceivingAccount.getSelectedItemPosition();
		int sendingAccIndex = spnSendingAccount.getSelectedItemPosition();

		Account sendingAccount = (Account) spnSendingAccount.getItemAtPosition(sendingAccIndex);
		Account receivingAccount = (Account) spnReceivingAccount.getItemAtPosition(receivingAccIndex);
		Customer receivingCustomer = (Customer) spnReceivingCustomer.getItemAtPosition(receivingProfIndex);

		customer.addTransferTransaction(sendingAccount,receivingAccount,transferAmount);
		spnSendingAccount.setAdapter(accountAdapter);
		spnReceivingAccount.setAdapter(accountsToTransferAdapter);

		spnSendingAccount.setSelection(sendingAccIndex);
		spnReceivingAccount.setSelection(receivingAccIndex);

		ApplicationDB applicationDb = new ApplicationDB(getApplicationContext());

		applicationDb.overwriteAccount(customer,sendingAccount);
		applicationDb.overwriteAccount(receivingCustomer,receivingAccount);

		sessionManager.saveCustomerObjForSession(customer);

		Toast.makeText(getApplicationContext(),"Transfer of $" +
		                                       String.format(Locale.getDefault(),"%.2f",transferAmount) +
		                                       " successfully made",Toast.LENGTH_SHORT).show();
		transferDialog.dismiss();
		drawerLayout.closeDrawers();
		startActivity(new Intent(DashboardActivity.this,DashboardActivity.class));
	}

	private void displayLoanDialog()
	{
		loanDialog = new Dialog(this);
		loanDialog.setContentView(R.layout.loan_dialog);
		loanDialog.setCanceledOnTouchOutside(true);
		loanDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
				Toast.makeText(getApplicationContext(),"Loan Cancelled",Toast.LENGTH_SHORT).show();
			}
		});
//        clerks = sessionManager.getClerksFromSession();
		topSpinner = loanDialog.findViewById(R.id.spn_clerk_list_Loan_dialog);
		ArrayAdapter<Clerk> clerkAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,clerks);
		clerkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		topSpinner.setAdapter(clerkAdapter);
		topSpinner.setSelection(0);

		bottomSpinner = loanDialog.findViewById(R.id.spn_account_dialog);
		accountAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,customer.getAccounts());
		accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bottomSpinner.setAdapter(accountAdapter);
		bottomSpinner.setSelection(0);
		edtLoanAmount = loanDialog.findViewById(R.id.edt_loan_amount);

		btnCancel = loanDialog.findViewById(R.id.btn_cancel_loan_dialog);
		btnSuccess = loanDialog.findViewById(R.id.btn_success_loan_dialog);

		btnCancel.setOnClickListener(loanClickListener);
		btnSuccess.setOnClickListener(loanClickListener);
		loanDialog.show();
	}

	private void makeLoan()
	{
		double loanAmount = 0;
		boolean isNum = false;
		int clerkSelectedIndex = topSpinner.getSelectedItemPosition();
		int accountSelectedIndex = bottomSpinner.getSelectedItemPosition();

		try
		{
			loanAmount = Double.parseDouble(edtLoanAmount.getText().toString());
			isNum = true;
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		if(!isNum)
		{
			Toast.makeText(DashboardActivity.this,"Please enter a valid amount",Toast.LENGTH_SHORT).show();
		}
		else if(loanAmount < LOAN_MIN_LIMIT)
		{
			Toast.makeText(DashboardActivity.this,
					"There is a loan minimum limit of " + LOAN_MIN_LIMIT,Toast.LENGTH_SHORT).show();
		}
		else
		{
			sessionManager.saveCustomerObjForSession(customer);
			Clerk clerk = clerks.get(clerkSelectedIndex);
			Account account = customer.getAccounts().get(accountSelectedIndex);
			clerk.addLoanTransaction(customer.getId(),account,loanAmount);
			ApplicationDB applicationDb = new ApplicationDB(getApplicationContext());
			applicationDb.overwriteAccount(customer,customer.getAccounts().get(accountSelectedIndex));
			applicationDb.saveNewLoan(clerk,customer,
					customer.getAccounts().get(accountSelectedIndex).getTransactions().get(
							customer.getAccounts().get(accountSelectedIndex).getTransactions().size() -
							1));
			Toast.makeText(this,"Loan of $" + String.format(Locale.getDefault(),"%.2f",loanAmount) +
			                    " " + "is pending",Toast.LENGTH_SHORT).show();
			loanDialog.dismiss();
			drawerLayout.closeDrawers();
			startActivity(new Intent(DashboardActivity.this,DashboardActivity.class));
//            manualNavigation(manualNavID.ACCOUNTS_ID, null);
		}
	}

	public ArrayList<Account> getAccountsFromCurrentCustomer(String customerID)
	{
		accountsToTransfer = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Accounts").child(customerID)
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
					accountsToTransfer.add(new Account(
							ds.child("accountName").getValue(String.class),
							ds.child("accountNo").getValue(String.class),
							ds.child("accountBalance").getValue(Double.class)
					));
					accountsToTransfer.get(accountsToTransfer.size() - 1).getTransactions()
							.addAll(getTransactionsForAccount(accountsToTransfer.get(
									accountsToTransfer.size() - 1)));
				}

				//Setting adapter for customers accounts after pulling data from DB
				if(accountsToTransfer != null)
				{
					accountsToTransferAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,accountsToTransfer);
					accountsToTransferAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spnReceivingAccount.setAdapter(accountsToTransferAdapter);
				}
				else
				{
					accountsToTransfer = new ArrayList<>(0);
				}
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

		return accountsToTransfer;
	}

	public void getConversionRate()
	{
		int receivingProfIndex = spnReceivingCustomer.getSelectedItemPosition();
		boolean isNum = false;
		double transferAmount = 0;
		String receivingCurrency = null;
		String sendingCurrency = null;
		if(customer.getCountry().equals(CurrencyConverter.CURRENCIES.USA.toString()))
			sendingCurrency = CurrencyConverter.CURRENCIES.USA.getCurrency();
		else if(customer.getCountry().equals(CurrencyConverter.CURRENCIES.ISRAEL.toString()))
			sendingCurrency = CurrencyConverter.CURRENCIES.ISRAEL.getCurrency();

		if(customersForTransfer.get(receivingProfIndex).getCountry().equals(CurrencyConverter.CURRENCIES.ISRAEL.toString()))
			receivingCurrency = CurrencyConverter.CURRENCIES.ISRAEL.getCurrency();
		else if(customersForTransfer.get(receivingProfIndex).getCountry().equals(CurrencyConverter.CURRENCIES.USA.toString()))
			sendingCurrency = CurrencyConverter.CURRENCIES.USA.getCurrency();

		try
		{
			transferAmount = Double.parseDouble(transfer_amount.getText().toString());
			isNum = true;
		} catch(Exception e)
		{
			Toast.makeText(getApplicationContext(),"Please enter an amount to transfer",Toast.LENGTH_SHORT).show();
		}
		if(isNum)
		{
			if(transferAmount < DEPOSIT_MIN_LIMIT)
			{
				Toast.makeText(getApplicationContext(),"The minimum amount for a transfer is " +
				                                       DEPOSIT_MIN_LIMIT,Toast.LENGTH_SHORT).show();
			}
			else if(transferAmount >
			        customer.getAccounts().get(spnSendingAccount.getSelectedItemPosition()).getAccountBalance())
			{
				Account acc = (Account) spnSendingAccount.getSelectedItem();
				Toast.makeText(getApplicationContext(),"The account," + " " + acc.toString() + " " +
				                                       "does not have sufficient funds to make this transfer",Toast.LENGTH_LONG).show();
			}
			else
			{
				applyConversionRate(sendingCurrency,receivingCurrency,transferAmount);
			}
		}
	}

	public void applyConversionRate(String convertFrom,String convertTo,Double amountToConvert)
	{
		RequestQueue queue = Volley.newRequestQueue(DashboardActivity.this);
		String url = "https://free.currconv.com/api/v7/convert?q=" + convertFrom + "_" + convertTo +
		             "&compact=ultra&apiKey=9de917fc5752ab3a4e57";
		StringRequest stringRequest = new StringRequest(Request.Method.GET,url,response ->
		{
			JSONObject jsonObject;
			try
			{
				jsonObject = new JSONObject(response);
				double conversionRateValue = round(((Double) jsonObject.get(
						convertFrom + "_" + convertTo)),2);
				transferAmount = round((conversionRateValue * amountToConvert),2);
				makeTransfer();
			} catch(JSONException e)
			{
				e.printStackTrace();
			}
		},error ->
		{
			Toast.makeText(DashboardActivity.this,"There was a problem with the conversion",Toast.LENGTH_SHORT).show();
			Log.d("CONVERSION_ERROR",error.toString());
		});
		queue.add(stringRequest);
	}

	public static double round(double value,int places)
	{
		if(places < 0)
			throw new IllegalArgumentException();
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places,RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public void displayHelpDialog()
	{
		helpDialog = new Dialog(this);
		helpDialog.setContentView(R.layout.help_dialog);
		helpDialog.setCanceledOnTouchOutside(true);
		helpDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(DashboardActivity.this, "Help request cancelled", Toast.LENGTH_SHORT).show();
			}
		});
		btnCancel = helpDialog.findViewById(R.id.btn_cancel_help_dialog);
		btnSuccess = helpDialog.findViewById(R.id.btn_send_help_dialog);
		btnCancel.setOnClickListener(helpClickListener);
		btnSuccess.setOnClickListener(helpClickListener);
		helpDialog.show();
	}

	View.OnClickListener helpClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.btn_cancel_help_dialog)
			{
				Toast.makeText(DashboardActivity.this,"Cancelled by user",Toast.LENGTH_SHORT).show();
				helpDialog.dismiss();
			}
			else if(v.getId() == R.id.btn_send_help_dialog)
			{
				sendUserReport();
			}
		}
	};

	private void sendUserReport()
	{
		EditText edtUserReport = helpDialog.findViewById(R.id.edt_complaint_help_dialog);
		Log.i("Send email", "");
		String[] TO = {"daniel4800@gmail.com"};
		String[] CC = {""};
		Intent emailIntent = new Intent(Intent.ACTION_SEND);

		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
		emailIntent.putExtra(Intent.EXTRA_CC, CC);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "User report");
		emailIntent.putExtra(Intent.EXTRA_TEXT, edtUserReport.getText().toString());
		try
		{
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			finish();
			Log.i("Finished sending email", "");
		}
		catch (android.content.ActivityNotFoundException ex)
		{
			Toast.makeText(DashboardActivity.this, "There is no email client installed", Toast.LENGTH_SHORT).show();
		}
	}

	public Customer getCustomer() {return customer;}

	public void setCustomer(Customer customer) {this.customer = customer;}

	public Clerk getClerk() {return clerk;}

	public void setClerk(Clerk clerk) {this.clerk = clerk;}

	public Spinner getAccounts()
	{
		return accounts;
	}

	public void setAccounts(Spinner accounts)
	{
		this.accounts = accounts;
	}

	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}
}