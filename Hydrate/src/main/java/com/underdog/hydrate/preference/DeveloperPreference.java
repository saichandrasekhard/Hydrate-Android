package com.underdog.hydrate.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.underdog.hydrate.database.HydrateDAO;

/**
 * Created by chandrasekhar.dandu on 6/1/2017.
 */

public class DeveloperPreference extends Preference {

    HydrateDAO hydrateDAO;

    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onClick() {
        super.onClick();

//        hydrateDAO.
    }
}
