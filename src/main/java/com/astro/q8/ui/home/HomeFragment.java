package com.astro.q8.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.astro.q8.adapter.CategoriesGridAdapter;
import com.astro.q8.adapter.CategoriesList;
import com.astro.q8.ui.LoginActivity;
import com.astro.q8.ui.RegisterActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.astro.q8.MainActivity;
import com.astro.q8.Site;
import com.astro.q8.adapter.BannerRecyclerClass;
import com.astro.q8.adapter.BannersRecyclerAdapter;
import com.astro.q8.adapter.ProductGridAdapter;
import com.astro.q8.adapter.ProductList;
import com.astro.q8.adapter.VideoBannerRecyclerAdapter;
import com.astro.q8.handler.SiteInfo;
import com.astro.q8.handler.UpdateCartCount;
import com.astro.q8.handler.UserSession;
import com.astro.q8.ui.MyGridView;
import com.astro.q8.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {


    View root;
    TextView cartCounter;
    UserSession userSession;
    SiteInfo siteInfo;
//    Dialog loadingDialog;
    boolean firstTimeHere = true;


    MyGridView featuredProductsGrid;
    MyGridView catGridView;
    ArrayList<ProductList> featuredProductLists;
    ArrayList<CategoriesList> catGridLists;
    ProductGridAdapter featuredProductsAdapter;
    CategoriesGridAdapter catGridAdapter;
    Button featuredProductsRefresh;
    ShimmerFrameLayout featuredProductsShim;
    ShimmerFrameLayout bannerShimmer;

    Button loginBtn, signUpBtn;
    LinearLayout loggedCheck;

    RecyclerView bannersRecycler; //slide banner

    RequestQueue requestQueue;

    //because of cartCounter
    @SuppressLint("ResourceType")
    public void initOnCreateViews() {

        userSession = new UserSession(requireContext().getApplicationContext());

        siteInfo = new SiteInfo(requireActivity().getApplicationContext());

        ((MainActivity) getActivity()).appBarType("welcome");

        ((MainActivity) getActivity()).setDrawerToggle();


        if (firstTimeHere) {
            firstTimeHere = false;

            //FOR BANNER RECYCLER
            bannersRecycler = (RecyclerView) requireActivity().findViewById(R.id.bannerRecycler);
            bannersRecycler.setVisibility(View.GONE);

            requestQueue = Volley.newRequestQueue(requireContext());

            loggedCheck = (LinearLayout) requireActivity().findViewById(R.id.logged_check);
            loginBtn = (Button) requireActivity().findViewById(R.id.login_btn);
            signUpBtn = (Button) requireActivity().findViewById(R.id.register_btn);

            if (userSession.logged()) {
                loggedCheck.setVisibility(View.GONE);
            } else {
                loggedCheck.setVisibility(View.VISIBLE);
            }

            featuredProductsShim = (ShimmerFrameLayout) requireActivity().findViewById(R.id.shimmer_view_container);
            featuredProductsShim.startShimmer();
            featuredProductsShim.setVisibility(View.VISIBLE);
            bannerShimmer = (ShimmerFrameLayout) requireActivity().findViewById(R.id.banner_shimmer);
            bannerShimmer.setVisibility(View.GONE);

            featuredProductsRefresh = (Button) requireActivity().findViewById(R.id.featuredRefresh);
            featuredProductsRefresh.setVisibility(View.GONE);
            featuredProductsGrid = (MyGridView) requireActivity().findViewById(R.id.featuredGrid);
            featuredProductLists = new ArrayList<>();
            featuredProductsAdapter = new ProductGridAdapter(requireActivity(), featuredProductLists, cartCounter, R.layout.single_product_card);
            featuredProductsGrid.setAdapter(featuredProductsAdapter);

            //categories Grids
            catGridView = (MyGridView) requireActivity().findViewById(R.id.categories_grids);
            catGridLists = new ArrayList<>();
            catGridAdapter = new CategoriesGridAdapter(requireActivity(), catGridLists);
            catGridView.setAdapter(catGridAdapter);

            fetchAvailableBanners();

            fetchCategoriesGrids();

            ((TextView) requireActivity().findViewById(R.id.by_trending)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("order_by", "popularity");
                    ((MainActivity) getActivity()).changeFragment(R.id.shop_fragment, bundle);
                }
            });
            ((LinearLayout) requireActivity().findViewById(R.id.view_all_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).changeFragment(R.id.shop_fragment);
                }
            });


            //fetchProduct(featuredProductsAdapter, featuredProductLists, featuredProductsShim, featuredProductsRefresh, "popularity");

            featuredProductsRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fetchAvailableBanners();
                    //fetchProduct(featuredProductsAdapter, featuredProductLists, featuredProductsShim, featuredProductsRefresh, "popularity");
                }
            });

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                }
            });
            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(requireActivity(), RegisterActivity.class));
                }
            });


        }

    }
    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        initOnCreateViews();

        ImageView iconImage =  (ImageView) cartCount.findViewById(R.id.cartIconMenu);
//        iconImage.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        userSession = new UserSession(requireActivity().getApplicationContext());
        new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
        cartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });

        MenuItem wishItem = menu.findItem(R.id.nav_menu_wishlist);
        wishItem.setActionView(R.layout.wislist_icon_update_layout);
        RelativeLayout wishLayout = (RelativeLayout) wishItem.getActionView();
        TextView wishNotification = (TextView) wishLayout.findViewById(R.id.actionbar_notifcation_textview);
        if (userSession.has_wishlist()) {
            wishNotification.setVisibility(View.VISIBLE);
        } else {
            wishNotification.setVisibility(View.GONE);
        }
        wishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(wishItem);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cartMenuIcon:{
                ((MainActivity)getActivity()).changeFragment(R.id.navigation_cart);
            }
            break;
            case R.id.nav_menu_wishlist: {
                if (userSession.logged()) {
                    ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
                } else {
                    Toast.makeText(requireContext(), "Please login first!", Toast.LENGTH_LONG).show();
                }
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (root == null) {
            root = inflater.inflate(R.layout.fragment_home, container, false);
        }

        return root;
    }

    public void fetchProduct(ProductGridAdapter adapter, ArrayList<ProductList> theList, ShimmerFrameLayout progressBar, Button refreshBtn, String category) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startShimmer();

        String url = Site.SIMPLE_PRODUCTS + "?orderby=popularity&per_page=20&hide_description=1&show_variation=1" + Site.TOKEN_KEY_APPEND;

        userSession = new UserSession(requireContext().getApplicationContext());
        if (userSession.logged()) {
            url += "&user_id=" + userSession.userID;
        }
        url = url + Site.TOKEN_KEY_APPEND;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }
//                Log.e("RES", response);
                parseJSONData(response, adapter, theList, progressBar, refreshBtn);
            }

        }, (VolleyError error) -> {
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            if (!isAdded()) return;
            Toast.makeText(getContext(), "Connection error. Please check your connection.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            progressBar.stopShimmer();
            refreshBtn.setVisibility(View.VISIBLE);
        }); //{
//
//            @Override
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                try {
//                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
//                    if (cacheEntry == null) {
//                        cacheEntry = new Cache.Entry();
//                    }
//                    final long cacheHitButRefreshed = 3 * 60 * 1000; // q8 3 minutes cache will be hit, but also refreshed on background
//                    final long cacheExpired = 24 * 60 * 60 * 1000; // q8 24 hours this cache entry expires completely
//                    long now = System.currentTimeMillis();
//                    final long softExpire = now + cacheHitButRefreshed;
//                    final long ttl = now + cacheExpired;
//                    cacheEntry.data = response.data;
//                    cacheEntry.softTtl = softExpire;
//                    cacheEntry.ttl = ttl;
//                    String headerValue;
//                    headerValue = response.headers.get("Date");
//                    if (headerValue != null) {
//                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                    }
//                    headerValue = response.headers.get("Last-Modified");
//                    if (headerValue != null) {
//                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                    }
//                    cacheEntry.responseHeaders = response.headers;
//                    final String jsonString = new String(response.data,
//                            HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(jsonString, cacheEntry);
//                } catch (UnsupportedEncodingException e) {
//                    return Response.error(new ParseError(e));
//                }
//            }
//
//            @Override
//            protected void deliverResponse(String response) {
//                super.deliverResponse(response);
//            }
//
//            @Override
//            public void deliverError(VolleyError error) {
//                super.deliverError(error);
//            }
//
//            @Override
//            protected VolleyError parseNetworkError(VolleyError volleyError) {
//                return super.parseNetworkError(volleyError);
//            }
//        };

        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json, ProductGridAdapter adapter, ArrayList<ProductList> theList, ShimmerFrameLayout progressBar, Button refreshBtn) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

//            Log.e("RESSPONSE", String.valueOf(array.length()));

            if (!isAdded()) return;

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String name = jsonObject.getString("name");
//                String title = jsonObject.getString("title");
                String image = jsonObject.getString("image");
                String price = jsonObject.getString("price");
                String product_type = jsonObject.getString("product_type");
                String regular_price = jsonObject.getString("regular_price");
                String type = jsonObject.getString("type");
                String description = jsonObject.getString("description");
                String in_wishlist = jsonObject.getString("in_wishlist");
                String categories = jsonObject.getString("categories");
                String stock_status = jsonObject.getString("stock_status");
                String lowest_price = jsonObject.getString("lowest_variation_price");
                JSONArray attributes = jsonObject.getJSONArray("attributes");
                JSONArray variations = (!jsonObject.getString("variations").equals("null")) ? jsonObject.getJSONArray("variations") : new JSONArray();

                ProductList list = new ProductList();
                list.setId(id);
//                list.setTitle(title);
                list.setName(name);
                list.setImage(image);
                list.setPrice(price);
                list.setRegular_price(regular_price);
                list.setType(type);
                list.setProduct_type(product_type);
                list.setDescription(description);
                list.setIn_wish_list(in_wishlist);
                list.setCategories(categories);
                list.setStock_status(stock_status);
                list.setLowest_price(lowest_price);
                list.setAttributes(attributes);
                list.setVariations(variations);
                theList.add(list);
            }
//            Toast.makeText(getContext(), String.valueOf(theList.size()), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!isAdded()) return;
        //when fetching is ready
        adapter.notifyDataSetChanged();
        progressBar.stopShimmer();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);
        ((LinearLayout) requireActivity().findViewById(R.id.view_all_layout)).setVisibility(View.VISIBLE);


    }


    RecyclerView carouselBannerRecycler;

    public void fetchAvailableBanners() {
        carouselBannerRecycler = (RecyclerView)  root.findViewById(R.id.carousel_banner_recycler); //to be able to add auto scroll

        if (siteInfo.is_banner_enabled("slide"))
            fetchForBanners(bannersRecycler, "slide", R.layout.single_banner_recycler_layout, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (siteInfo.is_banner_enabled("big"))
            fetchForBanners((RecyclerView) root.findViewById(R.id.big_banner_recycler), "big", R.layout.single_big_banner_recycler_layout, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (siteInfo.is_banner_enabled("carousel"))
            fetchForBanners(carouselBannerRecycler, "carousel", R.layout.single_carousel_banner_recycler_layout, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (siteInfo.is_banner_enabled("thin"))
            fetchForBanners((RecyclerView) root.findViewById(R.id.thin_banner_recycler), "thin", R.layout.single_thin_banner_recycler_layout, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (siteInfo.is_banner_enabled("grid"))
            fetchForBanners((RecyclerView) root.findViewById(R.id.grid_banner_recycler), "grid", R.layout.single_grid_banner_recycler_layout, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        if (siteInfo.is_banner_enabled("video"))
            fetchVideoBanner();
    }


    public void fetchForBanners(RecyclerView bannerRecyclerView, String type, int layout, RecyclerView.LayoutManager layoutManager) {
        if (type.equals("slide")) bannerShimmer.setVisibility(View.VISIBLE);
        if (type.equals("slide")) bannerShimmer.startShimmer();

        ArrayList<BannerRecyclerClass> bannerLists = new ArrayList<>();

        String url = Site.BANNERS + "?type=" + type + Site.TOKEN_KEY_APPEND;

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!isAdded()) return;
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray banners = object.getJSONArray("results");
                    if (object.getInt("count") < 1) {
                        bannerRecyclerView.setVisibility(View.GONE);
                        if (type.equals("slide")) bannerShimmer.setVisibility(View.GONE);
                        return;
                    }
                    for(int i = 0; i < banners.length(); i++) {
                        JSONObject banner = banners.getJSONObject(i);
                        bannerLists.add(
                                new BannerRecyclerClass(
                                        false,
                                        0,
                                        banner.getString("image"),
                                        banner.getString("title"),
                                        banner.getString("description"),
                                        banner.getString("on_click_to"),
                                        banner.getString("category"),
                                        banner.getString("url")
                                )
                        );
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BannersRecyclerAdapter bannerAdapter = new BannersRecyclerAdapter(getActivity(), layout, bannerLists);
                bannerRecyclerView.setAdapter(bannerAdapter);
                bannerRecyclerView.setLayoutManager(layoutManager);

                bannerRecyclerView.setVisibility(View.VISIBLE);

                if (type.equals("carousel")) {
                    SwipeCarouselTask swipeTask = new SwipeCarouselTask();
                    Timer timer = new Timer();
                    timer.schedule(swipeTask, 0, 4000);
                }


                if (type.equals("slide")) bannerShimmer.stopShimmer();
                if (type.equals("slide")) bannerShimmer.setVisibility(View.GONE);

            }
        }, (VolleyError error) -> {
            if (!isAdded()) return;
            //handle error
            bannerRecyclerView.setVisibility(View.GONE);
            if (type.equals("slide")) bannerShimmer.setVisibility(View.GONE);
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // q8 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // q8 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    public void fetchVideoBanner() {

        RecyclerView videoRecyclerView = (RecyclerView) root.findViewById(R.id.video_banner_recycler);
        ArrayList<BannerRecyclerClass> bannerLists = new ArrayList<>();

        String url = Site.BANNERS + "?type=video" + Site.TOKEN_KEY_APPEND;

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!isAdded()) return;
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray banners = object.getJSONArray("results");
                    if (object.getInt("count") < 1) {
                        videoRecyclerView.setVisibility(View.GONE);
                        return;
                    }
                    for(int i = 0; i < banners.length(); i++) {
                        JSONObject banner = banners.getJSONObject(i);
                        bannerLists.add(
                                new BannerRecyclerClass(
                                        false,
                                        0,
                                        banner.getString("image"),
                                        banner.getString("title"),
                                        banner.getString("description"),
                                        banner.getString("on_click_to"),
                                        banner.getString("category"),
                                        banner.getString("url")
                                )
                        );
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                VideoBannerRecyclerAdapter bannerAdapter = new VideoBannerRecyclerAdapter(getActivity(), R.layout.single_video_banner_recycler_layout, bannerLists);
                videoRecyclerView.setAdapter(bannerAdapter);
                videoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

                videoRecyclerView.setVisibility(View.VISIBLE);

            }
        }, (VolleyError error) -> {
            if (!isAdded()) return;
            //handle error
            videoRecyclerView.setVisibility(View.GONE);
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // q8 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // q8 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    static int currentCarouselBannerPosition = 0;
    private class SwipeCarouselTask extends TimerTask {
        public void run() {
            carouselBannerRecycler.post(()->{
                if (Objects.requireNonNull(carouselBannerRecycler.getAdapter()).getItemCount() > 0) {
                    currentCarouselBannerPosition++;
                    if (currentCarouselBannerPosition >= carouselBannerRecycler.getAdapter().getItemCount()) {
                        currentCarouselBannerPosition = 0;
                    }
//                    Log.e("hello", "Slide" + currentBannerPosition);
                    carouselBannerRecycler.smoothScrollToPosition(currentCarouselBannerPosition);
                }
            });
        }
    }


    public void fetchCategoriesGrids() {
        featuredProductsShim.setVisibility(View.VISIBLE);
        featuredProductsShim.startShimmer();
        String url = Site.CATEGORIES + "?hide_empty=1&order_by=menu_order" + Site.TOKEN_KEY_APPEND;

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }

                catGridLists = new ArrayList<>();

                try {
                    JSONArray array = new JSONArray(response);
                    if (!isAdded()) return;

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject category = array.getJSONObject(i);
                        catGridLists.add(new CategoriesList(category.getString("name"), category.getString("slug"), category.getString("count"), null, category.getString("image"), category.getString("icon")));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                catGridAdapter = new CategoriesGridAdapter(requireActivity(), catGridLists);
                catGridView.setAdapter(catGridAdapter);
                featuredProductsShim.stopShimmer();
                featuredProductsShim.setVisibility(View.GONE);
                featuredProductsRefresh.setVisibility(View.GONE);


            }
        }, (VolleyError error) -> {
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            if (!isAdded()) return;

            featuredProductsShim.setVisibility(View.GONE);
            featuredProductsShim.stopShimmer();
            featuredProductsRefresh.setVisibility(View.VISIBLE);
        });

        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


}