package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab crimeLab;
    private List<Crime> crimes;

    public static CrimeLab get(Context context){
        if(crimeLab == null){
            crimeLab = new CrimeLab(context);
        }
        return crimeLab;
    }

    private CrimeLab(Context context){
        crimes = new ArrayList<>();
    }

    public List<Crime> getCrimes(){
        return crimes;
    }

    public Crime getCrime(UUID id){
        for(Crime crime : crimes){
            if(crime.getId().equals(id)){
                return crime;
            }
        }
        return null;
    }
    public void addCrime(Crime c){
        crimes.add(c);
    }

    public void deleteCrime(UUID id){
        for(int i = 0;i < crimes.size(); i++){
            if(crimes.get(i).getId().equals(id)){
                crimes.remove(i);
            }
        }
    }
}
