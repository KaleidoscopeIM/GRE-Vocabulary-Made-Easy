package com.curiousfreaks.greword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.curiousfreaks.greword.inappbilling.utils.IabHelper;
import com.curiousfreaks.greword.inappbilling.utils.IabResult;
import com.curiousfreaks.greword.inappbilling.utils.Inventory;
import com.curiousfreaks.greword.inappbilling.utils.Purchase;

public class BuyCoffee extends AppCompatActivity {
    Button buyButton;
    IabHelper mHelper;
    private final String item="acoffee";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_coffee);
        buyButton=findViewById(R.id.buyCoffee);

        String base64EncodedKey="";
        mHelper=new IabHelper(getApplicationContext(),base64EncodedKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener(){
            @Override
            public void onIabSetupFinished(IabResult result) {
                if(result.isSuccess())
                {
                    //Toast.makeText(getApplicationContext(),"Connection Success",Toast.LENGTH_SHORT).show();
                }
                if(result.isFailure())
                {
                    //Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mHelper.launchPurchaseFlow(BuyCoffee.this, item, 99, purchaseFinishedListener, "myCoffee");

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!mHelper.handleActivityResult(requestCode,resultCode,data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    private final IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener=new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            try {
                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor=sharedPreferences.edit();
                if (info.getSku().equals(item)) {
                    prefEditor.putBoolean("PURCHASE",true);
                    mHelper.queryInventoryAsync(inventoryListener);
                }
                if (!info.getSku().equals(item)) {
                    prefEditor.putBoolean("PURCHASE",false);
                    Toast.makeText(getApplicationContext(), "There is some issue in purchasing coffee.", Toast.LENGTH_SHORT).show();
                }
                prefEditor.apply();
                prefEditor.commit();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    };
    private final IabHelper.QueryInventoryFinishedListener inventoryListener =new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            try {
                if (!result.isFailure()) {
                    mHelper.consumeAsync(inv.getPurchase(item), consumeFinishLitener);
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    };
    private final IabHelper.OnConsumeFinishedListener consumeFinishLitener=new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if(result.isSuccess())
            {
                Toast.makeText(getApplicationContext(),"Thanks you for purchasing me a coffee.",Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mHelper != null) {
                mHelper.dispose();
                mHelper = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}