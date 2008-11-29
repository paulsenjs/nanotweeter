package net.jjc1138.android.twitter;

import java.text.ChoiceFormat;
import java.text.MessageFormat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class TwitterConfig extends Activity {
	private final int[] INTERVALS = { 1, 3, 5, 10, 15, 30, 60 };

	private Spinner check_interval;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Set up the interval choices:
		final String[] intervalChoiceText = new String[INTERVALS.length];
		for (int i = 0; i < INTERVALS.length; ++i) {
			int option = INTERVALS[i];
			intervalChoiceText[i] = MessageFormat.format(
				new ChoiceFormat(getString(R.string.check_interval_option))
					.format(option), option);
		}
		ArrayAdapter<String> a = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, intervalChoiceText);
		a.setDropDownViewResource(
			android.R.layout.simple_spinner_dropdown_item);
		check_interval = (Spinner) findViewById(R.id.check_interval);
		check_interval.setAdapter(a);
		check_interval.setSelection(5);
	}
}
