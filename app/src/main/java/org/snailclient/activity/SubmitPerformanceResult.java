package org.snailclient.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import org.androidpn.demoapp.R;
import org.snailclient.activity.fragments.ReportExistFragment;
import org.snailclient.activity.fragments.ReportNewFragment;

import java.util.ArrayList;
import java.util.List;

public class SubmitPerformanceResult extends AppCompatActivity {

	private TabLayout headerLayout;
	private ViewPager contentLayout;

	private String tid;
	private String packageName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_performance_result);
		initData();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {

		Bundle data = getIntent().getExtras();
		if(data != null){
			tid = data.getString("tid");
			packageName = data.getString("packageName");
		}


		headerLayout = (TabLayout)findViewById(R.id.main_tab);
		contentLayout = (ViewPager) findViewById(R.id.main_viewpager);

		List<String> headList = new ArrayList<>();
		headList.add("新建报告");
		headList.add("已有报告");

		//设置tab的模式
		headerLayout.setTabMode(TabLayout.MODE_FIXED);
		//添加tab选项卡
		for (int i = 0; i < headList.size(); i++) {
			headerLayout.addTab(headerLayout.newTab().setText(headList.get(i)));
		}
		//把TabLayout和ViewPager关联起来
//		headerLayout.setupWithViewPager(contentLayout);

//		contentLayout
		final PagerAdapter adapter = new PagerAdapter
				(getSupportFragmentManager(), headerLayout.getTabCount());
		contentLayout.setAdapter(adapter);
		contentLayout.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(headerLayout));
		headerLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				contentLayout.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
	}

	public class PagerAdapter extends FragmentStatePagerAdapter {
		int nNumOfTabs;

		public PagerAdapter(FragmentManager fm, int nNumOfTabs)
		{
			super(fm);
			this.nNumOfTabs=nNumOfTabs;
		}
		@Override
		public Fragment getItem(int position) {
			switch(position)
			{
				case 0:
					ReportNewFragment tab1 = ReportNewFragment.newInstance(tid,packageName);
					return tab1;
				case 1:
					ReportExistFragment tab2 = ReportExistFragment.newInstance(tid);
					return tab2;
			}
			return null;
		}

		@Override
		public int getCount() {
			return nNumOfTabs;
		}
	}
}
