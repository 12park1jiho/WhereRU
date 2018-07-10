package jiho.whereru.org.ignitednewapplication.Util;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import jiho.whereru.org.ignitednewapplication.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by pingrae on 2017. 10. 21..
 */
public class SetAlramCycleFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY), minute = c.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, hour, minute, true);
        TextView tvTitle = new TextView(getActivity());
        tvTitle.setText("알림 주기 설정");
        //tvTitle.setBackgroundColor();
        tvTitle.setPadding(5, 3, 5, 3);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tpd.setCustomTitle(tvTitle);
        return tpd;
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        TextView tv = (TextView) getActivity().findViewById( R.id.TV_insertTime);
        tv.setText(String.valueOf(hour) + "시간 " + String.valueOf(minute) + "분");

        SharedPreferences pref = getActivity().getSharedPreferences("f_uid",MODE_PRIVATE);
        String uid = pref.getString( "f_uid", "null");
        DatabaseReference timeRef = FirebaseDatabase.getInstance().getReference(uid);
        timeRef.child( "hour" ).setValue( String.valueOf( hour ) );
        timeRef.child( "minute" ).setValue( String.valueOf( minute ) );
    }
}