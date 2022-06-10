package com.example.ymdbanking.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.example.ymdbanking.R;
import com.example.ymdbanking.model.Customer;

import java.util.ArrayList;

/**
 * Adapter for displaying profiles
 */
public class ProfileAdapter extends ArrayAdapter<Customer>
{
	private Context context;
	private int resource;

	public ProfileAdapter(Context context, int resource, ArrayList<Customer> customers)
	{
		super(context,resource, customers);
		this.context = context;
		this.resource = resource;
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent)
	{
		if (convertView == null)
		{

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(resource, parent, false);
		}

		Customer customer = getItem(position);

//		if(LoginFragment.getUserRole() == User.USER_TYPE.CLERK.getValue())
//		{
//			TextView txtUserName = convertView.findViewById(R.id.txt_profile_username);
//			txtUserName.setText(profile.getUsername());
//		}
//		else if(LoginFragment.getUserRole() == User.USER_TYPE.ADMIN.getValue())
//		{
//			TextView txtAccountName = convertView.findViewById(R.id.txt_profile_name);
//			txtAccountName.setText(profile.getFirstName() + " " + profile.getLastName());
//			TextView txtUserName = convertView.findViewById(R.id.txt_profile_username);
//			txtUserName.setText(profile.getUsername());
//		}
		TextView txtAccountName = convertView.findViewById(R.id.txt_profile_name);
		txtAccountName.setText(customer.getFullName());
		TextView txtUserName = convertView.findViewById(R.id.txt_profile_username);
		txtUserName.setText(customer.getUsername());
		return convertView;
	}
}
