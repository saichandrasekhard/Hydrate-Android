package com.underdog.hydrate.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.underdog.hydrate.R;
import com.underdog.hydrate.constants.Constants;

import java.util.ArrayList;

public class DaySelectionArrayAdapter extends ArrayAdapter<Object> {

	private Bundle args;
	private ArrayList<Integer> daysSelected;

	public DaySelectionArrayAdapter(Context context, int resource,
			Object[] values, Bundle bundle) {
		super(context, resource, values);
		args = bundle;
		daysSelected = args.getIntegerArrayList(Constants.DAYS_SELECTED);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = ((Activity) getContext()).getLayoutInflater().inflate(
				R.layout.day_check, parent, false);
		TextView dayView = (TextView) rowView.findViewById(R.id.day_name);
		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.selected_day);
		if (daysSelected.contains(position)) {
			checkBox.setChecked(true);
		}
		switch (position) {
		case 0:
			dayView.setText(R.string.monday);
			break;
		case 1:
			dayView.setText(R.string.tuesday);
			break;
		case 2:
			dayView.setText(R.string.wednesday);
			break;
		case 3:
			dayView.setText(R.string.thursday);
			break;
		case 4:
			dayView.setText(R.string.friday);
			break;
		case 5:
			dayView.setText(R.string.saturday);
			break;
		case 6:
			dayView.setText(R.string.sunday);
			break;

		default:
			break;
		}

		return rowView;
	}
}
