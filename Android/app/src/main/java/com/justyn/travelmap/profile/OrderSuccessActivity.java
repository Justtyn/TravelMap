package com.justyn.travelmap.profile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.justyn.travelmap.R;

public class OrderSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_NO = "extra_order_no";
    public static final String EXTRA_ORDER_PRICE = "extra_order_price";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        TextView tvOrderNo = findViewById(R.id.tvOrderNo);
        TextView tvOrderPrice = findViewById(R.id.tvOrderPrice);
        MaterialButton btnFinish = findViewById(R.id.btnFinish);
        String orderNo = getIntent().getStringExtra(EXTRA_ORDER_NO);
        String price = getIntent().getStringExtra(EXTRA_ORDER_PRICE);
        tvOrderNo.setText(getString(R.string.order_success_no, orderNo));
        tvOrderPrice.setText(getString(R.string.order_success_price, price));
        btnFinish.setOnClickListener(v -> finish());
    }
}
