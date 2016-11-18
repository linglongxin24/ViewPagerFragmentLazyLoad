package cn.bluemobi.dylan.viewpagerfragmentlazyload;

import android.util.Log;

/**
 * Created by yuandl on 2016-11-17.
 */

public class Fragment1 extends LazyLoadFragment {
    @Override
    public int setContentView() {
        return R.layout.fm_layout1;
    }

    @Override
    protected void lazyLoad() {
        String message = "Fragment1" + (isInit ? "已经初始并已经显示给用户可以加载数据" : "没有初始化不能加载数据")+">>>>>>>>>>>>>>>>>>>";
        showToast(message);
        Log.d(TAG, message);
    }

    @Override
    protected void stopLoad() {
        Log.d(TAG, "Fragment1" + "已经对用户不可见，可以停止加载数据");
    }
}
