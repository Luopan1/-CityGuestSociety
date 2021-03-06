package www.cityguestsociety.com.firstfragmentactivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.apkfuns.logutils.LogUtils;
import com.bumptech.glide.Glide;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import www.cityguestsociety.com.R;
import www.cityguestsociety.com.UrlFactory;
import www.cityguestsociety.com.adapter.BaseRecyclerAdapter;
import www.cityguestsociety.com.adapter.BaseRecyclerHolder;
import www.cityguestsociety.com.application.MyApplication;
import www.cityguestsociety.com.baseui.BaseFragment;
import www.cityguestsociety.com.entity.GuojiangNews;
import www.cityguestsociety.com.ui.LazyFragmentPagerAdapter;

/**
 * Created by LuoPan on 2017/5/16 9:35.
 */
public class NewsFragment extends BaseFragment implements LazyFragmentPagerAdapter.Laziable {

    private View mView;
    private LRecyclerView mListView;
    List<GuojiangNews.DataBean> lists;
    String index;

    /**
     * 服务器端一共多少条数据
     */
    private static int TOTAL_COUNTER = 0;

    /**
     * 每一页展示多少条数据
     */
    private static final int REQUEST_COUNT = MyApplication.getCount;

    /**
     * 已经获取到多少条数据了
     */
    private int mCurrentCounter = 0;

    private int mCurrentPage = 1;
    private BaseRecyclerAdapter mAdapter;
    private List<GuojiangNews.DataBean> mDataList;
    private View mEmptyView;
    private LRecyclerViewAdapter mRecyclerViewAdapter;
    private boolean isRefresh = false;

    @SuppressLint({"NewApi", "ValidFragment"})
    public NewsFragment() {

    }

    @SuppressLint({"NewApi", "ValidFragment"})
    public NewsFragment(String i) {
        lists = new ArrayList<>();
        mDataList = new ArrayList<>();
        index = i;
    }

    public static NewsFragment newInstance(String i) {

        NewsFragment fragment = new NewsFragment(i);
        return fragment;
    }


    public void setListener() {
        mRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LogUtils.e(position);
                Bundle bundle = new Bundle();
                bundle.putString(GuoJiangWebViewActivity.ID, mDataList.get(position).getId());
                jumpToActivity(GuoJiangWebViewActivity.class, bundle, false);
            }
        });
        mListView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                isRefresh = false;
                if (mCurrentCounter < TOTAL_COUNTER) {
                    // loading more
                    initData();
                } else {
                    //the end
                    mListView.setNoMore(true);
                }
            }
        });

        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                mCurrentPage = 1;
                mCurrentCounter = 0;
                initData();

            }
        });

        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });

    }

    @Override
    protected int setLayoutResID() {
        return R.layout.fragment_news;
    }

    private void setAdapter() {
        //        mAdapter = new QuickAdapter();
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new BaseRecyclerAdapter<GuojiangNews.DataBean>(getActivity(), mDataList, R.layout.item_guojiang) {

            @Override
            public void convert(BaseRecyclerHolder holder, GuojiangNews.DataBean item, int position, boolean isScrolling) {
                holder.setText(R.id.textViewGJ, item.getTitle());
                holder.setText(R.id.textViewGJInfo, item.getCreation_time());
                if (item.getImg() != null) {
                    ImageView imageView = holder.getView(R.id.imageViewGJ);
                    Glide.with(getActivity()).load(UrlFactory.imaPath + item.getImg()).asBitmap().centerCrop().into(imageView);
                } else {
                    holder.setImageResource(R.id.imageViewGJ, R.drawable.sysyh);
                }
            }
        };
        mRecyclerViewAdapter = new LRecyclerViewAdapter(mAdapter);
        mListView.setAdapter(mRecyclerViewAdapter);


    }


    @Override
    protected void initView() {
        mListView = getView(R.id.newsFragments);
        mEmptyView = getView(R.id.empty_view);
        //        mListView.setEmptyView(mEmptyView);


        mListView.setFooterViewColor(R.color.colorAccent, R.color.white, android.R.color.white);
        //设置底部加载文字提示
        mListView.setFooterViewHint("拼命加载中", "已经全部为你呈现了", "网络不给力啊，点击再试一次吧");
        setAdapter();

    }

    public void initData() {

        RequestParams params = new RequestParams();
        params.put("next", mCurrentPage);
        params.put("label", index);
        params.put("page", REQUEST_COUNT);
        getDataFromInternet(UrlFactory.carpenter_list, params, 0);


    }

    @Override
    public void getSuccess(JSONObject object, int what) {
        switch (what) {
            case 0:
                Gson gson = new Gson();
                lists.clear();
                if (isRefresh) {
                    mDataList.clear();
                }
                GuojiangNews guojiangNews = gson.fromJson(object.toString(), GuojiangNews.class);
                lists = guojiangNews.getData();

                mDataList.addAll(lists);

                TOTAL_COUNTER = Integer.parseInt(guojiangNews.getPagecount());
                mCurrentPage++;
                LogUtils.e(mDataList.toString());
                mCurrentCounter += lists.size();

                LogUtils.e(mCurrentCounter + "____" + TOTAL_COUNTER);

                mAdapter.notifyDataSetChanged();

                mListView.refreshComplete(REQUEST_COUNT);

                break;
        }

    }


}
