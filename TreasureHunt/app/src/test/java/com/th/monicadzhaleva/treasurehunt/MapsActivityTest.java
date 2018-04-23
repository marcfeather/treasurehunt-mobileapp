package com.th.monicadzhaleva.treasurehunt;

import android.content.Context;
import android.test.mock.MockContext;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.th.monicadzhaleva.treasurehunt.Activities.LoginActivity;
import com.th.monicadzhaleva.treasurehunt.Activities.MapsActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by monicadzhaleva on 11/04/2018.
 */

public class MapsActivityTest {

    LoginActivity activity;
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
        activity = new LoginActivity();
    }

    @Test
    public void checkIfUsernameAndPasswordArePresent() throws Exception {
       /* View username = activity.findViewById(R.id.username);
        View password = activity.findViewById(R.id.password);

      assertThat(username,notNullValue());
        assertThat(username, instanceOf(AutoCompleteTextView.class));
        assertThat(password,notNullValue());
        assertThat(password, instanceOf(AutoCompleteTextView.class));*/
    }
}