package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CriminalListragment";
    private RecyclerView crimeRecyclerView;
    private CrimeAdapter crimeAdapter;

    private int itemHasChanged;

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container,false);
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        crimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    private void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if(crimeAdapter == null){
            crimeAdapter = new CrimeAdapter(crimes);
            crimeRecyclerView.setAdapter(crimeAdapter);
        } else {
//            crimeAdapter.notifyDataSetChanged();
            crimeAdapter.notifyItemChanged(itemHasChanged);
        }

    }

    public class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Crime crime;
        private TextView titleTextView;
        private TextView dateTextView;
        private ImageView solvedImageView;
        private SimpleDateFormat simpleDateFormat;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            super(inflater.inflate(viewType,parent,false));
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            solvedImageView = itemView.findViewById(R.id.crime_solved);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime crime){
            this.crime = crime;
            titleTextView.setText(crime.getTitle());
            dateTextView.setText(changeDate(crime.getDate()));
            solvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }


        private String changeDate(Date date){
            simpleDateFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy");
            return simpleDateFormat.format(date);
        }

        @Override
        public void onClick(View v) {
            itemHasChanged = getAdapterPosition();
            startActivity(CrimeActivity.newIntent(getActivity(),crime.getId()));
        }
    }

    public class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> crimes;

        public CrimeAdapter(List<Crime> crimes){
            this.crimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater,parent, viewType);
        }

        @Override
        public void onBindViewHolder( CrimeHolder holder, int position) {
            final Crime crime = crimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return crimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(crimes.get(position).isRequiresPolice()){
                return R.layout.list_item_crime_police;
            } else {
                return R.layout.list_item_crime;
            }
        }
    }
}