//package com.example.ymdbanking;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.replaceText;
//import static androidx.test.espresso.action.ViewActions.scrollTo;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//
//import android.view.View;
//
//import androidx.test.espresso.action.ViewActions;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
//import com.example.ymdbanking.model.Customer;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//
//public class RegistrationInstrumentedTest
//{
//	private Customer customer;
//	private String email,fullName,id,password,phone,username,country;
//
//	@Rule
//	public ActivityScenarioRule<SignUpActivity> activityScenarioRule = new ActivityScenarioRule<SignUpActivity>(SignUpActivity.class);
//
//
//	@Before
//	public void setUp()
//	{
//		//SetUp for customer's registration and login
//		email = "test@gmail.com";
//		fullName = "test test";
//		id = "000000000";
//		password = "123456";
//		phone = "1234567890";
//		username = "testing";
//		country = "ISRAEL";
//		customer = new Customer(email,fullName,id,password,phone,username,country);
//	}
//
//	@Test
//	public void checkIfUserIsInDbAfterSignup()
//	{
//		onView(withId(R.id.edt_fullname_signup)).perform(typeText(customer.getFullName()));
//		onView(withId(R.id.edt_username_signup)).perform(typeText(customer.getUsername()));
//		onView(withId(R.id.edt_email_signup)).perform(typeText(customer.getEmail()));
//		ViewActions.closeSoftKeyboard();
//		onView(withId(R.id.edt_password_signup)).perform(typeText(customer.getPassword()));
//		ViewActions.closeSoftKeyboard();
//		onView(withId(R.id.edt_confirm_pass_signup)).perform(scrollTo());
//		onView(withId(R.id.edt_confirm_pass_signup)).perform(typeText(customer.getPassword()));
//		ViewActions.closeSoftKeyboard();
//		onView(withId(R.id.edt_phone_signup)).perform(scrollTo());
//		onView(withId(R.id.edt_phone_signup)).perform(typeText(customer.getPhone()));
//		onView(withId(R.id.edt_country_signup)).perform(scrollTo());
//		onView(withId(R.id.edt_country_signup)).perform(typeText(customer.getCountry()));
//		onView(withId(R.id.edt_id_signup)).perform(scrollTo());
//		onView(withId(R.id.edt_id_signup)).perform(typeText(customer.getId()));
//		ViewActions.closeSoftKeyboard();
//		onView(withId(R.id.signup_btn)).perform(click());
//	}
//}
