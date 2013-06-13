package com.robotest.test;

import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import com.robotest.simpleapp.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class RoboTest {
    private Activity activity;

    @Before
    public void setUp() throws Exception {
        activity = new MainActivity();
    }

    @Test
    public void shouldUpdateResultsWhenButtonIsClicked() throws Exception {
        TextView view = (TextView) activity.findViewById(com.robotest.simpleapp.R.id.hello_world);
        String resultsText = view.getText().toString();
        assertThat(resultsText, equalTo("Hello World!"));
    }


}
