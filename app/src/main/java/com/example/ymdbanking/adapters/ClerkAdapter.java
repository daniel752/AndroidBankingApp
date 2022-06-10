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
import com.example.ymdbanking.model.Clerk;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ClerkAdapter extends ArrayAdapter<Clerk>
{
	private Context context;
	private int resource;

	public ClerkAdapter(Context context, int resource, ArrayList<Clerk> clerks)
	{
		super(context,resource,clerks);
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
		}

		Clerk clerk = getItem(position);

		TextView txtClerkName = convertView.findViewById(R.id.txt_profile_name);
		txtClerkName.setText(clerk.getFullName());

		TextView txtClerkUsername = convertView.findViewById(R.id.txt_profile_username);
		txtClerkUsername.setText(clerk.getUsername());

		return convertView;
	}
}
