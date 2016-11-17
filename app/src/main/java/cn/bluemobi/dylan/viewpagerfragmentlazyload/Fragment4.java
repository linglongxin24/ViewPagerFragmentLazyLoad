package cn.bluemobi.dylan.viewpagerfragmentlazyload;

import android.util.Log;

/**
 * Created by yuandl on 2016-11-17.
 */

public class Fragment4 extends LazyLoadFragment {

    @Override
    public int setContentView() {
        return R.layout.fm_layout4;
    }

    @Override
    protected void lazyLoad() {
        String message = "Fragment4" + (isInit ? "已经初始可以加载数据" : "没有初始化不能加载数据");
        showToast(message);
        Log.d(TAG, message);
    }
}
