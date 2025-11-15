package com.justyn.travelmap.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.justyn.travelmap.R;
import com.justyn.travelmap.model.CartItem;
import com.justyn.travelmap.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartActionListener {
        void onQuantityChanged(@NonNull CartItem item, int newQuantity);

        void onItemDeleted(@NonNull CartItem item);
    }

    private final CartActionListener listener;
    private List<CartItem> items = new ArrayList<>();

    public CartAdapter(CartActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> newItems) {
        if (newItems == null) {
            items = new ArrayList<>();
        } else {
            items = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_entry, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvQuantity;
        private final TextView tvPrice;
        private final android.widget.ImageButton btnMinus;
        private final android.widget.ImageButton btnPlus;
        private final MaterialButton btnDelete;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCartCover);
            tvTitle = itemView.findViewById(R.id.tvCartTitle);
            tvDesc = itemView.findViewById(R.id.tvCartDesc);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            btnMinus = itemView.findViewById(R.id.btnQuantityMinus);
            btnPlus = itemView.findViewById(R.id.btnQuantityPlus);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }

        void bind(CartItem item, CartActionListener listener) {
            FeedItem product = item.getProduct();
            if (product != null) {
                tvTitle.setText(product.getTitle());
                tvDesc.setText(product.getDescription());
                if (product.getPriceLabel() != null && !product.getPriceLabel().isEmpty()) {
                    tvPrice.setVisibility(View.VISIBLE);
                    tvPrice.setText(product.getPriceLabel());
                } else {
                    tvPrice.setVisibility(View.GONE);
                }
                Glide.with(ivCover.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivCover);
            } else {
                tvTitle.setText(R.string.app_name);
                tvDesc.setText("");
                tvPrice.setVisibility(View.GONE);
                ivCover.setImageResource(R.drawable.ic_image_placeholder);
            }
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            btnMinus.setEnabled(item.getQuantity() > 1);
            btnMinus.setOnClickListener(v -> {
                if (listener != null && item.getQuantity() > 1) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });
            btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDeleted(item);
                }
            });
        }
    }
}
