package com.example.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dreamera_master.R;
import com.example.interfaces.CityInterface;

import java.util.ArrayList;

/**
 * Created by yourgod on 2017/8/19.
 */

public class AddressSelector extends LinearLayout implements View.OnClickListener{

    private int textSelectedColor = getResources().getColor(R.color.colorPrimary);
    private int textEmptyColor = getResources().getColor(R.color.colorTextDark);
    //顶部tab集合
    private ArrayList<Tab> tabs;
    //列表适配器
    private AddressAdapter addressAdapter;
    private ArrayList<CityInterface> cities;
    private OnItemClickListener onItemClickListener;
    private OnTabSelectedListener onTabSelectedListener;
    private RecyclerView list;
    //tabs的外层layout
    private LinearLayout tabs_layout;
    //会移动的横线布局
    private Line line;
    private Context mContext;
    //总共tab的数量
    private int tabAmount = 3;
    //当前tab的位置
    private int tabIndex = 0;
    //分割线
    private View grayLine;
    //列表文字大小
    private int listTextSize = -1;
    //列表文字颜色
    private int listTextNormalColor = Color.parseColor("#333333");
    //列表文字选中颜色
    private int listTextSelectedColor = getResources().getColor(R.color.colorPrimary);
    //列表icon资源
    private int listItemIcon = -1;


    public AddressSelector(Context context) {
        this(context, null);
    }

    public AddressSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        tabIndex = 0;
        removeAllViews();
        this.mContext = context;
        setOrientation(VERTICAL);
        tabs_layout = new LinearLayout(mContext);
        tabs_layout.setWeightSum(tabAmount);
        tabs_layout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        tabs_layout.setOrientation(HORIZONTAL);
        addView(tabs_layout);
        tabs = new ArrayList<>();
        Tab tab = newTab("请选择", true);
        tabs_layout.addView(tab);
        tabs.add(tab);
        for (int i = 1; i < tabAmount; i++) {
            Tab _tab = newTab("", false);
            _tab.setIndex(i);
            tabs_layout.addView(_tab);
            tabs.add(_tab);
        }
        line = new Line(mContext);
        line.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, 6));
        line.setSum(tabAmount);
        addView(line);
        grayLine = new View(mContext);
        grayLine.setBackgroundColor(Color.parseColor("#dddddd"));
        grayLine.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
        addView(grayLine);
        list = new RecyclerView(mContext);
        list.setLayoutParams(new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        list.setLayoutManager(new LinearLayoutManager(mContext));
        addView(list);
    }

    /**
     * 得到一个新的tab对象
     */
    private Tab newTab(CharSequence text, boolean isSelected) {
        Tab tab = new Tab(mContext);
        tab.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, 40, 0, 40);
        tab.setSelected(isSelected);
        tab.setText(text);
        tab.setTextEmptyColor(textEmptyColor);
        tab.setTextSelectedColor(textSelectedColor);
        tab.setOnClickListener(this);
        return tab;
    }

    /**
     * 设置tab的数量，默认3个， 不少于2个
     * @param tabAmount
     */

    public void setTabAmount(int tabAmount) {
        if (tabAmount >= 2) {
            this.tabAmount = tabAmount;
            init(mContext);
        } else {
            throw new RuntimeException("AddressSelector tabAmount" +
                    "can not less than 2 !");
        }
    }

    /**
     * 设置列表的点击事件回调接口
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * 设置列表的数据源，设置后立即生效
     * @param cities
     */

    public void setCities(ArrayList cities) {
        if (cities == null || cities.size() <= 0) {
            return ;
        }
        if (cities.get(0) instanceof  CityInterface) {
            this.cities = cities;
            if (addressAdapter == null) {
                addressAdapter = new AddressAdapter();
                list.setAdapter(addressAdapter);
            }
            addressAdapter.notifyDataSetChanged();
        } else {
            throw new RuntimeException("AddressSelector cities must" +
                    " implements CityInterface");
        }
    }

    /**
     * 设置顶部tab的事件回调
     * @param listener
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.onTabSelectedListener = listener;
    }

    public void onClick(View v) {
        Tab tab = (Tab) v;
        //如果点击的tab大于index则直接跳出
        if (tab.index > tabIndex) {
            return ;
        }
        tabIndex = tab.index;
        if (onTabSelectedListener != null) {
            if (tab.isSelected) {
                onTabSelectedListener.onTabReselected(AddressSelector.this, tab);
            } else {
                onTabSelectedListener.onTabSelected(AddressSelector.this, tab);
            }
            resetAllTabs(tabIndex);
            line.setIndex(tabIndex);
            tab.setSelected(true);
        }
    }

    /**
     * 重置AddressSelector
     */

    public void resetAddressSelector() {
        tabIndex = 0;
        resetAllTabs(tabIndex);
        line.setIndex(0);
    }

    /**
     * 重置tab
     * @param tabIndex
     */

    public void resetAllTabs(int tabIndex) {
        if (tabs != null) {
            for (int i = 0; i < tabs.size(); i++) {
                tabs.get(i).resetState();
                if (i > tabIndex) {
                    tabs.get(i).setText("");
                }
            }
        }
    }

    /**
     *设置选中文字颜色
     * @param textSelectedColor
     */
    public void setTextSelectedColor(int textSelectedColor) {
        this.textSelectedColor = textSelectedColor;
    }
    /**
     * 设置默认文字颜色
     */
    public void setTextEmptyColor(int textEmptyColor) {
        this.textEmptyColor = textEmptyColor;
    }

    /**
     * 设置横线颜色
     * @param lineColor
     */
    public void setLineColor(int lineColor) {
        line.setSelctedColor(lineColor);
    }

    /**
     * 设置tab下方分割线颜色
     * @param grayLineColor
     */
    public void setGrayLineColor(int grayLineColor) {
        grayLine.setBackgroundColor(grayLineColor);
    }

    /**
     * 设置列表文字大小
     * @param listTextSize
     */
    public void setListTextSize(int listTextSize) {
        this.listTextSize = listTextSize;
    }

    /**
     * 设置列表文字默认颜色
     * @param listTextNormalColor
     */
    public void setListTextNormalColor(int listTextNormalColor) {
        this.listTextNormalColor = listTextNormalColor;
    }

    /**
     * 设置列表文字选中颜色
     * @param listTextSelectedColor
     */
    public void setListTextSelectedColor(int listTextSelectedColor) {
        this.listTextSelectedColor = listTextSelectedColor;
    }

    /**
     * 设置列表icon资源
     * @param listItemIcon
     */
    public void setListItemIcon(int listItemIcon) {
        this.listItemIcon = listItemIcon;
    }

    public class Tab extends android.support.v7.widget.AppCompatTextView {

        private int index = 0;
        private int textSelectedColor = getResources().getColor(R.color.colorPrimary);
        private int textEmptyColor = getResources().getColor(R.color.colorTextDark);

        private boolean isSelected = false;

        public Tab(Context context) {
            this(context, null);
        }

        public Tab(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public Tab(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            setTextSize(15);
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            if (!isSelected) {
                setTextColor(textEmptyColor);
            } else {
                setTextColor(textSelectedColor);
            }
            super.setText(text, type);
        }

        @Override
        public void setSelected(boolean selected) {
            isSelected = selected;
            setText(getText());
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void resetState() {
            isSelected = false;
            setText(getText());
        }

        public void setTextSelectedColor(int color) {
            this.textSelectedColor = color;
        }

        public void setTextEmptyColor(int color) {
            this.textEmptyColor = color;
        }
    }

    /**
     * 横线控件
     */

    public class Line extends LinearLayout {

        private int sum = 3;
        private int oldIndex = 0;
        private int nowIndex = 0;
        private View indicator;
        private int selectedColor = getResources().getColor(R.color.colorPrimary);

        public Line(Context context) {
            this(context, null);
        }

        public Line(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public Line(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init(context);
        }

        private void init(Context context) {
            setOrientation(HORIZONTAL);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 6));
            setWeightSum(tabAmount);
            indicator = new View(context);
            indicator.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
            indicator.setBackgroundColor(selectedColor);
            addView(indicator);
        }
        public void setIndex(int index) {
            int onceWidth = getWidth() / sum;
            this.nowIndex = index;
            ObjectAnimator animator = ObjectAnimator.ofFloat(indicator, "translationX",
                                        indicator.getTranslationX(), (nowIndex - oldIndex) * onceWidth);
            animator.setDuration(300);
            animator.start();
        }

        public void setSum(int sum) {
            this.sum = sum;
        }

        public void setSelctedColor(int color) {
            this.selectedColor = color;
        }
    }

    private class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.MyViewHolder> {

        @Override
        public AddressAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.address_selected_item_address, null);
            MyViewHolder viewHolder = new MyViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(AddressAdapter.MyViewHolder holder, int position) {
            if (listItemIcon != -1) {
                holder.img.setImageResource(listItemIcon);
            }
            if (listTextSize != -1) {
                holder.tv.setTextSize(listTextSize);
            }
            if (TextUtils.equals(tabs.get(tabIndex).getText(), cities.get(position).getCityName())) {
                holder.img.setVisibility(View.VISIBLE);
                holder.tv.setTextColor(listTextSelectedColor);
            } else {
                holder.img.setVisibility(View.INVISIBLE);
                holder.tv.setTextColor(listTextNormalColor);
            }
            holder.tv.setText(cities.get(position).getCityName());
            holder.itemView.setTag(cities.get(position));
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.itemClick(AddressSelector.this,
                                (CityInterface) v.getTag(), tabIndex);
                        tabs.get(tabIndex).setText(((CityInterface) v.getTag()).getCityName());
                        tabs.get(tabIndex).setTag(v.getTag());
                        if (tabIndex + 1 < tabs.size()) {
                            tabIndex++;
                            resetAllTabs(tabIndex);
                            line.setIndex(tabIndex);
                            tabs.get(tabIndex).setText("请选择");
                            tabs.get(tabIndex).setSelected(true);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv;
            private ImageView img;
            private View itemView;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                tv = (TextView) itemView.findViewById(R.id.address_selected_item_address_tv);
                img = (ImageView) itemView.findViewById(R.id.address_selected_item_address_img);
            }
        }
    }

    public interface OnItemClickListener {
        /**
         * city 返回地址列表对一个点击的对象
         * tabPosition 对应tab的位置
         */
        void itemClick(AddressSelector addressSelector, CityInterface city, int tabPosition);
    }

    public interface OnTabSelectedListener {
        void onTabSelected(AddressSelector addressSelector, Tab tab);
        void onTabReselected(AddressSelector addressSelctor, Tab tab);
    }
 }
