package uk.ac.tees.aad.W9517102.Common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.util.HashMap;

import uk.ac.tees.aad.W9517102.R;

public class UsersDialog {
    Context context;
    Dialog dialog;
    SelectUser selectUser;

    public UsersDialog(Context context) {
        this.context = context;
        dialog=new Dialog(context);
        selectUser= (SelectUser) context;

        dialog.setContentView(R.layout.layout_dialog_users);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnDriver = dialog.findViewById(R.id.btnDriver);
        Button btnRider = dialog.findViewById(R.id.btnRider);
        TextView closeApp = dialog.findViewById(R.id.closeApp);

        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectUser.selectUser("driver");
                dialog.dismiss();
            }
        });

        btnRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectUser.selectUser("rider");
                dialog.dismiss();
            }
        });
        closeApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}
