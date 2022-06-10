package com.example.ymdbanking;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ymdbanking.model.Account;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Message;
import com.example.ymdbanking.model.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionManager
{
    //Variables
    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    //Session names
    public static final String USER_SESSION = "userLoginSession";
    public static final String REMEMBER_ME_SESSION = "rememberMe";
    public static final String CLERK_SESSION = "ClerkLoginSession";

    public static final String SESSION_OBJ = "sessionObj";
    public static final String CUSTOMER_OBJ = "customerObj";
    private static final String SESSION_ACCOUNTS = "accountsObj";

    //User session variables
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_FULLNAME = "fullName";
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_TYPE_ID = "typeID";

    //Remember Me variables
    private static final String IS_REMEMBER_ME = "IsRememberMe";
    public static final String KEY_SESSION_ID = "id";
    public static final String KEY_SESSION_EMAIL = "email";
    public static final String KEY_SESSION_PASSWORD = "password";

    //Clerk session variables
    private static final String IS_CLERK = "IsClerk";
    public static final String KEY_CLERK_USERNAME = "username";
    public static final String KEY_CLERK_EMAIL = "email";
    public static final String KEY_CLERK_PASSWORD = "password";
    public static final String KEY_CLERK_PHONE = "phone";
    private static final String LIST_CLERKS = "clerkList";

    private static final String LIST_MESSAGES = "messages";


    public SessionManager(Context _context,String sessionName)
    {
        context = _context;
        userSession = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = userSession.edit();
    }
    /*
    Users -> Login Session
    */
    public void createLoginSession(String fullname, String id, String username, String email, String password, String phone, String typeID) {
        editor.putBoolean(IS_LOGIN, true);

        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_TYPE_ID,typeID);
        editor.commit();
    }

    public HashMap<String, String> getUserDetailFromSession()
    {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_FULLNAME, userSession.getString(KEY_FULLNAME, null));
        userData.put(KEY_ID, userSession.getString(KEY_ID, null));
        userData.put(KEY_USERNAME, userSession.getString(KEY_USERNAME, null));
        userData.put(KEY_EMAIL, userSession.getString(KEY_EMAIL, null));
        userData.put(KEY_PASSWORD, userSession.getString(KEY_PASSWORD, null));
        userData.put(KEY_PHONE, userSession.getString(KEY_PHONE, null));
        userData.put(KEY_TYPE_ID,userSession.getString(KEY_TYPE_ID,null));

        return userData;
    }

    public boolean checkLogin() {
        return userSession.getBoolean(IS_LOGIN, true);

    }

    public void logoutUserFromSession(){
        editor.clear();
        editor.commit();
    }

    /*
    RememberMe -> Session Functions
    */

    public void createRememberMeSession(String email, String id, String password) {

        editor.putBoolean(IS_REMEMBER_ME, true);

        editor.putString(KEY_SESSION_EMAIL, email);
        editor.putString(KEY_SESSION_ID, id);
        editor.putString(KEY_SESSION_PASSWORD, password);

        editor.commit();
    }
    public HashMap<String, String> getRememberMeDetailFromSession() {

        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_SESSION_EMAIL, userSession.getString(KEY_SESSION_EMAIL, null));
        userData.put(KEY_SESSION_ID, userSession.getString(KEY_SESSION_ID, null));
        userData.put(KEY_SESSION_PASSWORD, userSession.getString(KEY_SESSION_PASSWORD, null));

        return userData;
    }
    public boolean checkRememberMeLogin() {
        return userSession.getBoolean(IS_REMEMBER_ME, true);

    }

    public Customer getCustomerObjFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(SESSION_OBJ,null);
        return gson.fromJson(json, Customer.class);
    }

    public void saveCustomerObjForSession(Customer customer)
    {
        Gson gson = new Gson();
        String json = gson.toJson(customer);
        editor.putString(SESSION_OBJ,json);
        editor.commit();
    }

    public void saveAccountsObjForSession(ArrayList<Account> accounts)
    {
        Gson gson = new Gson();
        String json = gson.toJson(accounts);
        editor.putString(SESSION_ACCOUNTS,json);
        editor.commit();
    }

    public ArrayList<Account> getAccountsObjFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(SESSION_ACCOUNTS,null);
        return gson.fromJson(json,ArrayList.class);
    }

    public Clerk getClerkObjFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(SESSION_OBJ,null);
        return gson.fromJson(json, Clerk.class);
    }

    public void saveClerkObjForSession(Clerk clerk)
    {
        Gson gson = new Gson();
        String json = gson.toJson(clerk);
        editor.putString(SESSION_OBJ,json);
        editor.commit();
    }

    public Admin getAdminObjFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(SESSION_OBJ,null);
        return gson.fromJson(json, Admin.class);
    }

    public void saveAdminObjForSession(Admin admin)
    {
        Gson gson = new Gson();
        String json = gson.toJson(admin);
        editor.putString(SESSION_OBJ,json);
        editor.commit();
    }

    public void saveClerksForSession(ArrayList<Clerk> clerks)
    {
        Gson gson = new Gson();
        String json = gson.toJson(clerks);
        editor.putString(LIST_CLERKS,json);
        editor.commit();
    }

    public ArrayList<Clerk> getClerksFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(LIST_CLERKS,null);
        return gson.fromJson(json, ArrayList.class);
    }

    public void saveMessagesForSession(ArrayList<Message> messages)
    {
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        editor.putString(LIST_MESSAGES,json);
        editor.commit();
    }

    public ArrayList<Message> getMessagesFromSession()
    {
        Gson gson = new Gson();
        String json = userSession.getString(LIST_MESSAGES,null);
        return gson.fromJson(json,ArrayList.class);
    }
}