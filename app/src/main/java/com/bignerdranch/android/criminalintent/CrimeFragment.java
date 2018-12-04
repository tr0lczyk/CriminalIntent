package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.criminalintent.utils.PictureUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.bignerdranch.android.criminalintent.DatePickerFragment.EXTRA_DATE;
import static com.bignerdranch.android.criminalintent.TimePickerFragment.EXTRA_TIME;

public class CrimeFragment extends Fragment {

    private static final String ARG_KEY = "arg_key";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;
    public static final int REQUEST_CONTACT = 2;
    public static final int REQUEST_PHOTO = 3;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 4;
    private Crime crime;
    private Callbacks callbacks;

    EditText titleField;
    Button dateButton;
    CheckBox solvedCheckBox;
    Button timeButton;
    Button crimeReport;
    Button suspectButton;
    Button callButton;
    ImageView photoView;
    ImageButton photoButton;
    private File photoFile;
    private boolean isTherePhotoPath = false;
    private int lstKnownHeight = 0;

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(crime);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_KEY);
        crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        photoFile = CrimeLab.get(getActivity()).getPhotoFile(crime);
        setHasOptionsMenu(true);
    }

    public static CrimeFragment newInstance(UUID criminalId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY, criminalId);
        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        titleField = v.findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
                updateCrime();
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
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
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
                fragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                fragment.show(fragmentManager, DIALOG_TIME);
            }
        });
        solvedCheckBox = v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                crime.setSolved(isChecked);
                updateCrime();
            }
        });
        crimeReport = v.findViewById(R.id.crime_report);

        crimeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setSubject(getString(R.string.send_report))
                        .setText(getCrimeReport())
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();

//                Intent intent = new Intent(Intent.ACTION_SEND);
////                intent.setType("text/plain");
////                intent.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
////                intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.crime_report_subject));
////                intent = Intent.createChooser(intent,getString(R.string.send_report));
////                startActivity(intent);
            }
        });
        final Intent pickContact = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        suspectButton = v.findViewById(R.id.crime_suspect);
        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (crime.getSuspect() != null) {
            suspectButton.setText(crime.getSuspect());
        }
        PackageManager manager = getActivity().getPackageManager();
        if (manager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            suspectButton.setEnabled(false);
        }
        callButton = v.findViewById(R.id.callButton);
        if (crime.getSuspect() == null) {
            callButton.setEnabled(false);
            callButton.setText(getString(R.string.call, ""));
        } else {
            callButton.setText(getString(R.string.call, crime.getSuspect()));
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (crime.getSuspectNumber() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + crime.getSuspectNumber()));
                    startActivity(intent);
                }
            }
        });

        photoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = photoFile != null &&
                captureImage.resolveActivity(manager) != null;
        photoButton.setEnabled(canTakePhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri photoUri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });
        photoView = v.findViewById(R.id.crime_photo);
        photoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
                photoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
//        updatePhotoView();
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photoFile == null || !photoFile.exists()) {
                    Toast.makeText(getActivity(), "Gallery is empty", Toast.LENGTH_SHORT).show();
                } else {
                    FragmentManager fragmentManager = getFragmentManager();
                    CrimeGalleryFragment galleryFragment = CrimeGalleryFragment.newGallery(photoFile.getPath());
                    galleryFragment.show(fragmentManager,"Gallery");
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(EXTRA_DATE);
            crime.setDate(date);
            updateCrime();
            dateButtonSetter(crime.getDate());
        }
        if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(EXTRA_TIME);
            crime.setDate(date);
            timeButtonSetter(crime.getDate());
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    Contacts.DISPLAY_NAME,
                    Contacts._ID};
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            String suspectId;
            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                suspectId = c.getString(c.getColumnIndex(Contacts._ID));
                crime.setSuspect(suspect);
                updateCrime();
                callButton.setEnabled(true);
                callButton.setText(getString(R.string.call, crime.getSuspect()));
                suspectButton.setText(suspect);
            } finally {
                c.close();
            }

            if(ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_DENIED){
                contactUri = CommonDataKinds.Phone.CONTENT_URI;
                queryFields = new String[]{CommonDataKinds.Phone.NUMBER};

                c = getActivity().getContentResolver()
                        .query(contactUri,
                                queryFields,
                                CommonDataKinds.Phone._ID + "= ? ",
                                new String[]{suspectId},
                                null);
                try{
                    if(c.getCount() == 0){
                        return;
                    }
                    c.moveToFirst();
                    String number = c.getString(c.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                    crime.setSuspectNumber(number);
                } finally{
                    c.close();
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSIONS_REQUEST);
            }
        }
        if(requestCode == REQUEST_PHOTO){
            Uri photoUri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile);
            getActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
            updatePhotoView();
        }
    }

    private void dateButtonSetter(Date date) {
        dateButton.setText(date.toString());
    }

    private void timeButtonSetter(Date date) {
        timeButton.setText(changeDateToTime(date));
    }

    private String changeDateToTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(getActivity().findViewById(R.id.detail_fragment_container) == null){
            inflater.inflate(R.menu.fragment_crime_delete, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.remove_crime):
                CrimeLab.get(getActivity()).deleteCrime(crime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
//        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
//        String dateString = dateFormatter.format(crime.getDate());
        String dateString = android.text.format.DateFormat
                .format(dateFormat, crime.getDate()).toString();

        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, crime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView(){
        if(photoFile == null || !photoFile.exists()) {
            photoView.setImageDrawable(null);
        } else {
            int width = photoView.getWidth();
            int height = photoView.getHeight();
            if(lstKnownHeight != height){
                Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(),width,height);
                isTherePhotoPath = true;
                photoView.setImageBitmap(bitmap);
            }
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(crime);
        callbacks.onCrimeUpdated(crime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }
}
