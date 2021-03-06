package com.kigael.safemountain.ui.activate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.kigael.safemountain.MainActivity;
import com.kigael.safemountain.R;
import com.kigael.safemountain.service.FileSystemObserverService;
import com.kigael.safemountain.transfer.Restore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ActivateFragment extends Fragment {

    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_activate, container, false);
        final Button activate_deactivate_Button  = root.findViewById(R.id.buttonActivateNDeactivate);
        final TextView ObserverCount = root.findViewById(R.id.ObserverCount);
        final TextView BackupCount = root.findViewById(R.id.BackupCount);
        final DialogInterface.OnClickListener dialogClickListenerDeactivate = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent myIntent = new Intent(context, FileSystemObserverService.class);
                        context.stopService(myIntent);
                        Toast.makeText(context,"Safe Mountain deactivated",Toast.LENGTH_LONG).show();
                        changeActivateStatus(context);
                        activate_deactivate_Button.setBackgroundResource(R.drawable.activate);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(context,"Deactivated cancelled",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        final DialogInterface.OnClickListener dialogClickListenerActivate = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent myIntent = new Intent(context, FileSystemObserverService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(myIntent);
                        } else {
                            context.startService(myIntent);
                        }
                        Toast.makeText(context,"Safe Mountain activated",Toast.LENGTH_LONG).show();
                        changeActivateStatus(context);
                        activate_deactivate_Button.setBackgroundResource(R.drawable.deactivate);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(context,"Activated cancelled",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        context = container.getContext();
        if(!checkActivationStatus(context)){activate_deactivate_Button.setBackgroundResource(R.drawable.activate);}
        else{activate_deactivate_Button.setBackgroundResource(R.drawable.deactivate);}
        activate_deactivate_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkLoginStatus(context)){
                    Toast.makeText(context,"Login is required",Toast.LENGTH_LONG).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    if(checkActivationStatus(context)){
                        builder.setMessage("Deactivate Safe Mountain?").setPositiveButton("YES", dialogClickListenerDeactivate)
                                .setNegativeButton("NO", dialogClickListenerDeactivate).show();
                    }
                    else{
                        builder.setMessage("Activate Safe Mountain?").setPositiveButton("YES", dialogClickListenerActivate)
                                .setNegativeButton("NO", dialogClickListenerActivate).show();
                    }
                }
            }
        });
        if(checkActivationStatus(context)){
            ObserverCount.setText(FileSystemObserverService.Observer_Count+" Files are being watched");
            String sql = "SELECT * FROM Files_To_Transfer";
            Cursor cursor = MainActivity.database.rawQuery(sql,null);
            if(cursor==null||cursor.getCount()==0){
                BackupCount.setText("All files are backedup");
                if(cursor!=null){
                    cursor.close();
                }
            }
            else{
                if(cursor.getCount()>1){
                    BackupCount.setText(cursor.getCount()+" Files are being backedup");
                }
                else{
                    BackupCount.setText("1 File is being backedup");
                }
                cursor.close();
            }
        }
        else{
            ObserverCount.setText("Safe Mountain deactivated");
            BackupCount.setText("");
        }
        return root;
    }

    private boolean checkActivationStatus(Context context){
        String activate_info_path = context.getFilesDir().toString()+"/activate_info.txt";
        File activate_info = new File(activate_info_path);
        if(!activate_info.exists()) return false;
        else if(activate_info.exists()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(activate_info));
                boolean check = Boolean.parseBoolean(br.readLine());
                br.close();
                return check;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean checkLoginStatus(Context context){
        String account_info_path = context.getFilesDir().toString()+"/account_info.txt";
        File account_info = new File(account_info_path);
        return account_info.exists();
    }

    private void changeActivateStatus(Context context){
        String activate_info_path = context.getFilesDir().toString()+"/activate_info.txt";
        File activate_info = new File(activate_info_path);
        boolean currentStatus;
        try{
            BufferedReader br = new BufferedReader(new FileReader(activate_info));
            currentStatus = Boolean.parseBoolean(br.readLine());
            br.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(activate_info,false));
            if(currentStatus){bw.write("false");}
            else{bw.write("true");}
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}