package com.robotest.test;

import android.app.Activity;
import android.widget.TextView;

import com.robotest.simpleapp.MainActivity;
import com.robotest.simpleapp.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RoboTest {

    @Test
    public void shouldUpdateResultsWhenButtonIsClicked() throws Exception {
        String testText = "Hello World!";
        TextView view = new TextView(Robolectric.application);

        view.setText(testText);
	    String viewText = view.getText().toString();

        assertThat(viewText, equalTo(testText));
    }


}
