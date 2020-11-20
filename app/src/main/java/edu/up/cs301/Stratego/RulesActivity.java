package edu.up.cs301.Stratego;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import edu.up.cs301.R;

/**
 * loads up the rules page xml
 *
 * @author Gabby Marshak
 * @author Francisco Nguyen
 * @author Blake Nygren
 * @author Jack Volonte
 */
public class RulesActivity extends AppCompatActivity implements View.OnClickListener {
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(this);
    }

    /**
     * sends player back to the main game activity
     */
    @Override
    public void onClick(View view) {
        Log.i("testing back button", "back clicked");
        Intent mainIntent = new Intent(getApplicationContext(), StrategoMainActivity.class);
        startActivity(mainIntent);
    }
}