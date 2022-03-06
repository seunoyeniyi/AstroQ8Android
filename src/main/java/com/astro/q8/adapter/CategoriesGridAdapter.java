package com.astro.q8.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.astro.q8.MainActivity;
import com.astro.q8.R;
import com.astro.q8.Site;
import com.astro.q8.handler.AddToCartWithUpdateCartCount;
import com.astro.q8.handler.PriceFormatter;
import com.astro.q8.handler.UserSession;
import com.astro.q8.ui.AttributeBottomSheet;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoriesGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<CategoriesList> catLists;
    Activity activity;

    public CategoriesGridAdapter(Context context, ArrayList<CategoriesList> catLists) {
        this.context = context;
        this.catLists = catLists;
        this.activity = (Activity) context;
    }

    @Override
    public int getCount() {
        return catLists.size();
    }

    @Override
    public Object getItem(int i) {
        return catLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n", "DefaultLocale", "ResourceAsColor"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        rowView = layoutInflater.inflate(R.layout.single_cat_grid_layout, null);

        TextView titleView =(TextView) rowView.findViewById(R.id.textView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.cat_image);
        LinearLayout catLayout = (LinearLayout) rowView.findViewById(R.id.cat_layout);

        titleView.setText(Html.fromHtml(catLists.get(position).getName()));

        if (!catLists.get(position).getImage().equals("false")) {

            Glide
                .with(activity)
                .load(catLists.get(position).getImage().replace("localhost", Site.DOMAIN))
                .placeholder(R.drawable.sample_placeholder)
                .into(imageView);
        }

        catLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("cat_title", catLists.get(position).getName());
                bundle.putString("category_name", catLists.get(position).getSlug());
                ((MainActivity) activity).changeFragment(R.id.archive_fragment, bundle);
            }
        });


        return rowView;
    }

}