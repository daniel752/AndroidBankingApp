package com.example.ymdbanking.api;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverter extends AppCompatActivity
{
	public enum CURRENCIES
	{
		ISRAEL("ILS"),
		USA("USD"),
		AUSTRIA("EUR");

		String currency;

		CURRENCIES(String s)
		{
			currency = s;
		}

		public String getCurrency() {return currency;}
	}

	private double conversionValue;
	private Context context;

	public CurrencyConverter(Context context)
	{
		this.context = context;
	}
}
