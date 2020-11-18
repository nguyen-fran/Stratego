package edu.up.cs301.Stratego;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import edu.up.cs301.R;

/**
 * loads up the rules page xml
 * TODO: set up onClickListener for back button so you can go back to main activity
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class RulesActivity extends AppCompatActivity {
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        backButton = findViewById(R.id.back_button);

        //backButton.setOnClickListener();
    }

    /**
     * sends player back to the main game activity
     */
    public void back(){
        Log.i("testing back button", "back clicked");
        Intent mainIntent = new Intent(getApplicationContext(), StrategoMainActivity.class);
        startActivity(mainIntent);
    }
}