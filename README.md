#Android中ViewPager+Fragment懒加载

>在Android中我们经常会用到ViewPager+Fragment组合。然而，有一个很让人头疼的问题就是，我们去加载数据的时候
由于ViewPager的内部机制所限制，所以它会默认至少预加载一个。这让人很郁闷，所以，我就想到要封装一个Fragment来解决这个问题。

#1.问题初探
文章开始已经说过ViewPager的预加载机制。那么，我们可不可以设置ViewPager的预加载为0，不就解决问题了吗？

```java
        vp.setOffscreenPageLimit(0);
```
经过测试发现，根本不是这么回事，为什么呢?我们来看看Viewpager的setOffscreenPageLimit()方法的源码

```

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


