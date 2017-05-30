package com.underdog.hydrate.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;
import com.underdog.hydrate.util.Log;

import java.util.HashMap;

public class SummaryArrayAdapter extends ArrayAdapter<Object> {

	private Object[] values;
	private String defaultUnit;
	private Context context;

	public SummaryArrayAdapter(Context context, int resource, Object[] values,
			String defaultUnit) {
		super(context, resource, values);
		this.values = values;
		this.defaultUnit = defaultUnit;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView;
		TextView textView;
		HashMap<String, String> rowValues;
		double finalQuantity;
		double regularity;

		rowValues = (HashMap<String, String>) values[position];
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rowView = inflater.inflate(R.layout.summary_list_view, parent, false);

		Log.d(this.getClass().toString(),
				rowValues.get(Constants.SUMMARY_HEADING));
		textView = (TextView) rowView.findViewById(R.id.last);
		textView.setText(rowValues.get(Constants.SUMMARY_HEADING));

		textView = (TextView) rowView.findViewById(R.id.averageCupCount);
		textView.setText(rowValues.get(Constants.SUMMARY_CUPS));

		textView = (TextView) rowView.findViewById(R.id.averageQuantityUnits);

		finalQuantity = Double.valueOf(rowValues
				.get(Constants.SUMMARY_QUANTITY));
		if (defaultUnit
				.equalsIgnoreCase(context.getString(R.string.milliliter))) {
			textView.setText(context.getString(R.string.l));

			finalQuantity /= 10;
			finalQuantity = Math.round(finalQuantity);
			finalQuantity /= 100;

		} else if (defaultUnit.equalsIgnoreCase(context
				.getString(R.string.us_oz))) {
			textView.setText(context.getString(R.string.oz));

			finalQuantity *= 100;
			finalQuantity = Math.round(finalQuantity);
			finalQuantity /= 100;
		}

		textView = (TextView) rowView
				.findViewById(R.id.averageQuantityConsumed);
		textView.setText(String.valueOf(finalQuantity));

		textView = (TextView) rowView.findViewById(R.id.regulaityPercentage);
		regularity = Double.valueOf(rowValues.get(Constants.REGULARITY));
		regularity *= 10;
		regularity = Math.round(regularity);
		regularity /= 10;
		textView.setText(String.valueOf((int) regularity));
		if (regularity < 70) {
			textView.setTextColor(ContextCompat.getColor(context, R.color.danger));
		} else if (regularity < 100) {
			textView.setTextColor(ContextCompat.getColor(context, R.color.safe));
		}

		return rowView;
	}
}
