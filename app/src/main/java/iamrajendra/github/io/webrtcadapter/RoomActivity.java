package iamrajendra.github.io.webrtcadapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private EditText editText_roomName;
    private Button button_action;
    private ToggleButton toggleButton;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        intent = new Intent(this, ViewChangeActivity.class);
        editText_roomName = findViewById(R.id.room_name_tv);
        toggleButton = findViewById(R.id.join_tb);
        button_action = findViewById(R.id.action_b);
        button_action.setOnClickListener(this);
        toggleButton.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        intent.putExtra("roomName", editText_roomName.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        intent.putExtra("join", isChecked);
    }
}
