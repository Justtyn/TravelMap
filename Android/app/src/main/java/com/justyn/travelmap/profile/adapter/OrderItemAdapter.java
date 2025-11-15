package com.justyn.travelmap.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.travelmap.R;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.model.OrderItemDetail;
import com.justyn.travelmap.ui.common.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItemDetail> items = new ArrayList<>();

    public void submitList(List<OrderItemDetail> newItems) {
        if (newItems == null) {
            items = new ArrayList<>();
        } else {
            items = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvQuantity;
        private final TextView tvPrice;

        OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivOrderItemCover);
            tvTitle = itemView.findViewById(R.id.tvOrderItemTitle);
            tvDesc = itemView.findViewById(R.id.tvOrderItemDesc);
            tvQuantity = itemView.findViewById(R.id.tvOrderItemQuantity);
            tvPrice = itemView.findViewById(R.id.tvOrderItemPrice);
        }

        void bind(OrderItemDetail detail) {
            FeedItem product = detail.getProduct();
            if (product != null) {
                tvTitle.setText(product.getTitle());
                tvDesc.setText(product.getDescription());
                ImageLoader.load(ivCover, product.getImageUrl());
            } else {
                tvTitle.setText(R.string.app_name);
                tvDesc.setText("");
                ivCover.setImageResource(R.drawable.ic_image_placeholder);
            }
            tvQuantity.setText(itemView.getContext().getString(R.string.order_item_quantity, detail.getQuantity()));
            double price = detail.getPrice();
            if (!Double.isNaN(price)) {
                tvPrice.setText(String.format("¥%.2f", price));
                tvPrice.setVisibility(View.VISIBLE);
            } else if (product != null && product.getPriceLabel() != null) {
                tvPrice.setText(product.getPriceLabel());
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }
        }
    }
}
