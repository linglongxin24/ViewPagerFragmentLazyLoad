#Android中ViewPager+Fragment取消(禁止)预加载延迟加载(懒加载)问题解决方案

>在Android中我们经常会用到ViewPager+Fragment组合。然而，有一个很让人头疼的问题就是，我们去加载数据的时候
由于ViewPager的内部机制所限制，所以它会默认至少预加载一个。这让人很郁闷，所以，我就想到要封装一个Fragment来解决这个问题。
>这里还解决一个问题就是在[Android酷炫欢迎页播放视频,仿蚂蜂窝自由行和慕课网](http://blog.csdn.net/linglongxin24/article/details/53115253)
 这里感谢有一位网友提出了bug,就是在播放视频的时候如果滑动到第二页和第三页，第一页的视频还在播放，这是个让人很头疼的问题，在这里也完美解决。
#1.问题初探
文章开始已经说过ViewPager的预加载机制。那么，我们可不可以设置ViewPager的预加载为0，不就解决问题了吗？

```java
        vp.setOffscreenPageLimit(0);
```
经过测试发现，根本不是这么回事，为什么呢?我们来看看Viewpager的setOffscreenPageLimit()方法的源码

```java
   private static final int DEFAULT_OFFSCREEN_PAGES = 1;
    /**
     * Set the number of pages that should be retained to either side of the
     * current page in the view hierarchy in an idle state. Pages beyond this
     * limit will be recreated from the adapter when needed.
     *
     * <p>This is offered as an optimization. If you know in advance the number
     * of pages you will need to support or have lazy-loading mechanisms in place
     * on your pages, tweaking this setting can have benefits in perceived smoothness
     * of paging animations and interaction. If you have a small number of pages (3-4)
     * that you can keep active all at once, less time will be spent in layout for
     * newly created view subtrees as the user pages back and forth.</p>
     *
     * <p>You should keep this limit low, especially if your pages have complex layouts.
     * This setting defaults to 1.</p>
     *
     * @param limit How many pages will be kept offscreen in an idle state.
     */
    public void setOffscreenPageLimit(int limit) {
        if (limit < DEFAULT_OFFSCREEN_PAGES) {
            Log.w(TAG, "Requested offscreen page limit " + limit + " too small; defaulting to "
                    + DEFAULT_OFFSCREEN_PAGES);
            limit = DEFAULT_OFFSCREEN_PAGES;
        }
        if (limit != mOffscreenPageLimit) {
            mOffscreenPageLimit = limit;
            populate();
        }
    }
```

我们发现，即使你设置为0，那么还是会在里面判断后设为默认值1。所以这个方法是行不通的。

#2.问题再探
我们发现Fragment中有一个setUserVisibleHint(boolean isVisibleToUser)方法，这个方法就是告诉用户，UI对用户是否可见，那么我们在这里去加载数据会怎么样呢？
![日志](https://github.com/linglongxin24/ViewPagerFragmentLazyLoad/blob/master/screenshorts/log_error.png?raw=true)
这又是为什么呢？
因为ViewPager会加载好多Fragment，为了节省内容等会在Fragment不可见的某个时候调用onDestroyView()将用户界面销毁掉但是Fragment的实例还在，所以可能第一次加载没有问题，
但是再次回到第一个Fragment再去加载的时候就会出现UI对用户可见但是视图还没有初始化。
#3.最终解决方案

```java
package cn.bluemobi.dylan.viewpagerfragmentlazyload;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Fragment预加载问题的解决方案：
 * 1.可以懒加载的Fragment
 * 2.切换到其他页面时停止加载数据（可选）
 * Created by yuandl on 2016-11-17.
 * blog ：http://blog.csdn.net/linglongxin24/article/details/53205878
 */

public abstract class LazyLoadFragment extends Fragment {
    /**
     * 视图是否已经初初始化
     */
    protected boolean isInit = false;
    protected boolean isLoad = false;
    protected final String TAG = "LazyLoadFragment";
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(setContentView(), container, false);
        isInit = true;
        /**初始化的时候去加载数据**/
        isCanLoadData();
        return view;
    }

    /**
     * 视图是否已经对用户可见，系统的方法
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isCanLoadData();
    }

    /**
     * 是否可以加载数据
     * 可以加载数据的条件：
     * 1.视图已经初始化
     * 2.视图对用户可见
     */
    private void isCanLoadData() {
        if (!isInit) {
            return;
        }

        if (getUserVisibleHint()) {
            lazyLoad();
            isLoad = true;
        } else {
            if (isLoad) {
                stopLoad();
            }
        }
    }

    /**
     * 视图销毁的时候讲Fragment是否初始化的状态变为false
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isInit = false;
        isLoad = false;

    }

    protected void showToast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 设置Fragment要显示的布局
     *
     * @return 布局的layoutId
     */
    protected abstract int setContentView();

    /**
     * 获取设置的布局
     *
     * @return
     */
    protected View getContentView() {
        return view;
    }

    /**
     * 找出对应的控件
     *
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T findViewById(int id) {

        return (T) getContentView().findViewById(id);
    }

    /**
     * 当视图初始化并且对用户可见的时候去真正的加载数据
     */
    protected abstract void lazyLoad();

    /**
     * 当视图已经对用户不可见并且加载过数据，如果需要在切换到其他页面时停止加载数据，可以覆写此方法
     */
    protected void stopLoad() {
    }
}

}

```

#4.用法

LazyLoadFragment是一个抽象类，可以作为BaseFragment,继承它。

 * (1).用setContentView()方法去加载要显示的布局

 * (2).lazyLoad()方法去加载数据
 * (3).stopLoad()方法可选，当视图已经对用户不可见并且加载过数据，如果需要在切换到其他页面时停止加载数据，可以覆写此方法
 
 ```java
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


 ```
 
#5.看效果界面

![界面](https://github.com/linglongxin24/ViewPagerFragmentLazyLoad/blob/master/screenshorts/userinterface.jpg?raw=true)
![Log](https://github.com/linglongxin24/ViewPagerFragmentLazyLoad/blob/master/screenshorts/log.png?raw=true)


#6.[GitHub](https://github.com/linglongxin24/ViewPagerFragmentLazyLoad)