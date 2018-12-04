package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.security.auth.callback.Callback;

import static com.bignerdranch.android.criminalintent.CrimeListActivity.FRAGMENT_TAG;

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CriminalListFragment";
    public static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private RecyclerView crimeRecyclerView;
    private CrimeAdapter crimeAdapter;
    private boolean subtitleVisible;
    private Button crimeDetected;
    private TextView noCrimeDetected;
    private int itemHasChanged;
    private Callbacks callbacks;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        crimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        crimeDetected = view.findViewById(R.id.btCrimeDetetcted);
        noCrimeDetected = view.findViewById(R.id.no_crimes_warning);

        if (savedInstanceState != null) {
            subtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        updateUI();
        return view;
    }


    public void updateUI() {
        final CrimeLab crimeLab = CrimeLab.get(getActivity());
        final List<Crime> crimes = crimeLab.getCrimes();
        if (!crimes.isEmpty()) {
            crimeDetected.setVisibility(View.GONE);
            noCrimeDetected.setVisibility(View.GONE);
        }
        crimeDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewCrimeIntent();
            }
        });

        if (crimeAdapter == null) {
            crimeAdapter = new CrimeAdapter(crimes);
            crimeRecyclerView.setAdapter(crimeAdapter);
        } else {
            crimeAdapter.setCrimes(crimes);
//            crimeAdapter.notifyItemRemoved(itemHasChanged);
            crimeAdapter.notifyDataSetChanged();
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                crimeAdapter.removeItem(pos);
                crimeAdapter.removeCrime(pos);
                crimeAdapter.notifyItemRemoved(pos);
                if (pos != 0 && (getActivity().findViewById(R.id.detail_fragment_container) != null)) {
                    callbacks.onCrimeSelected(crimeAdapter.crimes.get(pos - 1));
                } else if (pos == 0 && !crimeAdapter.crimes.isEmpty() &&
                        (getActivity().findViewById(R.id.detail_fragment_container) != null)) {
                    callbacks.onCrimeSelected(crimeAdapter.crimes.get(pos));
                } else if(pos == 0 && crimeAdapter.crimes.isEmpty() &&
                        (getActivity().findViewById(R.id.detail_fragment_container) != null)){
                    crimeDetected.setVisibility(View.VISIBLE);
                    noCrimeDetected.setVisibility(View.VISIBLE);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .remove(getActivity().getSupportFragmentManager()
                                    .findFragmentById(FRAGMENT_TAG)).commit();
                } else if(pos == 0 && crimeAdapter.crimes.isEmpty()) {
                    crimeDetected.setVisibility(View.VISIBLE);
                    noCrimeDetected.setVisibility(View.VISIBLE);
                }
            }
        }).attachToRecyclerView(crimeRecyclerView);
        updateSubtitle();
    }

    public class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Crime crime;
        private TextView titleTextView;
        private TextView dateTextView;
        private ImageView solvedImageView;
        private SimpleDateFormat simpleDateFormat;
        private TextView timeTextView;


        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            super(inflater.inflate(viewType, parent, false));
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            solvedImageView = itemView.findViewById(R.id.crime_solved);
            timeTextView = itemView.findViewById(R.id.crime_time);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime crime) {
            this.crime = crime;
            titleTextView.setText(crime.getTitle());
            dateTextView.setText(changeDate(crime.getDate()));
            timeTextView.setText(changeDateToTime(crime.getDate()));
            solvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }


        private String changeDate(Date date) {
            simpleDateFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy");
            return simpleDateFormat.format(date);
        }

        private String changeDateToTime(Date date) {
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            return simpleDateFormat.format(date);
        }

        @Override
        public void onClick(View v) {
            itemHasChanged = getAdapterPosition();
            callbacks.onCrimeSelected(crime);
        }
    }

    public class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> crimes;

        public CrimeAdapter(List<Crime> crimes) {
            this.crimes = crimes;
        }

        public void removeCrime(int pos) {
            crimes.remove(pos);
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            return new CrimeHolder(layoutInflater, parent, viewType);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            final Crime crime = crimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return crimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            this.crimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if (crimes.get(position).isRequiresPolice()) {
                return R.layout.list_item_crime_police;
            } else {
                return R.layout.list_item_crime;
            }
        }

        public void removeItem(int position) {
            Crime crime = crimes.get(position);
            CrimeLab.get(getActivity()).deleteCrime(crime);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (subtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                openNewCrimeIntent();
                return true;
            case R.id.show_subtitle:
                subtitleVisible = !subtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {

        CrimeLab crimeLab = CrimeLab.get(getContext());
        int crimeSize = crimeLab.getCrimes().size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, crimeSize, crimeSize);

        if (!subtitleVisible) {
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, subtitleVisible);
    }

    public void openNewCrimeIntent() {
        Crime crime = new Crime();
        CrimeLab.get(getContext()).addCrime(crime);
//        updateUI();
        callbacks.onCrimeSelected(crime);
        if(getActivity().findViewById(R.id.detail_fragment_container) != null){
            updateUI();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }
}
