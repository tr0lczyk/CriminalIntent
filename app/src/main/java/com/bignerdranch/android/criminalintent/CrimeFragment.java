package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.bignerdranch.android.criminalintent.DatePickerFragment.EXTRA_DATE;
import static com.bignerdranch.android.criminalintent.TimePickerFragment.EXTRA_TIME;

public class CrimeFragment extends Fragment {


    private static final String ARG_KEY = "arg_key";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;
    private Crime crime;
    EditText titleField;
    Button dateButton;
    CheckBox solvedCheckBox;
    Button timeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_KEY);
        crime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    public static CrimeFragment newInstance(UUID criminalId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY,criminalId);
        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,container,false);
        titleField = v.findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dateButton = v.findViewById(R.id.crime_date);
        dateButtonSetter(crime.getDate());
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(crime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });
        timeButton = v.findViewById(R.id.timeButton);
        timeButtonSetter(crime.getDate());
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                TimePickerFragment fragment = TimePickerFragment.newInstance(crime.getDate());
                fragment.setTargetFragment(CrimeFragment.this,REQUEST_TIME);
                fragment.show(fragmentManager, DIALOG_TIME);
            }
        });
        solvedCheckBox = v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                crime.setSolved(isChecked);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(EXTRA_DATE);
            crime.setDate(date);
            dateButtonSetter(crime.getDate());
        }
        if(requestCode == REQUEST_TIME){
            Date date = (Date) data.getSerializableExtra(EXTRA_TIME);
            crime.setDate(date);
            timeButtonSetter(crime.getDate());
        }
    }

    private void dateButtonSetter(Date date) {
        dateButton.setText(date.toString());
    }

    private void timeButtonSetter(Date date) {
        timeButton.setText(changeDateToTime(date));
    }

    private String changeDateToTime(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}
