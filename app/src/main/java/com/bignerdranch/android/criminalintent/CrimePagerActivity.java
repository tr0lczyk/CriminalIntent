package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "id";

    private ViewPager viewPager;
    private List<Crime> crimes;

    private Button buttonFirst;
    private Button buttonLast;

    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID,crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CRIME_ID);

        viewPager = findViewById(R.id.crime_view_pager);

        buttonFirst = findViewById(R.id.buttonFirst);
        buttonLast = findViewById(R.id.buttonLast);

        crimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = crimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return crimes.size();
            }
        });

        for(int i = 0; i < crimes.size(); i++){
            if(crimes.get(i).getId().equals(crimeId)){
                viewPager.setCurrentItem(i);
                break;
            }
        }
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(viewPager.getCurrentItem() == 0)){
                    viewPager.setCurrentItem(0);
                } else {
                    Toast.makeText(CrimePagerActivity.this,"You are already there!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(viewPager.getCurrentItem() == crimes.size()-1)){
                    viewPager.setCurrentItem(crimes.size()-1);
                } else {
                    Toast.makeText(CrimePagerActivity.this,"You are already there!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
