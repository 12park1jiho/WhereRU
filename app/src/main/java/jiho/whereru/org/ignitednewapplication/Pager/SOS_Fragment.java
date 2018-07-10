package jiho.whereru.org.ignitednewapplication.Pager;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import jiho.whereru.org.ignitednewapplication.R;

public class SOS_Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate( R.layout.sos__fragment ,container,false);
        Button btn_SOS = (Button)getActivity().findViewById( R.id.btn_SOS );
        btn_SOS.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( Intent.ACTION_VIEW ,Uri.parse( "tel:112" ));
                startActivity( intent );
            }
        } );
        return layout;

    }
}
