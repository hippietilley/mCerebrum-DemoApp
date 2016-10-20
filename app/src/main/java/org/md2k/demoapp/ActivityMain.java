package org.md2k.demoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeInt;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;

import java.util.ArrayList;
import java.util.Random;

public class ActivityMain extends AppCompatActivity {
    DataKitAPI dataKitAPI;
    DataSourceClient dataSourceClientRegister =null;
    DataTypeInt dataTypeIntInsert =null;
    DataSourceClient dataSourceClientSubscribe=null;
    DataTypeInt dataTypeIntSubscribe=null;
    ArrayList<DataType> dataTypeIntQuery=null;
    TextView textViewConnect;
    TextView textViewRegister;
    TextView textViewInsert;
    TextView textViewSubscribe;
    TextView textViewQuery;
    Button buttonConnect;
    Button buttonRegister;
    Button buttonInsert;
    Button buttonSubscribe;
    Button buttonQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataKitAPI = DataKitAPI.getInstance(this);
        setUI();
        updateUI();
    }
    void setUI(){
        textViewConnect=(TextView) findViewById(R.id.textView_connect);
        textViewRegister=(TextView) findViewById(R.id.textView_register);
        textViewInsert=(TextView) findViewById(R.id.textView_insert);
        textViewSubscribe=(TextView) findViewById(R.id.textView_subscribe);
        textViewQuery=(TextView) findViewById(R.id.textView_query);
        buttonConnect=(Button) findViewById(R.id.button_connect);
        buttonRegister=(Button) findViewById(R.id.button_register);
        buttonInsert=(Button) findViewById(R.id.button_insert);
        buttonSubscribe=(Button) findViewById(R.id.button_subscribe);
        buttonQuery=(Button) findViewById(R.id.button_query);
        setButtonConnect();
        setButtonRegister();
        setButtonInsert();
        setButtonQuery();
        setButtonSubscribe();
    }
    void setButtonRegister(){
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(dataSourceClientRegister ==null) {
                        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.STATUS);
                        dataSourceClientRegister = dataKitAPI.register(dataSourceBuilder);
                    }else{
                        dataKitAPI.unregister(dataSourceClientRegister);
                        dataSourceClientRegister =null;
                    }
                } catch (DataKitException ignored) {
                    dataSourceClientRegister =null;
                }
                updateUI();
            }
        });
    }
    void setButtonSubscribe(){
        buttonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(dataSourceClientSubscribe ==null) {
                        Application application=new ApplicationBuilder().setId(ActivityMain.this.getPackageName()).build();
                        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.STATUS).setApplication(application);
                        ArrayList<DataSourceClient> dataSourceClients=dataKitAPI.find(dataSourceBuilder);
                        if(dataSourceClients.size()==0){
                            Toast.makeText(getBaseContext(),"DataSource not registered yet",Toast.LENGTH_SHORT).show();
                        }else {
                            dataSourceClientSubscribe=dataSourceClients.get(0);
                            dataKitAPI.subscribe(dataSourceClientSubscribe, new OnReceiveListener() {
                                @Override
                                public void onReceived(DataType dataType) {
                                    dataTypeIntSubscribe= (DataTypeInt) dataType;
                                    updateUI();
                                }
                            });
                        }
                    }else{
                        dataKitAPI.unsubscribe(dataSourceClientSubscribe);
                        dataSourceClientSubscribe=null;
                        dataTypeIntSubscribe=null;
                    }
                    updateUI();
                } catch (DataKitException ignored) {
                    dataSourceClientSubscribe=null;
                    dataTypeIntSubscribe=null;
                }
            }
        });
    }

    void setButtonInsert(){
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(dataSourceClientRegister !=null) {
                        dataTypeIntInsert =new DataTypeInt(DateTime.getDateTime(), Math.abs(new Random().nextInt()%10000));
                        dataKitAPI.insert(dataSourceClientRegister, dataTypeIntInsert);
                    }else{
                        Toast.makeText(getBaseContext(),"DataSource not registered yet",Toast.LENGTH_SHORT).show();
                    }
                } catch (DataKitException ignored) {
                    dataTypeIntInsert =null;
                }
                updateUI();
            }
        });
    }
    void setButtonQuery(){
        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Application application=new ApplicationBuilder().setId(ActivityMain.this.getPackageName()).build();
                    DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.STATUS).setApplication(application);
                    ArrayList<DataSourceClient> dataSourceClients=dataKitAPI.find(dataSourceBuilder);
                    if(dataSourceClients.size()==0){
                        Toast.makeText(getBaseContext(),"DataSource not registered yet",Toast.LENGTH_SHORT).show();
                    }else {
                        dataTypeIntQuery = dataKitAPI.query(dataSourceClients.get(0), 3);
                    }
                } catch (DataKitException ignored) {
                    dataTypeIntQuery=null;
                }
                updateUI();
            }
        });
    }
    void setButtonConnect(){
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(dataKitAPI.isConnected()){
                        dataKitAPI.disconnect();
                        dataSourceClientRegister =null;
                        dataTypeIntInsert =null;
                        dataSourceClientSubscribe=null;
                        dataTypeIntSubscribe=null;
                        dataTypeIntQuery=null;
                    }else {
                        dataKitAPI.connect(new OnConnectionListener() {
                            @Override
                            public void onConnected() {
                                updateUI();
                            }
                        });
                    }
                } catch (DataKitException ignored) {

                }
                updateUI();
            }
        });
    }
    void updateConnectView(){
        if(dataKitAPI.isConnected()){
            buttonConnect.setText("Disconnect");
            textViewConnect.setText("Connected");
        }else{
            buttonConnect.setText("Connect");
            textViewConnect.setText("Not connected");
        }
    }
    void updateRegisterView(){
        if(dataKitAPI.isConnected()){
            buttonRegister.setEnabled(true);
            if(dataSourceClientRegister ==null) {
                buttonRegister.setText("Register");
                textViewRegister.setText("Not registered");
            }else{
                buttonRegister.setText("Unregister");
                textViewRegister.setText("Registered");
            }
        }else{
            buttonRegister.setText("Register");
            buttonRegister.setEnabled(false);
            textViewRegister.setText("");
        }
    }
    void updateInsertView(){
        if(dataKitAPI.isConnected()){
            buttonInsert.setEnabled(true);
            if(dataTypeIntInsert ==null || dataSourceClientRegister==null) {
                textViewInsert.setText("");
            }else{
                textViewInsert.setText("Insert Data = "+String.valueOf(dataTypeIntInsert.getSample()));
            }
        }else{
            buttonInsert.setEnabled(false);
            dataTypeIntInsert=null;
            textViewInsert.setText("");
        }
    }
    void updateQueryView(){
        if(dataKitAPI.isConnected()){
            buttonQuery.setEnabled(true);
            if(dataTypeIntQuery ==null) {
                textViewQuery.setText("");
            }else{
                String str="";
                for(int i=0;i<dataTypeIntQuery.size();i++) {
                    DataTypeInt dataTypeInt= (DataTypeInt) dataTypeIntQuery.get(i);
                    if(str.length()!=0) str=str+"\n";
                    str=str+"Query Data("+(i+1)+") =  "+String.valueOf(dataTypeInt.getSample());
                }
                textViewQuery.setText(str);
            }
        }else{
            buttonQuery.setEnabled(false);
            dataTypeIntQuery=null;
            textViewQuery.setText("");
        }
    }
    void updateSubscribeView(){
        if(dataKitAPI.isConnected()){
            buttonSubscribe.setEnabled(true);
            if(dataSourceClientSubscribe ==null) {
                buttonSubscribe.setText("Subscribe");
                textViewSubscribe.setText("Not subscribed");
            }else{
                buttonSubscribe.setText("Unsubscribe");
                if(dataTypeIntSubscribe!=null)
                    textViewSubscribe.setText("Received Data = "+String.valueOf(dataTypeIntSubscribe.getSample()));
                else{
                    textViewSubscribe.setText("");
                }
            }
        }else{
            buttonSubscribe.setEnabled(false);
            dataSourceClientSubscribe=null;
            dataTypeIntSubscribe=null;
            textViewSubscribe.setText("");
        }
    }


    void updateUI(){
        updateConnectView();
        updateRegisterView();
        updateInsertView();
        updateQueryView();
        updateSubscribeView();
    }
}
