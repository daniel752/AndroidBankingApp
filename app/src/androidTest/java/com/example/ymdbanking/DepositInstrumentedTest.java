//package com.example.ymdbanking;
//
//import static androidx.test.espresso.Espresso.closeSoftKeyboard;
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.clearText;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
//import static org.junit.Assert.assertEquals;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.Spinner;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.espresso.action.ViewActions;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import com.example.ymdbanking.model.Account;
//import com.example.ymdbanking.model.Customer;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//
//public class DepositInstrumentedTest
//{
//	private DashboardActivity dashboardActivity;
//	private Dialog dlgDeposit;
//	private EditText edtAmount;
//	private ArrayAdapter<Account> accountAdapter;
//	private ArrayAdapter<String> depositMethodAdapter;
//	private Spinner spnAccounts,spnDepositMethod;
//	private final String[] depositMethod = {"Cash","Credit"};
//	private Customer customer;
//
//	@Rule
//	public ActivityScenarioRule<LoginActivity> activityScenarioRule = new ActivityScenarioRule<LoginActivity>(LoginActivity.class);
//
//	@Before
//	public void setUp() throws Exception
//	{
////		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
////		context.startActivity(new Intent(context,DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
////		dashboardActivity = new DashboardActivity();
////		customer = new Customer("daniel@gmail.com","Daniel Arbiv","123456789","123456",
////				"12345678","daniel","USA");
////		customer.addAccount("Daniel-1",1000);
////		dashboardActivity.setCustomer(customer);
////		DashboardActivity.setCustomer(customer);
////		DashboardActivity.setCustomer(customer);
//
////		dashboardActivity.setDepositDialog(new Dialog(context));
////		dlgDeposit = dashboardActivity.getDepositDialog();
////		dlgDeposit.setContentView(R.layout.deposit_dialog);
////		accountAdapter = dashboardActivity.getAccountAdapter();
////		accountAdapter = new AccountAdapter(context,R.layout.lst_accounts,customer.getAccounts());
////		depositMethodAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,depositMethod);
////		spnAccounts = dlgDeposit.findViewById(R.id.spn_accounts_deposit_dialog);
////		spnDepositMethod = dlgDeposit.findViewById(R.id.spn_method_deposit_dialog);
////		spnAccounts.setAdapter(accountAdapter);
////		spnDepositMethod.setAdapter(depositMethodAdapter);
////		//Choose first account
////		spnAccounts.setSelection(0);
////		//Choose deposit method 'Credit'
////		spnDepositMethod.setSelection(1);
////		dlgDeposit.show();
//
//		//Login
//		onView(withId(R.id.login_email)).perform(clearText());
//		onView(withId(R.id.login_id)).perform(clearText());
//		onView(withId(R.id.login_pass)).perform(clearText());
//
//		onView(withId(R.id.login_email)).perform(typeText("daniel@gmail.com"));
//		onView(withId(R.id.login_id)).perform(typeText("123456789"));
//		onView(withId(R.id.login_pass)).perform(typeText("123456"));
//		onView(withId(R.id.login_pass)).perform(ViewActions.closeSoftKeyboard());
//		onView(withId(R.id.login_btn)).perform(click());
//
//		//Dashboard
//		onView(withId(R.id.menu_icon)).perform(click());
////		onView(withId(R.id.nav_transaction)).perform(click());
//		onView(withId(R.id.nav_deposit)).perform(click());
//		onView(withId(R.id.edt_deposit_amount)).perform(typeText("1000"));
//		onView(withId(R.id.btn_deposit)).perform(click());
//	}
//
//	@Test
//	public void validateMoneyGetsToAccountAfterDeposit()
//	{
//		double expectedAmount = 2000;
//		double actualAmount = customer.getAccounts().get(customer.getAccounts().size() - 1).getAccountBalance();
//		assertEquals(expectedAmount,actualAmount,0.0001);
//	}
//}