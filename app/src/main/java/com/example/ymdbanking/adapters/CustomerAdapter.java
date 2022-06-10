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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomerAdapter extends ArrayAdapter<Customer>
{
	private Context context;
	private int resource;

	public CustomerAdapter(Context context, int resource, ArrayList<Customer> customers)
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
			convertView = LayoutInflater.from(context).inflate(resource,parent,false);
//			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//			convertView = inflater.inflate(resource, parent, false);
		}
		Customer customer = getItem(position);
		TextView txtFullName = convertView.findViewById(R.id.txt_profile_name);
		txtFullName.setText(customer.getFullName());
		TextView txtUserName = convertView.findViewById(R.id.txt_profile_username);
		txtUserName.setText(customer.getUsername());
		return convertView;
	}
}
